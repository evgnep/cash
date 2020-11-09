package su.nepom.cash.phone.model;

import androidx.lifecycle.MutableLiveData;
import su.nepom.cash.dto.AccountDto;
import su.nepom.cash.phone.rest.AccountApi;

import java.util.Comparator;
import java.util.List;

public class AccountStorage extends Storage<AccountDto> {
    public AccountStorage(AccountApi api, MutableLiveData<State> state) {
        super(api, state);
    }

    @Override
    public long getId(AccountDto elem) {
        return elem.getId();
    }

    @Override
    protected void preprocessItems(List<AccountDto> items) {
        items.sort(Comparator.comparing(AccountDto::getName));
    }
}
