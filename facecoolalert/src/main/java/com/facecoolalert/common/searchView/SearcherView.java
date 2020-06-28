package com.facecoolalert.common.searchView;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.facecoolalert.R;
import com.facecoolalert.common.SearchableTextView.CheckableListAdapter;

public class SearcherView extends CardView {

    public EditText searchEditText;
    public CheckBox assignAllCheckBox;
    public ListView recyclerView;
    private CheckableListAdapter adapter;

    public SearcherView(Context context) {
        super(context);
        init();
    }

    public SearcherView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearcherView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);

        // Initialize views
        searchEditText = findViewById(R.id.search_subjects);
        assignAllCheckBox = findViewById(R.id.assignall);
        recyclerView = findViewById(R.id.recycleView);




    }


    public String getSearchQuery() {
        return searchEditText.getText().toString().trim();
    }



    public void setAssignAllCheckboxChecked(boolean isChecked) {
        assignAllCheckBox.setChecked(isChecked);
    }

    public void setTitle(String str)
    {
        searchEditText.setHint(str);
    }

    public void setAdapter(CheckableListAdapter adapter) {
        this.adapter=adapter;
        recyclerView.setAdapter(adapter);

        assignAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    adapter.checkAll();
                else
                    adapter.uncheckAll();
            }
        });



        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
