package su.nepom.cash.phone.model;

import androidx.lifecycle.MutableLiveData;
import su.nepom.cash.dto.AccountGroupDto;
import su.nepom.cash.phone.rest.AccountGroupApi;

public class AccountGroupStorage extends Storage<AccountGroupDto> {
    public AccountGroupStorage(AccountGroupApi api, MutableLiveData<State> state) {
        super(api, state);
    }

    @Override
    public long getId(AccountGroupDto elem) {
        return elem.getId();
    }
}
