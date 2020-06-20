package su.nepom.cash.server.remote.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.UserDto;
import su.nepom.cash.server.remote.mapper.UserMapper;
import su.nepom.cash.server.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository repository;
    private final UserMapper mapper;

    @GetMapping
    List<UserDto> getAll() {
        return repository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    UserDto getOne(@PathVariable long id) {
        return repository.findById(id).map(mapper::map).orElseThrow(EntityNotFoundException::new);
    }

    @PostMapping
    UserDto insert(@RequestBody UserDto user) {
        return mapper.map(repository.save(mapper.map(user)));
    }

    @PutMapping("/{id}")
    @Transactional
    UserDto update(@PathVariable long id, @RequestBody UserDto user) {
        if (!repository.existsById(id))
            throw  new EntityNotFoundException();
        user.setId(id);
        return mapper.map(repository.save(mapper.map(user)));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
