package com.facecoolalert.ui.Alerts.Subscribers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facecoolalert.R;
import com.facecoolalert.database.entities.Subscriber;
import com.facecoolalert.ui.Alerts.AlertFragment;

import java.util.List;

public class SubscribersAdapter extends RecyclerView.Adapter<SubscribersAdapter.ViewHolder> {

    private AlertFragment alertFragment;
    private List<Subscriber> dataList;

    // Constructor to initialize the data list
    public SubscribersAdapter(List<Subscriber> dataList, AlertFragment alertFragment) {
        this.dataList = dataList;
        this.alertFragment = alertFragment;
    }

    // ViewHolder to hold views
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public TextView emailTextView;
        public TextView smsNumberTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            emailTextView = itemView.findViewById(R.id.email);
            smsNumberTextView = itemView.findViewById(R.id.smsnumber);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscriber, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the views within the ViewHolder
        Subscriber data = dataList.get(position);
        holder.nameTextView.setText(data.getName());

        if(data.getEmail().isEmpty())
            holder.emailTextView.setText("--");
        else
            holder.emailTextView.setText(data.getEmail());

        if(data.getPhone().isEmpty())
            holder.smsNumberTextView.setText("--");
        else
            holder.smsNumberTextView.setText(data.getPhone());

//        holder.smsNumberTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);


        holder.itemView.setOnClickListener(v -> AlertFragment.mainActivity.addFragment(new AddSubscriberFragment(alertFragment,data)));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
