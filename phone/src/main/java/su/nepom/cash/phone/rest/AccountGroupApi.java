package su.nepom.cash.phone.rest;

import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import retrofit2.http.*;
import su.nepom.cash.dto.AccountGroupDto;

import java.util.List;

public interface AccountGroupApi extends Api<AccountGroupDto> {
}

interface AccountGroupRest {
    @GET("/api/account-group")
    Single<List<AccountGroupDto>> getAll();

    @GET("/api/account-group/{id}")
    Single<AccountGroupDto> get(@Path("id") long id);

    @POST("/api/account-group")
    Single<AccountGroupDto> insert(@Body AccountGroupDto elem);

    @PUT("/api/account-group/{id}")
    Single<AccountGroupDto> save(@Path("id") long id, @Body AccountGroupDto elem);

    @DELETE("/api/account-group/{id}")
    Completable delete(@Path("id") long id);
}

@RequiredArgsConstructor
class AccountGroupApiImpl implements AccountGroupApi {
    private final AccountGroupRest rest;

    @Override
    public Single<List<AccountGroupDto>> getAll() {
        return rest.getAll();
    }

    @Override
    public Single<AccountGroupDto> save(AccountGroupDto elem) {
        return rest.save(elem.getId(), elem);
    }

    @Override
    public Single<AccountGroupDto> insert(AccountGroupDto elem) {
        return rest.insert(elem);
    }

    @Override
    public Single<AccountGroupDto> get(long id) {
        return rest.get(id);
    }

    @Override
    public Completable delete(long id) {
        return rest.delete(id);
    }
}
