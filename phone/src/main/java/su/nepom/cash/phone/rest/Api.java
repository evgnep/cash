package su.nepom.cash.phone.rest;


import io.reactivex.Completable;
import io.reactivex.Single;

import java.util.List;

/**
 * Интерфейс для работы с сервером
 * T - XxxDto
 */
public interface Api<T> {
    Single<List<T>> getAll();

    Single<T> get(long id);

    Single<T> save(T elem);

    Single<T> insert(T elem);

    Completable delete(long id);
}
