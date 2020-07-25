package su.nepom.cash.server.remote.crud;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import su.nepom.cash.dto.RecordDto;
import su.nepom.cash.dto.RecordPartDto;
import su.nepom.cash.server.domain.Account;
import su.nepom.cash.server.domain.Record;
import su.nepom.cash.server.remote.mapper.RecordMapper;
import su.nepom.cash.server.repository.AccountRepository;
import su.nepom.cash.server.repository.RecordRepository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import static su.nepom.cash.server.remote.crud.SecurityUtils.ifChild;
import static su.nepom.cash.server.remote.crud.SecurityUtils.isChild;

@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
public class RecordController {
    private final RecordRepository repository;
    private final AccountRepository accountRepository;
    private final RecordMapper mapper;

    @PostMapping("/filter")
    Page<RecordDto> filter(@RequestBody FilterDto filter, Pageable pageable) {
        var isChild = isChild();
        var page = repository
                .findByFilter(filter.accountId, filter.dateStart, filter.dateFinish, isChild, pageable);

        if (isChild)
            page.getContent().forEach(Record::removePartsUnavailableToChild);

        return page.map(mapper::map);
    }

    @GetMapping("/{id}")
    RecordDto getOne(@PathVariable UUID id) {
        var record = repository.findById(id).orElseThrow(EntityNotFoundException::new);
        ifChild(name -> record.removePartsUnavailableToChild());
        return mapper.map(record);
    }

    private void checkAccountsAccessForChild(RecordDto record) {
        // если проводка от ребенка - он должен иметь доступ ко всем участвующим счетам
        var accountIds = record.getParts().stream().map(RecordPartDto::getAccount).collect(Collectors.toList());
        if (!accountRepository.findAllById(accountIds).stream().allMatch(Account::isAvailableToChild))
            throw new ForbiddenException();
    }

    @PostMapping
    @Transactional
    RecordDto insert(@RequestBody RecordDto record) {
        if (record.getId() != null && repository.existsById(record.getId()))
            throw new EnityAlreadyExistsExeption();
        if (isChild())
            checkAccountsAccessForChild(record);
        return mapper.map(repository.save(mapper.map(record)));
    }

    @PutMapping("/{id}")
    @Transactional
    RecordDto update(@PathVariable UUID id, @RequestBody RecordDto record) {
        if (!repository.existsById(id))
            throw new EntityNotFoundException();
        record.setId(id);
        if (isChild()) {
            checkAccountsAccessForChild(record); // проверим и для новой записи
            // и для предыдущей
            var prevRecord = repository.findById(id).orElseThrow(); // эксепшена быть не должно (ранее проверили на наличие). Но Идея ругается
            if (!prevRecord.isAvailableToChild())
                throw new ForbiddenException();
        }
        return mapper.map(repository.save(mapper.map(record)));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id) {
        if (isChild()) {
            var prevRecord = repository.findById(id);
            if (prevRecord.isEmpty())
                return; // удаление отсуствующей - не ошибка
            if (!prevRecord.get().isAvailableToChild())
                throw new ForbiddenException();
        }
        repository.deleteById(id);
    }

    @Data
    @Accessors(chain = true)
    public static class FilterDto {
        private long accountId;
        private Instant dateStart, dateFinish;
    }
}
