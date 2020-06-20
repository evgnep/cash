package su.nepom.cash.server.remote.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.CurrencyDto;
import su.nepom.cash.server.remote.mapper.CurrencyMapper;
import su.nepom.cash.server.repository.CurrencyRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyRepository repository;
    private final CurrencyMapper mapper;

    @GetMapping
    List<CurrencyDto> getAll() {
        return repository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    CurrencyDto getOne(@PathVariable long id) {
        return repository.findById(id).map(mapper::map).orElseThrow(EntityNotFoundException::new);
    }

    @PostMapping
    CurrencyDto insert(@RequestBody CurrencyDto currency) {
        return mapper.map(repository.save(mapper.map(currency)));
    }

    @PutMapping("/{id}")
    @Transactional
    CurrencyDto update(@PathVariable long id, @RequestBody CurrencyDto currency) {
        if (!repository.existsById(id))
            throw  new EntityNotFoundException();
        currency.setId(id);
        return mapper.map(repository.save(mapper.map(currency)));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
