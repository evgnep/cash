package su.nepom.cash.phone.rest;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import su.nepom.cash.dto.UserDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Точка доступа ко всем Api для сервера
 */
public class NetworkService {
    private static final String BASE_URL = "http://192.168.1.10:8080";
    private final Retrofit mRetrofit;
    private final UserRest userRest;
    private final CurrencyApi currencyApi;
    private final AccountGroupApi accountGroupApi;
    private final AccountApi accountApi;
    private String user, password;
    private UserDto currentUser;

    public NetworkService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("Authorization", Credentials.basic(user, password, StandardCharsets.UTF_8))
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();

        userRest = mRetrofit.create(UserRest.class);
        currencyApi = new CurrencyApiImpl(mRetrofit.create(CurrencyRest.class));
        accountGroupApi = new AccountGroupApiImpl(mRetrofit.create(AccountGroupRest.class));
        accountApi = new AccountApiImpl(mRetrofit.create(AccountRest.class));
    }

    public Completable login(String user, String password) {
        this.user = user;
        this.password = password;
        currentUser = null;
        return userRest.getCurrentUser()
                .doOnSuccess(result -> currentUser = result).ignoreElement();
    }

    public CurrencyApi getCurrencyApi() {
        return currencyApi;
    }

    public AccountGroupApi getAccountGroupApi() {
        return accountGroupApi;
    }

    public AccountApi getAccountApi() {
        return accountApi;
    }

    public boolean isCurrentUser(String user) {
        return currentUser != null && currentUser.getName().equals(user);
    }
}
