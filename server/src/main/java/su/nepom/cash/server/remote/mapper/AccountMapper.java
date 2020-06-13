package su.nepom.cash.server.remote.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import su.nepom.cash.dto.AccountDto;
import su.nepom.cash.server.domain.Account;

@Mapper(config = MapperConfig.class)
public interface AccountMapper {
    @Mapping(target = "currency", source = "currency.id")
    AccountDto map(Account account);
    @InheritInverseConfiguration
    Account map(AccountDto account);
}
