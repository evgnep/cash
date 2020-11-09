package su.nepom.cash.phone.rest;

import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import retrofit2.http.*;
import su.nepom.cash.dto.AccountDto;

import java.util.List;

public interface AccountApi extends Api<AccountDto> {
}

interface AccountRest {
    @GET("/api/account")
    Single<List<AccountDto>> getAll();

    @GET("/api/account/{id}")
    Single<AccountDto> get(@Path("id") long id);

    @POST("/api/account")
    Single<AccountDto> insert(@Body AccountDto elem);

    @PUT("/api/account/{id}")
    Single<AccountDto> save(@Path("id") long id, @Body AccountDto elem);

    @DELETE("/api/account/{id}")
    Completable delete(@Path("id") long id);
}

@RequiredArgsConstructor
class AccountApiImpl implements AccountApi {
    private final AccountRest rest;

    @Override
    public Single<List<AccountDto>> getAll() {
        return rest.getAll();
    }

    @Override
    public Single<AccountDto> save(AccountDto elem) {
        return rest.save(elem.getId(), elem);
    }

    @Override
    public Single<AccountDto> insert(AccountDto elem) {
        return rest.insert(elem);
    }

    @Override
    public Single<AccountDto> get(long id) {
        return rest.get(id);
    }

    @Override
    public Completable delete(long id) {
        return rest.delete(id);
    }
}
