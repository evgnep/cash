package su.nepom.cash.server.remote.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import su.nepom.cash.server.domain.Account;
import su.nepom.cash.server.domain.Record;
import su.nepom.cash.server.domain.RecordPart;
import su.nepom.cash.server.domain.User;
import su.nepom.cash.server.remote.mapper.RecordMapper;
import su.nepom.cash.server.repository.RecordRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static su.nepom.cash.server.remote.crud.JsonWriter.json;
import static su.nepom.cash.server.remote.crud.ResponseBodyMatchers.responseBody;

@WebMvcTest({RecordController.class,
        RecordMapper.class})
@DisplayName("Rest-CRUD записей")
class RecordControllerTest {
    private final static String URL = "/api/record", URL_ID = URL + "/{id}";
    private final Record record = new Record()
            .setId(UUID.randomUUID())
            .setCreator(new User(101))
            .setNote("some note")
            .setCreateTime(Instant.ofEpochSecond(500))
            .setTime(Instant.ofEpochSecond(10000))
            .addPart(new RecordPart().setAccount(new Account(201)).setMoney(BigDecimal.valueOf(10)).setId(51).setNo(1))
            .addPart(new RecordPart().setAccount(new Account(202)).setMoney(BigDecimal.valueOf(-10)).setId(52).setNo(2));

    @MockBean
    private RecordRepository repository;
    @Autowired
    private RecordMapper mapper;
    @Autowired
    private MockMvc mvc;

    @Test
    void filter() throws Exception {
        var URL_filter = URL + "/filter";
        when(repository.findByFilter(anyLong(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(record), Pageable.unpaged(), 1));

        mvc.perform(post(URL_filter).with(json(new RecordController.FilterDto().setAccountId(1))))
                .andExpect(responseBody().containsPageAsJson(List.of(mapper.map(record))));

        verify(repository).findByFilter(eq(1L), eq(null), eq(null), any());

        mvc.perform(post(URL_filter).with(json(new RecordController.FilterDto().setAccountId(42)
                .setDateStart(Instant.ofEpochSecond(10))
                .setDateFinish(Instant.ofEpochSecond(100)))))
                .andExpect(status().isOk());

        verify(repository).findByFilter(eq(42L), eq(Instant.ofEpochSecond(10)), eq(Instant.ofEpochSecond(100)), any());
    }

    @Test
    void getOneRecord() throws Exception {
        when(repository.findById(record.getId())).thenReturn(Optional.of(record));

        mvc.perform(get(URL_ID, record.getId()))
                .andExpect(responseBody().containsObjectAsJson(mapper.map(record)));
    }

    @Test
    void insertRecord() throws Exception {
        var id = record.getId();
        record.setId(null);
        var dto = mapper.map(record);

        when(repository.save(any())).thenAnswer(a -> ((Record) a.getArgument(0)).setId(id));

        mvc.perform(post(URL).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(id)));
    }

    @Test
    void updateRecord() throws Exception {
        var id = record.getId();
        record.setId(null);
        var dto = mapper.map(record);

        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        mvc.perform(put(URL_ID, id).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(id)));

        verify(repository).save(eq(record.setId(id)));
    }

    @Test
    void deleteRecord() throws Exception {
        mvc.perform(delete(URL_ID, record.getId())).andExpect(status().isOk());
        verify(repository).deleteById(eq(record.getId()));
    }
}
