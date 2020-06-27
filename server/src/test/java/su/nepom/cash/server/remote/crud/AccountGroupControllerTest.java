package su.nepom.cash.server.remote.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import su.nepom.cash.server.domain.Account;
import su.nepom.cash.server.domain.AccountGroup;
import su.nepom.cash.server.remote.mapper.AccountGroupMapper;
import su.nepom.cash.server.repository.AccountGroupRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static su.nepom.cash.server.remote.crud.JsonWriter.json;
import static su.nepom.cash.server.remote.crud.ResponseBodyMatchers.responseBody;

@WebMvcTest({AccountGroupController.class,
        AccountGroupMapper.class})
@WithMockUser(roles = "PARENT")
@DisplayName("Rest-CRUD групп кошельков")
class AccountGroupControllerTest {
    private final static String URL = "/api/account-group", URL_ID = URL + "/{id}";
    private final Account account1 = new Account(1);
    private final Account account2 = new Account(2);
    private final AccountGroup group1 = new AccountGroup().setId(101).setName("Group1").addAccount(account1).addAccount(account2);

    @MockBean
    private AccountGroupRepository repository;
    @MockBean
    private UserDetailsService userDetailsService;
    @Autowired
    private AccountGroupMapper mapper;
    @Autowired
    private MockMvc mvc;


    @Test
    void getAll() throws Exception {
        var group2 = new AccountGroup().setId(102).setName("Group2").addAccount(account2);
        var list = List.of(group1, group2);
        when(repository.findAll()).thenReturn(list);

        mvc.perform(get(URL))
                .andExpect(responseBody().containsObjectAsJson(List.of(mapper.map(group1), mapper.map(group2))));
    }

    @Test
    void getOneGroup() throws Exception {
        when(repository.findById(101L)).thenReturn(Optional.of(group1));

        mvc.perform(get(URL_ID, 101))
                .andExpect(responseBody().containsObjectAsJson(mapper.map(group1)));
    }

    @Test
    void insertAccount() throws Exception {
        group1.setId(0);
        var dto = mapper.map(group1);

        when(repository.save(any())).thenAnswer(a -> ((AccountGroup) a.getArgument(0)).setId(42));

        mvc.perform(post(URL).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42)));
    }

    @Test
    void updateAccount() throws Exception {
        group1.setId(0);
        var dto = mapper.map(group1).setId(1);

        when(repository.existsById(anyLong())).thenReturn(true);
        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42)));

        verify(repository).save(eq(group1.setId(42)));
        verify(repository).existsById(eq(42L));
    }

    @Test
    void shouldThrowIfAccountDontExists() throws Exception {
        var dto = mapper.map(group1);
        when(repository.existsById(anyLong())).thenReturn(false);
        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(status().isNotFound());
    }

    @Test
    void deleteGroup() throws Exception {
        mvc.perform(delete(URL_ID, 42)).andExpect(status().isOk());
        verify(repository).deleteById(eq(42L));
    }

}
