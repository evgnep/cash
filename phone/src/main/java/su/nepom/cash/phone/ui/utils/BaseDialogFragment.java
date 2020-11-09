package su.nepom.cash.phone.ui.utils;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import su.nepom.cash.phone.App;
import su.nepom.cash.phone.R;
import su.nepom.cash.phone.model.Storage;

import java.util.function.Function;

/**
 * Вспомогательный класс для фрагмента редактирования элемента
 */
public abstract class BaseDialogFragment<T> extends Fragment {
    protected static final String ARG_POS = "pos";
    protected boolean closeOnOk;
    protected T element;
    protected int pos = -1;

    public static Bundle createBundle(int position, long id) {
        Bundle args = new Bundle();
        args.putInt(ARG_POS, id == -1 ? -1 : position);
        return args;
    }

    protected abstract void updateElementFromView();

    protected void onCreate(View view, Storage<T> storage, Function<T, T> cloneElement) {
        pos = getArguments() == null ? -1 : getArguments().getInt(ARG_POS);

        element = cloneElement.apply(pos == -1 ? null : storage.get(pos));

        view.<Button>findViewById(R.id.cancel).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        Button btDelete = view.findViewById(R.id.delete);
        if (pos == -1)
            btDelete.setVisibility(View.INVISIBLE);
        else
            btDelete.setOnClickListener(v ->
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.msg_question_delete)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                closeOnOk = true;
                                storage.delete(pos);
                            })
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {
                            })
                            .show()
            );

        view.<Button>findViewById(R.id.ok).setOnClickListener(v -> {
            closeOnOk = true;
            updateElementFromView();
            storage.set(pos, element);
        });

        App.get(this).getStorages().observeState(this, state -> {
            if (state.isInProgress())
                return;
            else if (state.hasError())
                Snackbar.make(getView(), state.getAndResetError().toString(), Snackbar.LENGTH_LONG).show();
            else if (closeOnOk)
                NavHostFragment.findNavController(this).navigateUp();
        });
    }
}
