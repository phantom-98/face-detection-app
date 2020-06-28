package com.facecoolalert.ui.Account;



import static com.facecoolalert.network.ErrorCodes.AUTH_TOO_MAN_REQUESTS;
import static com.facecoolalert.network.ErrorCodes.AUTH_USER_NOT_FOUND;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.design.buttons.PrimaryButton;

import com.facecoolalert.R;

import com.facecoolalert.di.module.NetworkModule;
import com.facecoolalert.sesssions.SessionManager;
import com.facecoolalert.ui.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.network.NetworkConfiguration;
import com.network.auth.AuthApiProvider;
import com.network.auth.AuthenticationApiProvider;
import com.network.auth.LoginRequest;
import com.network.auth.LoginResponse;
import com.network.common.APIClient;
import com.network.common.ErrorBodyResponse;
import com.network.common.NetworkResult;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class LoginActivity extends AppCompatActivity {



    private PrimaryButton loginButton;

    private TextView registerButton;

    private TextInputEditText emailField;

    private TextInputEditText passwordField;

    private TextView resetPassword;


    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();
        configureButtonsAndText();

    }

    private void initComponents() {
        loginButton=findViewById(R.id.btn_log_in_login);
        registerButton=findViewById(R.id.tv_log_in_sing_up_prompt);

        emailField=findViewById(R.id.iv_log_in_email_edit_text);
        passwordField=findViewById(R.id.iv_log_in_password_edit_text);
        resetPassword =findViewById(R.id.tv_log_in_reset_password);
    }

    private void configureButtonsAndText() {

        loginButton.setText(getString(R.string.login_title));
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent registerIntent=new Intent(LoginActivity.this,SIgnUpActivity.class);
                startActivity(registerIntent);

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setState(PrimaryButton.State.LOADING);
                attemptLogin();
//                loginButton.setState(PrimaryButton.State.ENABLED);
            }
        });

        resetPassword.setOnClickListener(v->{
            Intent resetPasswordIntent=new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(resetPasswordIntent);
        });

    }

    private void attemptLogin() {
        if(!validateData())
            return;

        LoginRequest loginRequest=new LoginRequest(emailField.getText().toString().trim(),passwordField.getText().toString());

        if(firebaseAuth==null)
            firebaseAuth=FirebaseAuth.getInstance();


        firebaseAuth.signInWithEmailAndPassword(loginRequest.getEmail(),loginRequest.getPassword())
                .addOnCompleteListener(this,OnLoginTask->{

                    if(OnLoginTask.isSuccessful()) {

                        Toast.makeText(LoginActivity.this, "Login Success, Checking Devices", Toast.LENGTH_SHORT).show();
                        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
                        //successful Runnable
                        Runnable successfulRunnable=()->{
                            //set loggedIn Device as this
                            UserDevice userDevice=new UserDevice(firebaseUser.getUid(),SessionManager.getDeviceIMEI(getApplicationContext()),new Date().getTime(),android.os.Build.MANUFACTURER + android.os.Build.MODEL);
                            firestore.collection("users_devices").document(userDevice.getUserId()).set(userDevice);
                            //end of setting loggedIn device
                            firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                                if(task.isSuccessful()) {
                                    try {
                                        SessionManager.saveToken(task.getResult().getToken(), getApplicationContext());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    successLogin();
                                }
                            });


                        };
                        //check if there is another logged in device

                        firestore.collection("users_devices").document(firebaseUser.getUid())
                                .get().addOnCompleteListener(task -> {
                                    if(task.isSuccessful())
                                    {
                                        DocumentSnapshot document = task.getResult();
                                        if(document.exists())
                                        {
                                            UserDevice userDevice=document.toObject(UserDevice.class);
                                            if(SessionManager.getDeviceIMEI(getApplicationContext()).equals(userDevice.getDeviceId()))
                                                successfulRunnable.run();
                                            else {
                                                //propmt device already Logged In
                                                new AlertDialog.Builder(this).setTitle("Another Device LoggedIn").setMessage("\nAnother Device "+userDevice.getDeviceName()+" has already LoggedIn using this account")
                                                        .setNegativeButton("Retry", (dialog, i) -> {
                                                            FirebaseAuth.getInstance().signOut();
                                                        })
                                                        .setPositiveButton("LogOut "+userDevice.getDeviceName(), ((dialogInterface, i) -> {
                                                            successfulRunnable.run();
                                                        })).create().show();
                                            }
                                        }
                                        else
                                            successfulRunnable.run();

                                    } else
                                    {
                                        FirebaseAuth.getInstance().signOut();
                                        Toast.makeText(this, "An error Occurred : "+task.getException(), Toast.LENGTH_SHORT).show();
                                        task.getException().printStackTrace();
                                    }

                                });

                        //end


//                            OnLoginTask.getResult().getUser().getIdToken()

                    }
                    else {
                        String friendlyError;
                        if(OnLoginTask.getException() instanceof FirebaseTooManyRequestsException) {
                            friendlyError=getString(R.string.login_error_too_many_request);
                        } else if (OnLoginTask.getException() instanceof FirebaseAuthInvalidUserException) {
                            friendlyError=getString(R.string.login_error_user_not_found);
                        } else if (OnLoginTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            friendlyError="Incorrect Password";
                        } else {
                            friendlyError="Error : "+OnLoginTask.getException();
                        }

                        Toast.makeText(this, friendlyError, Toast.LENGTH_SHORT).show();
                        resetPassword.setText(friendlyError);
                        loginButton.setState(PrimaryButton.State.ENABLED);
                    }
                });
    }


    private void attemptLogin2() {
        if(!validateData())
            return;

        LoginRequest loginRequest=new LoginRequest(emailField.getText().toString().trim(),passwordField.getText().toString());

        NetworkConfiguration networkConfiguration= NetworkModule.INSTANCE.provideNetworkConfiguration();
        APIClient apiClient= NetworkModule.INSTANCE.provideAPIClient(getApplication(),networkConfiguration,new HashMap<>());
        AuthApiProvider authenticationApiProvider=new AuthenticationApiProvider(networkConfiguration,apiClient,new Gson());


        authenticationApiProvider.logIn(loginRequest,new Continuation<Object>() {
            @NotNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NotNull Object result) {

                if (result instanceof NetworkResult) {
                    NetworkResult<LoginResponse> networkResult = (NetworkResult<LoginResponse>) result;

                    if(networkResult instanceof NetworkResult.Success)//successful login
                    {
//                        System.out.println("Logged In Successfully");
                        NetworkResult.Success<LoginResponse> responseSuccess= (NetworkResult.Success<LoginResponse>) networkResult;
                        LoginResponse loginResponse=responseSuccess.getData();
                        String token=loginResponse.getToken();
                        new Handler(Looper.getMainLooper()).post(()-> {
                            Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                        });
                        try {
                            SessionManager.saveToken(token,getApplicationContext());
                            successLogin();
                        } catch (IOException e) {
//                            throw new RuntimeException(e);
                            new Handler(Looper.getMainLooper()).post(()-> {
                                Toast.makeText(LoginActivity.this, "Failed to save Session", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else if (networkResult instanceof NetworkResult.Error) {
//                        System.out.println("Logged In Error");
                        NetworkResult.Error<LoginResponse> loginResponseError= (NetworkResult.Error<LoginResponse>) networkResult;
                        ErrorBodyResponse loginResponse=loginResponseError.getData();
                        String errorCode=loginResponse.getError().getCode();
//                        anonymoutText.setText();
                        String friendlyError="";
                        switch (errorCode) {
                            case AUTH_TOO_MAN_REQUESTS:
                                friendlyError=(getString(R.string.login_error_too_many_request));
                                break;
                            case AUTH_USER_NOT_FOUND:
                                friendlyError=(getString(R.string.login_error_user_not_found));
                                break;
                            default:
                                friendlyError=loginResponse.getMessage().toString();
                                break;
                        }

                        String finalFriendlyError = friendlyError;
                        new Handler(Looper.getMainLooper()).post(()->{
                        resetPassword.setText(finalFriendlyError);
                        loginButton.setState(PrimaryButton.State.ENABLED);
                            Toast.makeText(LoginActivity.this, finalFriendlyError, Toast.LENGTH_SHORT).show();
                        });

                    } else if (networkResult instanceof NetworkResult.Exception) {
//                        System.out.println("Logged In Successfully");
                        NetworkResult.Exception exception= (NetworkResult.Exception) networkResult;

                        new Handler(Looper.getMainLooper()).post(()->{
                        resetPassword.setText(exception.getE().toString());
                        loginButton.setState(PrimaryButton.State.ENABLED);
                            Toast.makeText(LoginActivity.this, resetPassword.getText(), Toast.LENGTH_SHORT).show();
                        });
                    }

                }
            }
        });

    }

    private void successLogin() {
        Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
        resetPassword.setTextColor(Color.GREEN);
        resetPassword.setText("Login Successful");
        Intent mainIntent=new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
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
            loginButton.setState(PrimaryButton.State.ENABLED);
            return false;
        }
    }
}