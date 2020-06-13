package su.nepom.cash.server.remote.crud;

import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.UserDto;
import su.nepom.cash.server.remote.mapper.UserMapper;
import su.nepom.cash.server.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserController(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping
    List<UserDto> getAll() {
        return repository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    UserDto getOne(@PathVariable long id) {
        return repository.findById(id).map(mapper::map).orElseThrow();
    }

    @PostMapping
    UserDto insert(@RequestBody UserDto user) {
        return mapper.map(repository.save(mapper.map(user)));
    }

    @PutMapping("/{id}")
    UserDto update(@PathVariable long id, @RequestBody UserDto user) {
        user.setId(id);
        return insert(user);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
