package com.facecoolalert.common.SearchableTextView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.facecoolalert.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CheckableListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> originalData;
    private LayoutInflater inflater;

    private ArrayList<String> filteredData;

    private LinkedList<String> checked;

    public CheckableListAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        this.originalData = data;
        this.filteredData = new ArrayList<>(originalData);
        inflater = LayoutInflater.from(context);
        checked=new LinkedList<>();
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_items_withcheckboxes, parent, false);
        }

        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        TextView textView = convertView.findViewById(R.id.textView);
        textView.setVisibility(View.GONE);

        String itemText = filteredData.get(position);
        //textView.setText(itemText);
        checkBox.setText(itemText);


        checkBox.setChecked(checked.contains(itemText));
        //System.out.println("checked has "+checked);


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String pitemText=buttonView.getText().toString();
                //System.out.println("has checked "+pitemText+" "+isChecked);
                if(isChecked) {
                    if(!checked.contains(pitemText))
                        checked.add(pitemText);
                }
                else
                    checked.remove(pitemText);

            }
        });

        return convertView;
    }

    public void filter(String query) {
        query = query.toLowerCase().trim(); // Convert to lowercase and remove leading/trailing spaces
        filteredData.clear();

        if (query.isEmpty()) {
            // If the query is empty, show all items
            filteredData.addAll(originalData);
        } else {
            // Otherwise, add items that match the query
            for (String item : originalData) {
                if (item.toLowerCase().contains(query)) {
                    filteredData.add(item);
                }
            }
        }
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    public void resetData() {
        filteredData.clear();
        filteredData.addAll(originalData);
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    public List<String> getCheckedItems() {

        return checked;
    }

    public void checkAll()
    {
        checked.clear();
        checked.addAll(originalData);
        notifyDataSetChanged();
    }

    public void uncheckAll()
    {
        checked.clear();
        notifyDataSetChanged();
    }


    public void setList(ArrayList<String> arrayList) {
        this.originalData=arrayList;
        this.filteredData=new ArrayList<>(originalData);
        notifyDataSetChanged();
    }
}
