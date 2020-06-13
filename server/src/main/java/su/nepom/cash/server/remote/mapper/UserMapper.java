package su.nepom.cash.server.remote.mapper;

import org.mapstruct.Mapper;
import su.nepom.cash.dto.UserDto;
import su.nepom.cash.server.domain.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserDto map(User user);
    User map(UserDto user);
}
