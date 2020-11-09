package su.nepom.cash.phone.ui.utils;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import com.google.android.material.snackbar.Snackbar;
import su.nepom.cash.phone.App;
import su.nepom.cash.phone.R;
import su.nepom.cash.phone.model.Storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Хелпер для реализации фрагментов-списков на основе хранилища
 */
@SuppressWarnings("unchecked")
public abstract class BaseListFragment<T> extends ListFragment {
    protected final int listItemLayout, listLayout;
    protected Storage<T> storage;

    public BaseListFragment(int listItemLayout) {
        this(listItemLayout, -1);
    }

    public BaseListFragment(int listItemLayout, int listLayout) {
        this.listItemLayout = listItemLayout;
        this.listLayout = listLayout;
    }

    protected abstract Storage<T> getStorage();

    protected abstract void dataToView(T elem, View view);

    protected boolean isMatchFilter(String filter, T element) {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storage = getStorage();
        App.get(this).getStorages().observeState(this, state -> {
            setListShown(!state.isInProgress());
            if (state.hasError())
                Snackbar.make(getView(), state.getAndResetError().toString(), Snackbar.LENGTH_LONG).show();
        });
        storage.observeItems(this, list -> {
            if (getListAdapter() != null)
                ((BaseListAdapter) getListAdapter()).onItemsUpdated();
            else
                setListAdapter(new BaseListAdapter(list, getContext()));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (listLayout == -1)
            return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(listLayout, container, false);
        EditText eFilter = view.findViewById(R.id.filter);

        eFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        View listView = super.onCreateView(inflater, container, savedInstanceState);
        LinearLayout listContainer = view.findViewById(R.id.list_content);
        listContainer.addView(listView);

        return view;
    }

    public void filter(CharSequence sequence) {
        ((BaseListAdapter) getListAdapter()).getFilter().filter(sequence);
    }

    public int unfilterPosition(int position) {
        BaseListAdapter adapter = ((BaseListAdapter) getListAdapter());
        if (adapter.filteredPositions == null)
            return position;
        else if (position == adapter.filteredPositions.size())
            return adapter.list.size();
        else
            return adapter.filteredPositions.get(position);
    }

    private class BaseListAdapter extends BaseAdapter implements Filterable {
        private final List<T> list;
        private final LayoutInflater inflater;
        private List<Integer> filteredPositions; // null, если фильтра нет. Иначе - номер отфильрованных элементов
        private MyFilter filter;

        public BaseListAdapter(List<T> list, Context context) {
            this.list = list;
            this.inflater = LayoutInflater.from(context);
        }

        public void onItemsUpdated() {
            if (filter != null && !filter.lastConstraint.isEmpty())
                filter.filter(filter.lastConstraint);
            else
                notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (filteredPositions == null ? list.size() : filteredPositions.size()) + 1;
        }

        @Override
        public Object getItem(int position) {
            if (position == (filteredPositions == null ? list.size() : filteredPositions.size()))
                return null;
            else if (filteredPositions == null)
                return list.get(position);
            else
                return list.get(filteredPositions.get(position));
        }

        @Override
        public long getItemId(int position) {
            if (position == (filteredPositions == null ? list.size() : filteredPositions.size()))
                return -1;
            else if (filteredPositions == null)
                return storage.getId(list.get(position));
            else
                return storage.getId(list.get(filteredPositions.get(position)));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(listItemLayout, parent, false);

            //noinspection unchecked
            dataToView((T) getItem(position), convertView);

            return convertView;
        }

        @Override
        public Filter getFilter() {
            if (filter == null)
                filter = new MyFilter();
            return filter;
        }

        class MyFilter extends Filter {
            private String lastConstraint = "";

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                lastConstraint = constraint == null ? "" : constraint.toString().trim().toLowerCase();

                if (lastConstraint.isEmpty())
                    return results;

                ArrayList<Integer> filtered = new ArrayList<>();
                for (int i = 0, alast = storage.size(); i < alast; ++i)
                    if (isMatchFilter(lastConstraint, storage.get(i)))
                        filtered.add(i);

                results.values = filtered;
                results.count = filtered.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                filteredPositions = (List<Integer>) results.values;
                notifyDataSetChanged();
            }
        }


    }
}
