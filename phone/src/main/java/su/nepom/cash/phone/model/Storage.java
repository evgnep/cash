package su.nepom.cash.phone.model;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import su.nepom.cash.phone.rest.Api;

import java.util.List;
import java.util.Objects;

/**
 * Базовый класс хранилища (кэша)
 * Элементы однократно запрашиваются через Api, а затем обновляются через Api и локально
 */
public abstract class Storage<T> {
    private final Api<T> api;
    private final MutableLiveData<List<T>> items = new MutableLiveData<>();
    private final MutableLiveData<State> state;

    public Storage(Api<T> api, MutableLiveData<State> state) {
        this.api = api;
        this.state = state;
    }

    public Completable start() {
        return api.getAll()
                .doOnSuccess(result -> {
                    preprocessItems(result);
                    items.postValue(result);
                })
                .ignoreElement();
    }

    public LiveData<State> getState() {
        return state;
    }

    private void checkNoOp() {
        if (state.getValue().isInProgress())
            throw new RuntimeException("Операция выполняется");
    }

    private void onStart() {
        state.getValue().setInProgress(true);
        state.getValue().setError(null);
        state.setValue(state.getValue());
    }

    private void onComplete(Throwable error) {
        state.getValue().setInProgress(false);
        state.getValue().setError(error);
        state.setValue(state.getValue());
    }

    public abstract long getId(T elem);

    protected void preprocessItems(List<T> items) {
    }

    public List<T> getItems() {
        return items.getValue();
    }

    public int size() {
        return Objects.requireNonNull(items.getValue()).size();
    }

    public T get(int i) {
        return Objects.requireNonNull(items.getValue()).get(i);
    }

    public T getById(long id) {
        if (items.getValue() == null)
            return null;
        for (T e : items.getValue())
            if (getId(e) == id)
                return e;
        return null;
    }

    public void set(int i, T elem) {
        if (i == -1) {
            add(elem);
            return;
        }

        checkNoOp();

        wrap(api.save(elem)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(returned -> {
                    items.getValue().set(i, returned);
                    items.setValue(items.getValue());
                }));
    }

    public void delete(int index) {
        checkNoOp();
        long id = getId(items.getValue().get(index));
        wrap(api.delete(id)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    items.getValue().remove(index);
                    items.setValue(items.getValue());
                }));
    }

    public void add(T elem) {
        checkNoOp();
        wrap(api.insert(elem)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(returned -> {
                    items.getValue().add(returned);
                    items.setValue(items.getValue());
                }));
    }

    public boolean isInProgress() {
        return state.getValue().isInProgress();
    }

    public void observeItems(LifecycleOwner owner, Observer<? super List<T>> observer) {
        items.observe(owner, observer);
    }

    private void wrapInner(Completable c) {
        c.subscribe(() -> onComplete(null), this::onComplete).isDisposed();
        onStart();
    }

    protected Completable wrap(Completable c) {
        c = c.cache();
        wrapInner(c);
        return c;
    }

    protected <T> Single<T> wrap(Single<T> s) {
        s = s.cache();
        wrapInner(s.ignoreElement());
        return s;
    }
}
