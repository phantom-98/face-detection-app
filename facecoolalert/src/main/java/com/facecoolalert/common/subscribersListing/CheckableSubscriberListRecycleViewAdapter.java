package com.facecoolalert.common.subscribersListing;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubscriberDistributionListDao;
import com.facecoolalert.database.entities.DistributionList;
import com.facecoolalert.database.entities.Subscriber;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CheckableSubscriberListRecycleViewAdapter extends RecyclerView.Adapter<CheckableSubscriberListRecycleViewAdapter.ViewHolder> {

    private SubscriberDistributionListDao subscriberDistributionListDao;
    private DistributionList list;
    private Context context;
    private ArrayList<String> originalData;
    private LayoutInflater inflater;

    private ArrayList<String> filteredData;

    private LinkedList<String> checked;

    public CheckableSubscriberListRecycleViewAdapter(Context context, ArrayList<String> data, DistributionList list) {
        this.list=list;
        this.context = context;
        this.originalData = data;
        this.filteredData = new ArrayList<>(originalData);
        inflater = LayoutInflater.from(context);
        checked = new LinkedList<String>();
        this.subscriberDistributionListDao= MyDatabase.getInstance(context).subscriberDistributionListDao();


        if(list!=null)
        {
            new Thread(()->{

                    for(Subscriber i: subscriberDistributionListDao.getSubscribersForList(list.getUid()))
                        checked.add(i.getName());
            }).start();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_subscriber_withcheckboxes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String itemText =filteredData.get(position);

        holder.checkBox.setText(itemText);

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
            for (String item : originalData) {
                if (item.toLowerCase().contains(query) ) {
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
        checked.addAll(originalData);
        new Handler(Looper.getMainLooper()).post(()->{
        notifyDataSetChanged();
        });
        }).start();
    }

    public void uncheckAll() {
        checked.clear();
        notifyDataSetChanged();
    }

    public void setList(ArrayList<String> arrayList) {
        this.originalData = arrayList;
        this.filteredData = new ArrayList<>(originalData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;


        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
