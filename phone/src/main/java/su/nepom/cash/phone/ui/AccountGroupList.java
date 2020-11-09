package su.nepom.cash.phone.ui;

import android.view.View;
import android.widget.TextView;
import su.nepom.cash.dto.AccountGroupDto;
import su.nepom.cash.phone.App;
import su.nepom.cash.phone.R;
import su.nepom.cash.phone.model.Storage;
import su.nepom.cash.phone.ui.utils.BaseListFragment;


public class AccountGroupList extends BaseListFragment<AccountGroupDto> {
    public AccountGroupList() {
        super(R.layout.item_list_content);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.account_groups);
    }

    @Override
    protected Storage<AccountGroupDto> getStorage() {
        return App.get(this).getStorages().getAccountGroupStorage();
    }

    @Override
    protected void dataToView(AccountGroupDto elem, View view) {
        TextView id = view.findViewById(R.id.id);
        TextView name = view.findViewById(R.id.content);

        if (elem != null) {
            id.setText(Long.toString(elem.getId()));
            name.setText(elem.getName());
        } else {
            id.setText("");
            name.setText("<Новая группа>");
        }
    }
}
