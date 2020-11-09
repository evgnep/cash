package su.nepom.cash.phone.model;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import lombok.var;
import su.nepom.cash.phone.rest.NetworkService;

import java.util.Arrays;

public class Storages {
    private final NetworkService networkService;
    private final MutableLiveData<State> asyncOperState = new MutableLiveData<>();
    private final CurrencyStorage currencyStorage;
    private final AccountGroupStorage accountGroupStorage;
    private final AccountStorage accountStorage;
    private boolean loadComplete = false;
    private Disposable loadDisposable;

    public Storages(NetworkService networkService) {
        asyncOperState.setValue(new State());
        this.networkService = networkService;
        currencyStorage = new CurrencyStorage(networkService.getCurrencyApi(), asyncOperState);
        accountGroupStorage = new AccountGroupStorage(networkService.getAccountGroupApi(), asyncOperState);
        accountStorage = new AccountStorage(networkService.getAccountApi(), asyncOperState);
    }

    public CurrencyStorage getCurrencyStorage() {
        return currencyStorage;
    }

    public AccountGroupStorage getAccountGroupStorage() {
        return accountGroupStorage;
    }

    public AccountStorage getAccountStorage() {
        return accountStorage;
    }

    public Completable login(String user, String password) {
        if (networkService.isCurrentUser(user) && loadComplete)
            return Completable.complete();

        if (loadDisposable != null)
            loadDisposable.dispose();

        loadComplete = false;

        Completable login = networkService.isCurrentUser(user) ? Completable.complete() : networkService.login(user, password);

        var loginAndLoad = login
                .andThen(Completable.merge(Arrays.asList(
                        currencyStorage.start(),
                        accountGroupStorage.start(),
                        accountStorage.start()
                )))
                .cache();
        loadDisposable = loginAndLoad.subscribe(() -> loadComplete = true, throwable -> {
        });
        return loginAndLoad;
    }

    public void observeState(LifecycleOwner owner, Observer<State> observer) {
        asyncOperState.observe(owner, observer);
    }

    public LiveData<State> getOperationState() {
        return asyncOperState;
    }
}
