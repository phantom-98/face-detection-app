package com.facecoolalert.common.searchView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.facecoolalert.R;
import com.facecoolalert.common.SearchableTextView.CheckableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchableView extends LinearLayout {

    public SearcherTextView searchableTextView;
    public SearcherView searcherView;
    private ArrayList<String> arrayList;
    private CheckableListAdapter adapter;

    public SearchableView(Context context) {
        super(context);
        init();
    }

    public SearchableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.selectable_search, this, true);
        // Find and save references to your UI components
        searchableTextView = findViewById(R.id.select_button);
        searcherView = findViewById(R.id.searchingview);


        searchableTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!dropped)
                {
                    searchableTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
                    searcherView.setVisibility(VISIBLE);

                }
                else
                {
                    searchableTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
                    searcherView.setVisibility(GONE);

                }


                dropped=!dropped;
            }
        });




    }

    private Boolean dropped=false;



    public SearcherView getSearcherView() {
        return searcherView;
    }

    public void setTitle(String str) {
//        SearcherTextView.setHint(str);
        searchableTextView.setHint(str);
        searcherView.setTitle(str);

    }

    public void setArrayList(ArrayList<String> arrayList) {
        this.arrayList = arrayList;
//        this.adapter.setList(arrayList);
        this.adapter = new CheckableListAdapter(getContext(), arrayList);
        searcherView.setAdapter(adapter);
    }

    public List<String> getChecked() {
        return adapter.getCheckedItems();
    }
}
