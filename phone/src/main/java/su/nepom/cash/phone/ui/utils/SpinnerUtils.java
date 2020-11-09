package su.nepom.cash.phone.ui.utils;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SpinnerUtils {

    public static <T> void populateSpinner(Spinner spinner, List<T> items, Function<T, IdName> converter, long current) {
        List<IdName> idNames = new ArrayList<>();

        int selected = -1;
        for (T elem : items) {
            IdName idName = converter.apply(elem);
            if (idName.id == current)
                selected = idNames.size();
            idNames.add(idName);
        }

        SpinnerAdapter adapter = new ArrayAdapter<IdName>(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item, idNames) {
            @Override
            public long getItemId(int position) {
                return getItem(position).id;
            }
        };
        spinner.setAdapter(adapter);
        if (selected != -1)
            spinner.setSelection(selected);
    }

    @AllArgsConstructor
    public static class IdName {
        public final long id;
        public final String name;

        @Override
        public String toString() {
            return name;
        }
    }
}
