package su.nepom.cash.server.remote.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import su.nepom.cash.dto.UserDto;
import su.nepom.cash.server.domain.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserDto map(User user);

    @AfterMapping
    static void hidePassword(@MappingTarget UserDto user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty())
            user.setPassword(User.NONEMPTY_PASSWORD);
    }

    @Mapping(target = "authorities", ignore = true)
    User map(UserDto user);
}
