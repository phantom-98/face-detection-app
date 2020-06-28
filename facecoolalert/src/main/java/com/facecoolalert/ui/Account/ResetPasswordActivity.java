package com.facecoolalert.ui.Account;



import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.design.buttons.PrimaryButton;

import com.facecoolalert.R;

import com.facecoolalert.sesssions.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.network.auth.LoginRequest;

import java.io.IOException;
import java.util.Date;

public class ResetPasswordActivity extends AppCompatActivity {



    private PrimaryButton resetButton;

    private TextView loginBotton;

    private TextInputEditText emailField;



    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initComponents();
        configureButtonsAndText();

    }

    private void initComponents() {
        resetButton =findViewById(R.id.btn_log_in_login);
        loginBotton =findViewById(R.id.tv_log_in_prompt);

        emailField=findViewById(R.id.iv_log_in_email_edit_text);


    }

    private void configureButtonsAndText() {

        resetButton.setText(getString(R.string.reset_password));


        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButton.setState(PrimaryButton.State.LOADING);
                attemptResetPassword();
//                loginButton.setState(PrimaryButton.State.ENABLED);
            }
        });

        loginBotton.setOnClickListener(v -> {
            Intent loginIntent=new Intent(this,LoginActivity.class);
            startActivity(loginIntent);
            finish();
        });



    }

    private void attemptResetPassword() {
        if(!validateData())
            return;


        if(firebaseAuth==null)
            firebaseAuth=FirebaseAuth.getInstance();


        firebaseAuth.sendPasswordResetEmail(emailField.getText().toString().trim())
                .addOnCompleteListener(this,resetPasswordTask->{

                    if(resetPasswordTask.isSuccessful()) {

                        Toast.makeText(this, "Reset Link sent to email", Toast.LENGTH_LONG).show();
                        resetButton.setState(PrimaryButton.State.ENABLED);
                    }
                    else {
                        String friendlyError;
                        if(resetPasswordTask.getException() instanceof FirebaseTooManyRequestsException) {
                            friendlyError=getString(R.string.login_error_too_many_request);
                        } else if (resetPasswordTask.getException() instanceof FirebaseAuthInvalidUserException) {
                            friendlyError=getString(R.string.login_error_user_not_found);
                        } else if (resetPasswordTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            friendlyError="Incorrect Password";
                        } else {
                            friendlyError="Error : "+resetPasswordTask.getException();
                        }

                        Toast.makeText(this, friendlyError, Toast.LENGTH_SHORT).show();
//                        anonymoutText.setText(friendlyError);
                        resetButton.setState(PrimaryButton.State.ENABLED);
                    }
                });
    }





    private boolean validateData() {
        if(Patterns.EMAIL_ADDRESS.matcher(
                        emailField.getText())
                .matches()
        )
        {
            emailField.setError(null);
            return true;
        }
        else
        {
            emailField.setError("Invalid EMail");
            resetButton.setState(PrimaryButton.State.ENABLED);
            return false;
        }
    }
}