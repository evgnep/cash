package su.nepom.cash.phone.model;

import androidx.lifecycle.MutableLiveData;
import su.nepom.cash.dto.CurrencyDto;
import su.nepom.cash.phone.rest.CurrencyApi;

import java.util.Comparator;
import java.util.List;

public class CurrencyStorage extends Storage<CurrencyDto> {
    public CurrencyStorage(CurrencyApi api, MutableLiveData<State> state) {
        super(api, state);
    }

    @Override
    public long getId(CurrencyDto elem) {
        return elem.getId();
    }

    @Override
    protected void preprocessItems(List<CurrencyDto> items) {
        items.sort(Comparator.comparing(CurrencyDto::getName));
    }
}
