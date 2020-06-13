package su.nepom.cash.server.remote.crud;

import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.CurrencyDto;
import su.nepom.cash.server.remote.mapper.CurrencyMapper;
import su.nepom.cash.server.repository.CurrencyRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {
    private final CurrencyRepository repository;
    private final CurrencyMapper mapper;

    public CurrencyController(CurrencyRepository repository, CurrencyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping
    List<CurrencyDto> getAll() {
        return repository.findAll().stream().map(mapper::map).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    CurrencyDto getOne(@PathVariable long id) {
        return repository.findById(id).map(mapper::map).orElseThrow();
    }

    @PostMapping
    CurrencyDto insert(@RequestBody CurrencyDto currency) {
        return mapper.map(repository.save(mapper.map(currency)));
    }

    @PutMapping("/{id}")
    CurrencyDto update(@PathVariable long id, @RequestBody CurrencyDto currency) {
        currency.setId(id);
        return insert(currency);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
        repository.deleteById(id);
    }
}
