package su.nepom.cash.server.remote.crud;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.AccountGroupDto;
import su.nepom.cash.server.remote.mapper.AccountGroupMapper;
import su.nepom.cash.server.repository.AccountGroupRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account-group")
@RequiredArgsConstructor
public class AccountGroupController {
    private final AccountGroupRepository repository;
    private final AccountGroupMapper mapper;

    @GetMapping
    List<AccountGroupDto> getAll() {
        return repository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    AccountGroupDto getOne(@PathVariable long id) {
        return repository.findById(id).map(mapper::map).orElseThrow(EntityNotFoundException::new);
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
