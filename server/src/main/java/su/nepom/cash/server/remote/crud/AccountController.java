package su.nepom.cash.server.remote.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.AccountDto;
import su.nepom.cash.server.remote.mapper.AccountMapper;
import su.nepom.cash.server.repository.AccountRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static su.nepom.cash.server.remote.crud.SecurityUtils.ifChild;
import static su.nepom.cash.server.remote.crud.SecurityUtils.isChild;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountRepository repository;
    private final AccountMapper mapper;

    @GetMapping
    List<AccountDto> getAll() {
        var child = isChild();
        return repository.findAll().stream()
                .filter(account -> !child || account.isAvailableToChild())
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    AccountDto getOne(@PathVariable long id) {
        var account = repository.findById(id).map(mapper::map).orElseThrow(EntityNotFoundException::new);
        ifChild(n -> account.isAvailableToChild());
        return account;
    }

    @PostMapping
    AccountDto insert(@RequestBody AccountDto account) {
        return mapper.map(repository.save(mapper.map(account)));
    }

    @PutMapping("/{id}")
    @Transactional
    AccountDto update(@PathVariable long id, @RequestBody AccountDto account) {
        if (!repository.existsById(id))
            throw new EntityNotFoundException();
        account.setId(id);
        return mapper.map(repository.save(mapper.map(account)));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
