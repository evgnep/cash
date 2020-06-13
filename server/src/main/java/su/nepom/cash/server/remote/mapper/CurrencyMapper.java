package su.nepom.cash.server.remote.mapper;

import org.mapstruct.Mapper;
import su.nepom.cash.dto.CurrencyDto;
import su.nepom.cash.server.domain.Currency;

@Mapper(config = MapperConfig.class)
public interface CurrencyMapper {
    CurrencyDto map(Currency currency);
    Currency map(CurrencyDto currency);
}
