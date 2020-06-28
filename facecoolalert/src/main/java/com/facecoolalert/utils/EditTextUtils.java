package com.facecoolalert.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class EditTextUtils {
    public static void addTextChangeListener(EditText editText, final TextChangeAction action) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newValue = s.toString();
                action.performAction(newValue);
            }
        });
    }

    public interface TextChangeAction {
        void performAction(String newText);
    }
}
