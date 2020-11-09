package su.nepom.cash.phone.ui;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import su.nepom.cash.dto.CurrencyDto;
import su.nepom.cash.phone.App;
import su.nepom.cash.phone.R;
import su.nepom.cash.phone.model.Storage;
import su.nepom.cash.phone.ui.utils.BaseListFragment;


public class CurrencyList extends BaseListFragment<CurrencyDto> {
    public CurrencyList() {
        super(R.layout.item_list_content);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.currencies);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        NavHostFragment.findNavController(this).navigate(R.id.action_currencyList_to_currencyDialog,
                CurrencyDialog.createBundle(position, id));
    }

    @Override
    protected Storage<CurrencyDto> getStorage() {
        return App.get(this).getStorages().getCurrencyStorage();
    }

    @Override
    protected void dataToView(CurrencyDto elem, View view) {
        TextView id = view.findViewById(R.id.id);
        TextView name = view.findViewById(R.id.content);

        if (elem != null) {
            id.setText(Long.toString(elem.getId()));
            name.setText(elem.getName());
        } else {
            id.setText("");
            name.setText("<Новая валюта>");
        }
    }
}
