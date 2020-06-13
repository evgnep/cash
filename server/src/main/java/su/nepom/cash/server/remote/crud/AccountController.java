package su.nepom.cash.server.remote.crud;

import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.AccountDto;
import su.nepom.cash.server.remote.mapper.AccountMapper;
import su.nepom.cash.server.repository.AccountRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountRepository repository;
    private final AccountMapper mapper;

    public AccountController(AccountRepository repository, AccountMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping
    List<AccountDto> getAll() {
        return repository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    AccountDto getOne(@PathVariable long id) {
        return repository.findById(id).map(mapper::map).orElseThrow();
    }

    @PostMapping
    AccountDto insert(@RequestBody AccountDto account) {
        return mapper.map(repository.save(mapper.map(account)));
    }

    @PutMapping("/{id}")
    AccountDto update(@PathVariable long id, @RequestBody AccountDto account) {
        account.setId(id);
        return insert(account);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
