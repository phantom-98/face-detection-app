package com.facecoolalert.common.subjectsListing;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.entities.WatchList;
import com.facecoolalert.database.entities.Subject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckableSubjecttListRecycleViewAdapter extends RecyclerView.Adapter<CheckableSubjecttListRecycleViewAdapter.ViewHolder> {

    private WatchList watchlist;
    private Context context;
    private ArrayList<Subject> originalData;
    private LayoutInflater inflater;

    private ArrayList<Subject> filteredData;

    private LinkedList<String> checked;

    public CheckableSubjecttListRecycleViewAdapter(Context context, ArrayList<Subject> data, WatchList watchlist) {
        this.watchlist=watchlist;
        this.context = context;
        this.originalData = data;
        this.filteredData = new ArrayList<>(originalData);
        inflater = LayoutInflater.from(context);
        checked = new LinkedList<>();

        if(watchlist!=null)
        {
            new Thread(()->{
                for(Subject i:originalData)
                    if(i.getWatchlist().equals(this.watchlist.getName()))
                        checked.add(i.getName());
            }).start();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_items_withcheckboxes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subj = filteredData.get(position);
        String itemText = subj.getName();
        holder.textView.setText(subj.getID());

        holder.checkBox.setText(subj.getName());

        holder.checkBox.setChecked(checked.contains(itemText));

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String pitemText = buttonView.getText().toString();
                if (isChecked) {
                    if (!checked.contains(pitemText))
                        checked.add(pitemText);
                } else {
                    checked.remove(pitemText);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void filter(String query) {
        query = query.toLowerCase().trim();
        filteredData.clear();

        if (query.isEmpty()) {
            filteredData.addAll(originalData);
        } else {
            for (Subject item : originalData) {
                if (item.getName().toLowerCase().contains(query) || (item.getID()!=null&&item.getID().toLowerCase().contains(query))) {
                    filteredData.add(item);
                }
            }
        }
        new android.os.Handler(Looper.getMainLooper()).post(()->{
        notifyDataSetChanged();
        });
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
        new Thread(()->{
            checked.clear();
            checked.addAll(originalData.stream().map(Subject::getName).collect(Collectors.toList()));
            new Handler(Looper.getMainLooper()).post(()->{
                notifyDataSetChanged();
            });
        }).start();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}
