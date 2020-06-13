package su.nepom.cash.server.remote.crud;

import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.AccountGroupDto;
import su.nepom.cash.server.remote.mapper.AccountGroupMapper;
import su.nepom.cash.server.repository.AccountGroupRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accountGroup")
public class AccountGroupController {
    private final AccountGroupRepository repository;
    private final AccountGroupMapper mapper;

    public AccountGroupController(AccountGroupRepository repository, AccountGroupMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping
    List<AccountGroupDto> getAll() {
        return repository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    AccountGroupDto getOne(@PathVariable long id) {
        return repository.findById(id).map(mapper::map).orElseThrow();
    }

    @PostMapping
    AccountGroupDto insert(@RequestBody AccountGroupDto group) {
        return mapper.map(repository.save(mapper.map(group)));
    }

    @PutMapping("/{id}")
    AccountGroupDto update(@PathVariable long id, @RequestBody AccountGroupDto group) {
        group.setId(id);
        return insert(group);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
