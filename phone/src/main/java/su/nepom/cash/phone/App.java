package su.nepom.cash.phone;

import android.app.Activity;
import android.app.Application;
import androidx.fragment.app.Fragment;
import su.nepom.cash.phone.model.Storages;
import su.nepom.cash.phone.rest.NetworkService;

public class App extends Application {
    private NetworkService networkService;
    private Storages storages;

    public static App get(Activity activity) {
        return (App) activity.getApplication();
    }

    public static App get(Fragment fragment) {
        return (App) fragment.getActivity().getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        networkService = new NetworkService();
        storages = new Storages(networkService);
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public Storages getStorages() {
        return storages;
    }
}
