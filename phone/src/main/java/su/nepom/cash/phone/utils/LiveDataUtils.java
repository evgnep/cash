package su.nepom.cash.phone.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;

public class LiveDataUtils {
    public static <R, A, B> LiveData<R> waitAll(LiveData<A> a, LiveData<B> b, BiFunction<A, B, R> combine) {
        return null;
    }

    @RequiredArgsConstructor
    private static class WaitAll<R, A, B> {
        private final LiveData<A> a;
        private final LiveData<B> b;
        private final BiFunction<A, B, R> combine;
        private final MutableLiveData<R> result = new MutableLiveData<>();
        private A resultA;
        private B resultB;
        private int completeCount = 0;
        private Observer<B> observeB = new Observer<B>() {
            @Override
            public void onChanged(B b) {
                resultB = b;
                onComplete();
            }
        };
        private Observer<A> observeA = new Observer<A>() {
            @Override
            public void onChanged(A a) {
                resultA = a;
                onComplete();
            }
        };

        private void onComplete() {
            if (++completeCount != 2)
                return;

            a.removeObserver(observeA);
            b.removeObserver(observeB);
            result.setValue(combine.apply(resultA, resultB));
        }
    }


}
