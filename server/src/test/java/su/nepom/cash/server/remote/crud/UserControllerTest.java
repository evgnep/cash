package su.nepom.cash.server.remote.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import su.nepom.cash.server.domain.User;
import su.nepom.cash.server.remote.mapper.UserMapper;
import su.nepom.cash.server.repository.UserRepository;

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

@WebMvcTest({UserController.class,
        UserMapper.class})
@WithMockUser(roles = "PARENT")
@DisplayName("Rest-CRUD пользователя")
class UserControllerTest {
    private final static String URL = "/api/user", URL_ID = URL + "/{id}";
    private final User user1 = new User().setId(1).setName("Test");

    @MockBean
    private UserRepository repository;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper mapper;
    @Autowired
    private MockMvc mvc;

    @Test
    void getAll() throws Exception {
        var user2 = new User().setId(2).setName("tseT");
        var list = List.of(user1, user2);
        when(repository.findAll()).thenReturn(list);

        mvc.perform(get(URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("Test"))
                .andExpect(jsonPath("$.[1].id").value("2"));
    }

    @Test
    void getOneUser() throws Exception {
        user1.setPassword("123");
        when(repository.findById(1L)).thenReturn(Optional.of(user1));

        mvc.perform(get(URL_ID, 1)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.password").value(User.NONEMPTY_PASSWORD));
    }

    @Test
    void insertUserWithNullPassword() throws Exception {
        var user = new User().setName("qwe").setChild(true);
        var dto = mapper.map(user);

        when(repository.save(any())).thenAnswer(a -> ((User) a.getArgument(0)).setId(1));

        mvc.perform(post(URL).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(1).setPassword("")));
    }

    @Test
    void insertUserWithSomePassword() throws Exception {
        var user = new User().setName("qwe").setPassword("123").setEnabled(true);
        var dto = mapper.map(user).setPassword("123");

        when(passwordEncoder.encode(any())).thenReturn("~123");
        when(repository.save(any())).thenAnswer(a -> ((User) a.getArgument(0)).setId(1));

        mvc.perform(post(URL).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(1).setPassword(User.NONEMPTY_PASSWORD)));
        verify(passwordEncoder).encode(eq("123"));
    }

    @Test
    void updateUserWithNullPassword() throws Exception {
        var user = new User().setName("qwe").setChild(true);
        var dto = mapper.map(user);
        user.setId(42);

        when(repository.findById(anyLong())).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42).setPassword("")));

        verify(repository).save(eq(user.setPassword("")));
        verify(repository).findById(eq(42L));
    }

    @Test
    void updateUserWithSomePassword() throws Exception {
        var user = new User().setName("qwe").setChild(true);
        var dto = mapper.map(user).setPassword(User.NONEMPTY_PASSWORD);
        user.setId(42).setPassword("123");

        when(repository.findById(anyLong())).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42)));

        verify(repository).save(eq(user.setPassword("123")));
    }

    @Test
    void updateUserPassword() throws Exception {
        var user = new User().setName("qwe").setChild(true);
        var dto = mapper.map(user).setPassword("321");
        user.setId(42).setPassword("123");

        when(passwordEncoder.encode(any())).thenReturn("~321");
        when(repository.findById(anyLong())).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(responseBody().containsObjectAsJson(dto.setId(42).setPassword(User.NONEMPTY_PASSWORD)));

        verify(repository).save(eq(user.setPassword("~321")));
        verify(passwordEncoder).encode(eq("321"));
    }

    @Test
    void shouldThrowIfUserDontExists() throws Exception {
        var dto = mapper.map(user1);
        when(repository.existsById(anyLong())).thenReturn(false);
        mvc.perform(put(URL_ID, 42).with(json(dto))).andExpect(status().isNotFound());
    }

    @Test
    void deleteUser() throws Exception {
        mvc.perform(delete(URL_ID, 42)).andExpect(status().isOk());
        verify(repository).deleteById(eq(42L));
    }
}
