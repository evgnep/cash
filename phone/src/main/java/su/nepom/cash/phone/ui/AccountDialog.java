package su.nepom.cash.phone.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import su.nepom.cash.dto.AccountDto;
import su.nepom.cash.phone.App;
import su.nepom.cash.phone.R;
import su.nepom.cash.phone.model.Storages;
import su.nepom.cash.phone.ui.utils.BaseDialogFragment;
import su.nepom.cash.phone.ui.utils.SpinnerUtils;
import su.nepom.util.BigDecimals;

import java.math.BigDecimal;


public class AccountDialog extends BaseDialogFragment<AccountDto> {
    private EditText eName, eNote;
    private Switch swMoney, swClosed, swAvailableToChild;
    private Spinner sCurrency;

    @Override
    protected void updateElementFromView() {
        element.setName(eName.getText().toString());
        element.setNote(eNote.getText().toString());
        element.setMoney(swMoney.isChecked());
        element.setClosed(swClosed.isChecked());
        element.setAvailableToChild(swAvailableToChild.isChecked());
        element.setCurrency(sCurrency.getSelectedItemId());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_dialog, container, false);

        TextView vId = view.findViewById(R.id.id);
        eName = view.findViewById(R.id.name);
        eNote = view.findViewById(R.id.note);
        swMoney = view.findViewById(R.id.money);
        swClosed = view.findViewById(R.id.closed);
        swAvailableToChild = view.findViewById(R.id.availableToChild);
        sCurrency = view.findViewById(R.id.currency);

        Storages storages = App.get(this).getStorages();

        onCreate(view, storages.getAccountStorage(),
                e -> e == null ? new AccountDto(-1).setName("") : e.createCopy());

        vId.setText(pos == -1 ? "-" : Long.toString(element.getId()));
        eName.setText(element.getName());
        eNote.setText(element.getNote());
        swMoney.setChecked(element.isMoney());
        swClosed.setChecked(element.isClosed());
        swAvailableToChild.setChecked(element.isAvailableToChild());
        SpinnerUtils.populateSpinner(sCurrency, storages.getCurrencyStorage().getItems(),
                currency -> new SpinnerUtils.IdName(currency.getId(), currency.getName()),
                pos == -1 ? -1 : element.getCurrency());

        if (!BigDecimals.equalsValue(element.getTotal(), BigDecimal.ZERO))
            swClosed.setEnabled(false);

        return view;
    }
}
