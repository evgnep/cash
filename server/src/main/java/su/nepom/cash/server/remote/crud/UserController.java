package su.nepom.cash.server.remote.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.UserDto;
import su.nepom.cash.server.domain.User;
import su.nepom.cash.server.remote.mapper.UserMapper;
import su.nepom.cash.server.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    List<UserDto> getAll() {
        return repository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    UserDto getOne(@PathVariable long id) {
        return repository.findById(id).map(mapper::map).orElseThrow(EntityNotFoundException::new);
    }

    private void updatePassword(User user) {
        if (user.getPassword() == null)
            user.setPassword("");
        else if (!user.getPassword().isEmpty())
            user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    @PostMapping
    UserDto insert(@RequestBody UserDto userDto) {
        var user = mapper.map(userDto);
        updatePassword(user);
        return mapper.map(repository.save(user));
    }

    @PutMapping("/{id}")
    @Transactional
    UserDto update(@PathVariable long id, @RequestBody UserDto userDto) {
        var prevUser = repository.findById(id).orElseThrow(EntityNotFoundException::new);

        var user = mapper.map(userDto);
        user.setId(id);
        if (Objects.equals(user.getPassword(), User.NONEMPTY_PASSWORD))
            user.setPassword(prevUser.getPassword());
        else
            updatePassword(user);

        return mapper.map(repository.save(user));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
