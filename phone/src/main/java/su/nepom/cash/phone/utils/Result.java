package su.nepom.cash.phone.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import lombok.var;
import retrofit2.Call;
import retrofit2.Response;

import java.util.function.Consumer;
import java.util.function.Function;

public class Result<T> {
    private final T value;
    private final AppError error;

    private Result(T value, AppError error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> fail(String error) {
        return new Result<>(null, new AppError(error));
    }

    public static <T> Result<T> fail(AppError error) {
        return new Result<>(null, error);
    }

    public static <T> Result<T> failHttpStatus(int code) {
        return new Result<>(null, new AppError("HttpStatus: " + code));
    }

    public static <T> Result<T> failResponse(Response<?> response) {
        return failHttpStatus(response.code());
    }

    public static <R> LiveData<Result<R>> adaptCall(Call<R> call) {
        return adaptCall(call, x -> x);
    }

    public static <R, C> LiveData<Result<R>> adaptCall(Call<C> call, Function<C, R> transformResult) {
        CallAdapter<R, C> adapter = new CallAdapter<>();
        adapter.setTransformResult(transformResult);
        call.enqueue(adapter);
        return adapter.getResult();
    }

    public static <T, R> LiveData<Result<R>> andThen(LiveData<Result<T>> source, Function<T, LiveData<Result<R>>> transform) {
        var andThenResult = new MutableLiveData<Result<R>>();
        source.observeForever(new Observer<Result<T>>() {
            @Override
            public void onChanged(Result<T> result) {
                if (result.isError()) {
                    source.removeObserver(this);
                    andThenResult.setValue(Result.fail(result.getError()));
                } else {
                    var transformResult = transform.apply(result.getValue());
                    transformResult.observeForever(new Observer<Result<R>>() {
                        @Override
                        public void onChanged(Result<R> result) {
                            transformResult.removeObserver(this);
                            andThenResult.setValue(result);
                        }
                    });
                }
            }
        });
        return andThenResult;
    }

    public static <T> LiveData<Result<Void>> discardValue(LiveData<Result<T>> source) {
        return Transformations.map(source, x -> x.isError() ? Result.fail(x.getError()) : Result.success(null));
    }

    public boolean isOk() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }

    public T getValue() {
        return value;
    }

    public AppError getError() {
        return error;
    }

    public static class CallAdapter<R, C> implements retrofit2.Callback<C> {
        private final MutableLiveData<Result<R>> liveResult = new MutableLiveData<>();
        private Function<C, R> transformResult;
        private Consumer<AppError> onError;

        public void setTransformResult(Function<C, R> transformResult) {
            this.transformResult = transformResult;
        }

        public void setOnError(Consumer<AppError> onError) {
            this.onError = onError;
        }

        public LiveData<Result<R>> getResult() {
            return liveResult;
        }

        @Override
        public void onResponse(Call<C> call, Response<C> response) {
            if (!response.isSuccessful())
                processFailure(Result.failResponse(response));
            else {
                R result = null;
                if (transformResult != null)
                    result = transformResult.apply(response.body());
                liveResult.setValue(Result.success(result));
            }
        }

        @Override
        public void onFailure(Call<C> call, Throwable t) {
            processFailure(Result.fail(t.getMessage()));
        }

        private void processFailure(Result<R> result) {
            if (onError != null)
                onError.accept(result.getError());
            this.liveResult.setValue(result);
        }
    }
}
