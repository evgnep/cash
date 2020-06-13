package su.nepom.cash.server.remote.crud;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.RecordDto;
import su.nepom.cash.server.remote.mapper.RecordMapper;
import su.nepom.cash.server.repository.RecordRepository;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/record")
public class RecordController {
    private final RecordRepository repository;
    private final RecordMapper mapper;

    public RecordController(RecordRepository repository, RecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @PostMapping("/filter")
    Page<RecordDto> filter(@RequestBody FilterDto filter, Pageable pageable) {
        return repository
                .findByFilter(filter.accountId, filter.dateStart, filter.dateFinish, pageable)
                .map(mapper::map);
    }

    @GetMapping("/{id}")
    RecordDto getOne(@PathVariable UUID id) {
        return repository.findById(id).map(mapper::map).orElseThrow();
    }

    @PostMapping
    RecordDto insert(@RequestBody RecordDto record) {
        return mapper.map(repository.save(mapper.map(record)));
    }

    @PutMapping("/{id}")
    RecordDto update(@PathVariable UUID id, @RequestBody RecordDto record) {
        record.setId(id);
        return insert(record);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        repository.deleteById(id);
    }

    @Data
    @Accessors(chain = true)
    public static class FilterDto {
        private long accountId;
        private Instant dateStart, dateFinish;
    }
}
