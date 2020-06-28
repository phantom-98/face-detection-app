package com.facecoolalert.ui.Account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.design.buttons.PrimaryButton;
import com.facecoolalert.R;

public class ReadTermsAndConditions extends AppCompatActivity {

    private PrimaryButton backButton;

    private TextView terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        backButton=findViewById(R.id.btn_back);
        backButton.setText("Go Back");
        terms=findViewById(R.id.txt_terms_and_conditions);
        terms.setText(getString(R.string.terms_and_conditions_body));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();//close the terms and conditions activity
            }
        });


    }
}