package su.nepom.cash.server.remote.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.AccountGroupDto;
import su.nepom.cash.server.remote.mapper.AccountGroupMapper;
import su.nepom.cash.server.repository.AccountGroupRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static su.nepom.cash.server.remote.crud.SecurityUtils.ifChild;
import static su.nepom.cash.server.remote.crud.SecurityUtils.isChild;

@RestController
@RequestMapping("/api/account-group")
@RequiredArgsConstructor
public class AccountGroupController {
    private final AccountGroupRepository repository;
    private final AccountGroupMapper mapper;

    @GetMapping
    List<AccountGroupDto> getAll() {
        var isChild = isChild();
        return repository.findAll().stream()
                .filter(group -> !isChild || group.removeAccountsUnavailableToChild())
                .map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    AccountGroupDto getOne(@PathVariable long id) {
        var group = repository.findById(id).orElseThrow(EntityNotFoundException::new);
        ifChild(name -> group.removeAccountsUnavailableToChild());
        return mapper.map(group);
    }

    @PostMapping
    AccountGroupDto insert(@RequestBody AccountGroupDto group) {
        return mapper.map(repository.save(mapper.map(group)));
    }

    @PutMapping("/{id}")
    @Transactional
    AccountGroupDto update(@PathVariable long id, @RequestBody AccountGroupDto group) {
        if (!repository.existsById(id))
            throw  new EntityNotFoundException();
        group.setId(id);
        return mapper.map(repository.save(mapper.map(group)));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
