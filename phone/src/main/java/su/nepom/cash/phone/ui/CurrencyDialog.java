package su.nepom.cash.phone.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import su.nepom.cash.dto.CurrencyDto;
import su.nepom.cash.phone.App;
import su.nepom.cash.phone.R;
import su.nepom.cash.phone.ui.utils.BaseDialogFragment;


public class CurrencyDialog extends BaseDialogFragment<CurrencyDto> {
    private EditText eName, eCode;

    @Override
    protected void updateElementFromView() {
        element.setName(eName.getText().toString());
        element.setCode(eCode.getText().toString());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.currency_dialog, container, false);

        TextView vId = view.findViewById(R.id.id);
        eName = view.findViewById(R.id.name);
        eCode = view.findViewById(R.id.code);

        onCreate(view, App.get(this).getStorages().getCurrencyStorage(),
                e -> e == null ? new CurrencyDto(-1).setName("") : e.createCopy());

        vId.setText(pos == -1 ? "-" : Long.toString(element.getId()));
        eName.setText(element.getName());
        eCode.setText(element.getCode());

        return view;
    }
}
