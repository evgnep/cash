package su.nepom.cash.server.remote.mapper;

import org.mapstruct.*;
import su.nepom.cash.dto.AccountGroupDto;
import su.nepom.cash.dto.RecordDto;
import su.nepom.cash.dto.RecordPartDto;
import su.nepom.cash.server.domain.Account;
import su.nepom.cash.server.domain.AccountGroup;
import su.nepom.cash.server.domain.Record;
import su.nepom.cash.server.domain.RecordPart;

@Mapper(config = MapperConfig.class)
public interface AccountGroupMapper {
    @Mapping(target="accounts", ignore = true)
    AccountGroupDto map(AccountGroup group);

    @Mapping(target="accounts", ignore = true)
    @Mapping(target="removeAccount", ignore = true)
    AccountGroup map(AccountGroupDto group);

    @AfterMapping
    static void afterMapping(@MappingTarget AccountGroup trg, AccountGroupDto src) {
        for (var accountId : src.getAccounts())
            trg.addAccount(new Account(accountId));
    }

    @AfterMapping
    static void afterMapping(@MappingTarget AccountGroupDto trg, AccountGroup src) {
        for (var account : src.getAccounts())
            trg.getAccounts().add(account.getId());
    }
}
