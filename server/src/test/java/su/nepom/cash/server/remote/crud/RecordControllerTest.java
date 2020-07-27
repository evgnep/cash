package su.nepom.cash.server.remote.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import su.nepom.cash.server.domain.Account;
import su.nepom.cash.server.domain.Record;
import su.nepom.cash.server.domain.RecordPart;
import su.nepom.cash.server.domain.User;
import su.nepom.cash.server.remote.mapper.RecordMapper;
import su.nepom.cash.server.repository.AccountRepository;
import su.nepom.cash.server.repository.RecordRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static su.nepom.cash.server.remote.crud.JsonWriter.json;
import static su.nepom.cash.server.remote.crud.ResponseBodyMatchers.responseBody;

@WebMvcTest({RecordController.class,
        RecordMapper.class})
@WithMockUser(roles = "PARENT")
@DisplayName("Rest-CRUD записей")
class RecordControllerTest {
    private final static String URL = "/api/record", URL_ID = URL + "/{id}", URL_filter = URL + "/filter";
    private final Account account1 = new Account(201).setAvailableToChild(true);
    private final Account account2 = new Account(202).setAvailableToChild(false);
    private final Record record = createRecord(account1, account2);
    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private RecordRepository repository;

    private Record createRecord(Account account1, Account account2) {
        return new Record()
                .setId(UUID.randomUUID())
                .setCreator(new User(101))
                .setNote("some note")
                .setCreateTime(Instant.ofEpochSecond(500))
                .setTime(Instant.ofEpochSecond(10000))
                .addPart(new RecordPart().setAccount(account1).setMoney(BigDecimal.valueOf(10)).setId(51).setNo(1))
                .addPart(new RecordPart().setAccount(account2).setMoney(BigDecimal.valueOf(-10)).setId(52).setNo(2));
    }

    @MockBean
    private UserDetailsService userDetailsService;
    @Autowired
    private RecordMapper mapper;
    @Autowired
    private MockMvc mvc;

    @Test
    void filter() throws Exception {
        when(repository.findByFilter(anyLong(), any(), any(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of(record), Pageable.unpaged(), 1));

        mvc.perform(post(URL_filter).with(json(new RecordController.FilterDto().setAccountId(1))))
                .andExpect(responseBody().containsPageAsJson(List.of(mapper.map(record))));

        verify(repository).findByFilter(eq(1L), eq(null), eq(null), eq(false), any());

        mvc.perform(post(URL_filter).with(json(new RecordController.FilterDto().setAccountId(42)
                .setDateStart(Instant.ofEpochSecond(10))
                .setDateFinish(Instant.ofEpochSecond(100)))))
                .andExpect(status().isOk());

        verify(repository).findByFilter(eq(42L), eq(Instant.ofEpochSecond(10)), eq(Instant.ofEpochSecond(100)), eq(false), any());
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
    void shouldProhibitInsertIfAlreadyExists() throws Exception {
        var id = record.getId();
        var dto = mapper.map(record);
        when(repository.existsById(any())).thenReturn(true);

        mvc.perform(post(URL).with(json(dto))).andExpect(status().isMethodNotAllowed());

        verify(repository).existsById(eq(id));
    }

    @Test
    void updateRecord() throws Exception {
        var id = record.getId();
        record.setId(null);
        var dto = mapper.map(record);

        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(repository.existsById(any())).thenReturn(true);

        mvc.perform(put(URL_ID, id).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(id)));

        verify(repository).save(eq(record.setId(id)));
    }

    @Test
    void deleteRecord() throws Exception {
        mvc.perform(delete(URL_ID, record.getId())).andExpect(status().isOk());
        verify(repository).deleteById(eq(record.getId()));
    }

    @Test
    @WithAnonymousUser
    void shouldForbidAnonymous() throws Exception {
        mvc.perform(get(URL_ID, 42)).andExpect(status().isUnauthorized());
        mvc.perform(get(URL_filter)).andExpect(status().isUnauthorized());
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mvc.perform(put(URL_ID, 42)).andExpect(status().isUnauthorized());
        mvc.perform(post(URL_ID, 42)).andExpect(status().isUnauthorized());
        mvc.perform(delete(URL_ID, 42)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldPermitFilterForChildForAvailableAccount() throws Exception {
        // record - частично доступна
        var record2 = createRecord(account1, account1); // полностью доступна

        when(repository.findByFilter(anyLong(), any(), any(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of(record, record2), Pageable.unpaged(), 3));

        var dto1 = mapper.map(record);
        var dto2 = mapper.map(record2);
        dto1.getParts().remove(1);

        mvc.perform(post(URL_filter).with(json(new RecordController.FilterDto().setAccountId(1))))
                .andExpect(responseBody().containsPageAsJson(List.of(dto1, dto2)));

        verify(repository).findByFilter(eq(1L), eq(null), eq(null), eq(true), any());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldPermitGetOneForChildForAvailableAccount() throws Exception {
        when(repository.findById(record.getId())).thenReturn(Optional.of(record));

        var dto = mapper.map(record);
        dto.getParts().remove(1);

        mvc.perform(get(URL_ID, record.getId()))
                .andExpect(responseBody().containsObjectAsJson(dto));
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldForbidGetOneForChildForUnavailableAccount() throws Exception {
        account1.setAvailableToChild(false);
        when(repository.findById(record.getId())).thenReturn(Optional.of(record)); // полностью недоступна

        mvc.perform(get(URL_ID, record.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldCheckAccountsOnInsertForChild() throws Exception {
        account2.setAvailableToChild(true);
        when(accountRepository.findAllById(any())).thenReturn(List.of(account1, account2));

        var id = record.getId();
        record.setId(null);
        var dto = mapper.map(record);

        when(repository.save(any())).thenAnswer(a -> ((Record) a.getArgument(0)).setId(id));

        mvc.perform(post(URL).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(id)));

        verify(accountRepository).findAllById(List.of(account1.getId(), account2.getId()));
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldForbidInsertForChildIfUnavailableAccount() throws Exception {
        when(accountRepository.findAllById(any())).thenReturn(List.of(account1, account2));

        var id = record.getId();
        record.setId(null);
        var dto = mapper.map(record);

        mvc.perform(post(URL).with(json(dto))).andExpect(status().isForbidden());

        verify(repository, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldPermitUpdateForChildIfAvailableAccount() throws Exception {
        var id = record.getId();
        account2.setAvailableToChild(true);

        when(accountRepository.findAllById(any())).thenReturn(List.of(account1, account2));

        when(repository.findById(id)).thenReturn(Optional.of(record));
        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(repository.existsById(any())).thenReturn(true);

        var dto = mapper.map(record);
        dto.setId(null);

        mvc.perform(put(URL_ID, id).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(id)));

        verify(repository).save(eq(record.setId(id)));
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldForbidUpdateForChildIfNewHasUnavailableAccount() throws Exception {
        var id = record.getId();
        var dto = mapper.map(record);
        dto.setId(null);

        when(accountRepository.findAllById(any())).thenReturn(List.of(account1, account2));
        when(repository.existsById(any())).thenReturn(true);

        mvc.perform(put(URL_ID, id).with(json(dto))).andExpect(status().isForbidden());

        verify(repository, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldForbidUpdateForChildIfOldHasUnavailableAccount() throws Exception {
        Account account3 = new Account(203).setAvailableToChild(true);

        var id = record.getId();
        var dto = mapper.map(record);
        dto.setId(null);
        dto.getParts().get(1).setAccount(203); // новоя проводка имеет 201 и 203, оба доступны ребенку

        when(accountRepository.findAllById(any())).thenReturn(List.of(account1, account3));
        when(repository.existsById(any())).thenReturn(true);
        when(repository.findById(any())).thenReturn(Optional.of(record));

        mvc.perform(put(URL_ID, id).with(json(dto))).andExpect(status().isForbidden());

        verify(repository, never()).save(any());
        verify(repository).findById(eq(id));
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldPermitDeleteForChildIfAvailableAccount() throws Exception {
        account2.setAvailableToChild(true);
        var id = record.getId();

        when(repository.findById(any())).thenReturn(Optional.of(record));

        mvc.perform(delete(URL_ID, id)).andExpect(status().isOk());

        verify(repository).deleteById(eq(id));
        verify(repository).findById(eq(id));
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldForbidDeleteForChildIfUnavailableAccount() throws Exception {
        var id = record.getId();

        when(repository.findById(any())).thenReturn(Optional.of(record));

        mvc.perform(delete(URL_ID, id)).andExpect(status().isForbidden());

        verify(repository, never()).deleteById(any());
    }
}
