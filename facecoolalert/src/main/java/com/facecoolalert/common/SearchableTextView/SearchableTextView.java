package com.facecoolalert.common.SearchableTextView;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.facecoolalert.R;

import java.util.ArrayList;
import java.util.List;

public class SearchableTextView extends androidx.appcompat.widget.AppCompatTextView {

    private ArrayList<String> arrayList;
    private PopupWindow popupWindow; // Declare PopupWindow as a class member
    private TextView titleView;
    private CheckableListAdapter adapter;
    private View popupView;

    public SearchableTextView(Context context) {
        super(context);
        arrayList = new ArrayList<>();
        initFeatures();
        actions();

    }

    public SearchableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        arrayList = new ArrayList<>();
        initFeatures();
        actions();

    }

    public SearchableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        arrayList = new ArrayList<>();
        initFeatures();
        actions();

    }

    public void setArrayList(ArrayList<String> arrayList) {
        this.arrayList = arrayList;
        adapter.setList(arrayList);
    }

    private void initFeatures() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        this.popupView = inflater.inflate(R.layout.dialog_searchable_spinner, null);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Initialize and assign variables
        EditText editText = popupView.findViewById(R.id.edit_text);
        ListView listView = popupView.findViewById(R.id.list_view);
        this.titleView=popupView.findViewById(R.id.title);

        // Initialize your data source (arrayList) with the appropriate values

        // Create and set the adapter
        this.adapter = new CheckableListAdapter(getContext(), arrayList);
        listView.setAdapter(adapter);

        // Handle Mark All and UnMark All buttons
        popupView.findViewById(R.id.mark).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.checkAll();
            }
        });

        popupView.findViewById(R.id.unmark).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.uncheckAll();
            }
        });

        // Handle text filtering
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapter.filter(s.toString());
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Show the popup below the SearchableTextView
    }

    private void actions() {
        //final int yoffset=-popupWindo.getContentView().getHeight();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null) {
//                    //,0,yoffset); // Show popup below this TextView
                    //showAtLocation();
//                    popupWindow.showAsDropDown(SearchableTextView.this, 0, -SearchableTextView.this.getHeight());

//                    popupWindow.showAsDropDown(SearchableTextView.this);
                    popupWindow.showAtLocation(popupView, Gravity.NO_GRAVITY, 20, 20);


                }
            }
        });
    }

    public void setTitle(String str)
    {
        titleView.setText(str);
    }

    public List<String> getChecked()
    {
        return adapter.getCheckedItems();
    }
}
