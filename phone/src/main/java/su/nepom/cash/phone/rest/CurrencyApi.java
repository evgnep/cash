package su.nepom.cash.phone.rest;

import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import retrofit2.http.*;
import su.nepom.cash.dto.CurrencyDto;

import java.util.List;

public interface CurrencyApi extends Api<CurrencyDto> {
}

interface CurrencyRest {
    @GET("/api/currency")
    Single<List<CurrencyDto>> getAll();

    @GET("/api/currency/{id}")
    Single<CurrencyDto> get(@Path("id") long id);

    @POST("/api/currency")
    Single<CurrencyDto> insert(@Body CurrencyDto elem);

    @PUT("/api/currency/{id}")
    Single<CurrencyDto> save(@Path("id") long id, @Body CurrencyDto elem);

    @DELETE("/api/currency/{id}")
    Completable delete(@Path("id") long id);
}

@RequiredArgsConstructor
class CurrencyApiImpl implements CurrencyApi {
    private final CurrencyRest rest;

    @Override
    public Single<List<CurrencyDto>> getAll() {
        return rest.getAll();
    }

    @Override
    public Single<CurrencyDto> save(CurrencyDto elem) {
        return rest.save(elem.getId(), elem);
    }

    @Override
    public Single<CurrencyDto> insert(CurrencyDto elem) {
        return rest.insert(elem);
    }

    @Override
    public Single<CurrencyDto> get(long id) {
        return rest.get(id);
    }

    @Override
    public Completable delete(long id) {
        return rest.delete(id);
    }
}
