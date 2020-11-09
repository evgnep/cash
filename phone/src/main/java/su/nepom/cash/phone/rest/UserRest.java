package su.nepom.cash.phone.rest;

import io.reactivex.Single;
import retrofit2.http.GET;
import su.nepom.cash.dto.UserDto;

public interface UserRest {
    @GET("/api/user/current")
    Single<UserDto> getCurrentUser();
}
