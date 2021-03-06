package su.nepom.cash.server.remote.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import su.nepom.cash.server.domain.Currency;
import su.nepom.cash.server.remote.mapper.CurrencyMapper;
import su.nepom.cash.server.repository.CurrencyRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static su.nepom.cash.server.remote.crud.JsonWriter.json;
import static su.nepom.cash.server.remote.crud.ResponseBodyMatchers.responseBody;

@WebMvcTest({CurrencyController.class,
        CurrencyMapper.class})
@WithMockUser(roles = "PARENT")
@DisplayName("Rest-CRUD валюты")
class CurrencyControllerTest {
    private final static String URL = "/api/currency", URL_ID = URL + "/{id}";
    private final Currency currency1 = new Currency().setId(1).setName("Rub").setCode("r");

    @MockBean
    private CurrencyRepository repository;
    @MockBean
    private UserDetailsService userDetailsService;
    @Autowired
    private CurrencyMapper mapper;
    @Autowired
    private MockMvc mvc;

    @Test
    void getAll() throws Exception {
        var currency2 = new Currency().setId(2).setName("usd").setCode("u");
        var list = List.of(currency1, currency2);
        when(repository.findAll()).thenReturn(list);

        mvc.perform(get(URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("Rub"))
                .andExpect(jsonPath("$.[1].code").value("u"));
    }

    @Test
    void getOneCurrency() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(currency1));

        mvc.perform(get(URL_ID, 1)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rub"));
    }

    @Test
    void insertCurrency() throws Exception {
        currency1.setId(0);
        var dto = mapper.map(currency1);

        when(repository.save(any())).thenAnswer(a -> ((Currency)a.getArgument(0)).setId(42));

        mvc.perform(post(URL).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42)));
    }

    @Test
    void updateCurrency() throws Exception {
        currency1.setId(0);
        var dto = mapper.map(currency1);

        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(repository.existsById(anyLong())).thenReturn(true);

        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42)));

        verify(repository).save(eq(currency1.setId(42)));
    }

    @Test
    void deleteCurrency() throws Exception {
        mvc.perform(delete(URL_ID, 42)).andExpect(status().isOk());
        verify(repository).deleteById(eq(42L));
    }

    @Test
    @WithAnonymousUser
    void shouldForbidAnonymous() throws Exception {
        mvc.perform(get(URL_ID, 42)).andExpect(status().isUnauthorized());
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mvc.perform(put(URL_ID, 42)).andExpect(status().isUnauthorized());
        mvc.perform(post(URL_ID, 42)).andExpect(status().isUnauthorized());
        mvc.perform(delete(URL_ID, 42)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldPermitGetAllForChild() throws Exception {
        when(repository.findAll()).thenReturn(List.of(currency1));
        mvc.perform(get(URL)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldPermitGetOneForChild() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(currency1));
        mvc.perform(get(URL_ID, 1)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldForbidInsertForChild() throws Exception {
        currency1.setId(0);
        var dto = mapper.map(currency1);
        mvc.perform(post(URL).with(json(dto))).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldForbidUpdateForChild() throws Exception {
        currency1.setId(0);
        var dto = mapper.map(currency1);
        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CHILD")
    void shouldForbidDeleteForChild() throws Exception {
        mvc.perform(delete(URL_ID, 42)).andExpect(status().isForbidden());
    }
}

