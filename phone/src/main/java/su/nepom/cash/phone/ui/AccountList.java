package su.nepom.cash.phone.ui;

import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import su.nepom.cash.dto.AccountDto;
import su.nepom.cash.phone.App;
import su.nepom.cash.phone.R;
import su.nepom.cash.phone.model.CurrencyStorage;
import su.nepom.cash.phone.model.Storage;
import su.nepom.cash.phone.ui.utils.BaseListFragment;


public class AccountList extends BaseListFragment<AccountDto> {
    private CurrencyStorage currencyStorage;

    public AccountList() {
        super(R.layout.account_list_content, R.layout.list_with_filter);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.accounts);
        currencyStorage = App.get(this).getStorages().getCurrencyStorage();
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_accountList_to_accountDialog,
                        AccountDialog.createBundle(unfilterPosition(position), id));
    }

    @Override
    protected boolean isMatchFilter(String filter, AccountDto element) {
        return element.getName().toLowerCase().contains(filter);
    }

    @Override
    protected Storage<AccountDto> getStorage() {
        return App.get(this).getStorages().getAccountStorage();
    }

    @Override
    protected void dataToView(AccountDto elem, View view) {
        TextView id = view.findViewById(R.id.id);
        TextView name = view.findViewById(R.id.content);
        TextView currency = view.findViewById(R.id.currency);

        if (elem != null) {
            id.setText(Long.toString(elem.getId()));

            if (elem.isClosed()) {
                SpannableString s = new SpannableString(elem.getName());
                s.setSpan(new StrikethroughSpan(), 0, elem.getName().length(), 0);
                name.setText(s);
            } else
                name.setText(elem.getName());

            if (elem.getCurrency() == 1) // пока хардкодим рубли
                currency.setText("");
            else
                currency.setText(currencyStorage.getById(elem.getCurrency()).getName());
        } else {
            id.setText("");
            name.setText("<Новый кошелек>");
            currency.setText("");
        }
    }
}
