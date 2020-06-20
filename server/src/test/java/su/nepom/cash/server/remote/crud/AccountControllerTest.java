package su.nepom.cash.server.remote.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import su.nepom.cash.server.domain.Account;
import su.nepom.cash.server.domain.Currency;
import su.nepom.cash.server.remote.mapper.AccountMapper;
import su.nepom.cash.server.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static su.nepom.cash.server.remote.crud.JsonWriter.json;
import static su.nepom.cash.server.remote.crud.ResponseBodyMatchers.responseBody;

@WebMvcTest({AccountController.class,
        AccountMapper.class})
@DisplayName("Rest-CRUD кошельков")
class AccountControllerTest {
    private final static String URL = "/api/account", URL_ID = URL + "/{id}";
    private final Account account1 = new Account().setId(1).setName("Card")
            .setCurrency(new Currency(1)).setNote("note").setTotal(BigDecimal.TEN);

    @MockBean
    private AccountRepository repository;
    @Autowired
    private AccountMapper mapper;
    @Autowired
    private MockMvc mvc;

    @Test
    void getAll() throws Exception {
        var account2 = new Account().setId(2).setName("Cash");
        var list = List.of(account1, account2);
        when(repository.findAll()).thenReturn(list);

        mvc.perform(get(URL))
                .andExpect(responseBody().containsObjectAsJson(List.of(mapper.map(account1), mapper.map(account2))));
    }

    @Test
    void getOneAccount() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(account1));

        mvc.perform(get(URL_ID, 1))
                .andExpect(responseBody().containsObjectAsJson(mapper.map(account1)));
    }

    @Test
    void insertAccount() throws Exception {
        account1.setId(0);
        var dto = mapper.map(account1);

        when(repository.save(any())).thenAnswer(a -> ((Account) a.getArgument(0)).setId(42));

        mvc.perform(post(URL).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42)));
    }

    @Test
    void updateAccount() throws Exception {
        account1.setId(0);
        var dto = mapper.map(account1).setId(1);

        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(repository.existsById(anyLong())).thenReturn(true);

        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42)));

        verify(repository).save(eq(account1.setId(42)));
    }

    @Test
    void deleteAccount() throws Exception {
        mvc.perform(delete(URL_ID, 42)).andExpect(status().isOk());
        verify(repository).deleteById(eq(42L));
    }
}
