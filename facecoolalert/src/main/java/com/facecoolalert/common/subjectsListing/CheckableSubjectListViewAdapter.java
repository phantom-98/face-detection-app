package com.facecoolalert.common.subjectsListing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.facecoolalert.R;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.database.entities.WatchList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckableSubjectListViewAdapter extends BaseAdapter {

    private WatchList watchlist;
    private Context context;
    private List<Subject> originalData;
    private List<Subject> filteredData;
    private LinkedList<String> checked;
    private LayoutInflater inflater;

    public CheckableSubjectListViewAdapter(Context context, List<Subject> data, WatchList watchlist, LayoutInflater layoutInflater) {
        this.watchlist = watchlist;
        this.context = context;
        this.originalData = data;
        this.filteredData = new ArrayList<>(originalData);
        inflater = layoutInflater;
        checked = new LinkedList<>();

        if (watchlist != null) {
            new Thread(() -> {
                for (Subject subject : originalData) {
                    if (subject.getWatchlist().equals(this.watchlist.getName())) {
                        checked.add(subject.getName());
                    }
                }
            }).start();
        }
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Subject getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_items_withcheckboxes, parent, false);
        }

        CheckBox checkBox = view.findViewById(R.id.checkBox);
        TextView textView = view.findViewById(R.id.textView);

        Subject subject = filteredData.get(position);
        String itemText = subject.getName();

        textView.setText(subject.getID());
        checkBox.setText(subject.getName());
        checkBox.setChecked(checked.contains(itemText));

        checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            String pitemText = compoundButton.getText().toString();
            if (isChecked) {
                if (!checked.contains(pitemText)) {
                    checked.add(pitemText);
                }
            } else {
                checked.remove(pitemText);
            }
        });

        return view;
    }

    public void filter(String query) {
        query = query.toLowerCase().trim();
        filteredData.clear();

        if (query.isEmpty()) {
            filteredData.addAll(originalData);
        } else {
            for (Subject item : originalData) {
                if (item.getName().toLowerCase().contains(query) || (item.getID() != null && item.getID().toLowerCase().contains(query))) {
                    filteredData.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void resetData() {
        filteredData.clear();
        filteredData.addAll(originalData);
        notifyDataSetChanged();
    }

    public List<String> getCheckedItems() {
        return checked;
    }

    public void checkAll() {
        checked.clear();
        checked.addAll(originalData.stream().map(Subject::getName).collect(Collectors.toList()));
        notifyDataSetChanged();
    }

    public void uncheckAll() {
        checked.clear();
        notifyDataSetChanged();
    }

    public void setList(ArrayList<Subject> arrayList) {
        this.originalData = arrayList;
        this.filteredData = new ArrayList<>(originalData);
        notifyDataSetChanged();
    }
}
