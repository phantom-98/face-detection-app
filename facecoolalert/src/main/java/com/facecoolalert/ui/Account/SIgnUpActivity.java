package com.facecoolalert.ui.Account;

import static com.facecoolalert.network.ErrorCodes.AUTH_EMAIL_ALREADY_IN_USE;
import static com.facecoolalert.network.ErrorCodes.AUTH_TOO_MAN_REQUESTS;
import static com.facecoolalert.network.ErrorCodes.AUTH_USER_NOT_FOUND;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.design.buttons.PrimaryButton;
import com.facecoolalert.R;
import com.facecoolalert.di.module.NetworkModule;
import com.facecoolalert.sesssions.SessionManager;
import com.facecoolalert.ui.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.network.NetworkConfiguration;
import com.network.auth.AuthApiProvider;
import com.network.auth.AuthenticationApiProvider;
import com.network.auth.LoginResponse;
import com.network.auth.RegisterRequest;
import com.network.auth.RegisterResponse;
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

public class SIgnUpActivity extends AppCompatActivity {


    private TextView loginButton;

    private PrimaryButton registerButton;

    private TextInputEditText fullnameField;

    private TextInputEditText emailField;

    private TextInputEditText passwordField;

    private TextInputEditText confirmPasswordField;

    private CheckBox checkbox_accept_terms_and_conditions;

    private Button readTermsAndConditions;



    private TextInputEditText phoneTextField;


    private FirebaseAuth auth;
    private FirebaseFirestore firestore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);


        initComponents();
        configureTexts();

        configureClicks();
    }

    private void configureClicks() {

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SIgnUpActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerButton.setState(PrimaryButton.State.LOADING);
                attemptSignUp();
            }
        });

        readTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SIgnUpActivity.this,ReadTermsAndConditions.class);
                startActivity(intent);
            }
        });
    }

    private void attemptSignUp() {
        if(auth==null)
            auth = FirebaseAuth.getInstance();
        if(firestore==null)
            firestore = FirebaseFirestore.getInstance();

        if(validateData()) {
            RegisterRequest registerRequest = new RegisterRequest(
                    emailField.getText().toString().trim(),
                    passwordField.getText().toString(),
                    phoneTextField.getText().toString(),
                    android.os.Build.MANUFACTURER + android.os.Build.MODEL,
                    fullnameField.getText().toString()
            );

            //check if email is already reistered


            //create the acount.
            auth.createUserWithEmailAndPassword(registerRequest.getEmail(),registerRequest.getPassword())
                    .addOnCompleteListener(this,createUserTask -> {
                        if(createUserTask.isSuccessful())
                        {
                            FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                            String userId = firebaseUser.getUid();
                            AccountUser accountUser=new AccountUser(registerRequest.getEmail(),registerRequest.getPhone(),registerRequest.getName(),registerRequest.getDevice());
                            accountUser.setUserId(userId);

                            //saving user detail to firestore below
                            firestore.collection("users").document(userId).set(accountUser)//we save al other user details
                                    .addOnCompleteListener( this,saveUserDetailsTask->{
                                        if(saveUserDetailsTask.isSuccessful())
                                        {
                                            //set loggedIn Device as this
                                            UserDevice userDevice=new UserDevice(userId,SessionManager.getDeviceIMEI(getApplicationContext()),new Date().getTime(),android.os.Build.MANUFACTURER + android.os.Build.MODEL);
                                            firestore.collection("users_devices").document(userId).set(userDevice);
                                            //end of setting loggedIn device
                                            firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                                                if(task.isSuccessful()) {
                                                    try {
                                                        SessionManager.saveToken(task.getResult().getToken(), getApplicationContext());
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    Toast.makeText(SIgnUpActivity.this, "SignUp Success", Toast.LENGTH_SHORT).show();
                                                    successSignUp();
                                                    registerButton.setState(PrimaryButton.State.ENABLED);
                                                }
                                            });

                                        }
                                        else
                                        {
                                            Toast.makeText(SIgnUpActivity.this, "Failed to save User Details", Toast.LENGTH_SHORT).show();
                                            registerButton.setState(PrimaryButton.State.ENABLED);
                                        }

                                    });
//                            Toast.makeText(SIgnUpActivity.this, "SignUp Success", Toast.LENGTH_SHORT).show();
//                                            successSignUp();
//                                            registerButton.setState(PrimaryButton.State.ENABLED);

                        } else  {
                            if(createUserTask.getException() instanceof FirebaseAuthUserCollisionException)
                            {
                                Toast.makeText(SIgnUpActivity.this, "Email is already in use", Toast.LENGTH_SHORT).show();
                                registerButton.setState(PrimaryButton.State.ENABLED);
                            }
                            else {
                                Toast.makeText(SIgnUpActivity.this, "Error Signing Up : " + createUserTask.getException(), Toast.LENGTH_SHORT).show();
                                registerButton.setState(PrimaryButton.State.ENABLED);
                            }
                        }


                    });


        }
        else
            registerButton.setState(PrimaryButton.State.ENABLED);

    }

    private void attemptSignUp2() {

        if(validateData())
        {
            RegisterRequest registerRequest= new RegisterRequest(
                emailField.getText().toString().trim(),
                    passwordField.getText().toString(),
                    phoneTextField.getText().toString(),
                    android.os.Build.MANUFACTURER + android.os.Build.MODEL,
                    fullnameField.getText().toString()
            );

            NetworkConfiguration networkConfiguration= NetworkModule.INSTANCE.provideNetworkConfiguration();
            APIClient apiClient= NetworkModule.INSTANCE.provideAPIClient(getApplication(),networkConfiguration,new HashMap<>());
            AuthApiProvider authenticationApiProvider=new AuthenticationApiProvider(networkConfiguration,apiClient,new Gson());


            authenticationApiProvider.register(registerRequest,new Continuation<Object>() {
                @NotNull
                @Override
                public CoroutineContext getContext() {
                    return EmptyCoroutineContext.INSTANCE;
                }

                @Override
                public void resumeWith(@NotNull Object result) {

                    if (result instanceof NetworkResult) {
                        NetworkResult<RegisterResponse> networkResult = (NetworkResult<RegisterResponse>) result;

                        System.out.println("Register Result : "+networkResult.toString());
                        if(networkResult instanceof NetworkResult.Success)//successful login
                        {
//                        System.out.println("Registered Successfully");
                            NetworkResult.Success<RegisterResponse> responseSuccess= (NetworkResult.Success<RegisterResponse>) networkResult;
                            RegisterResponse registerResponse=responseSuccess.getData();
                            String token=registerResponse.getToken();

                            try {
                                SessionManager.saveToken(token,getApplicationContext());
                                new Handler(Looper.getMainLooper()).post(()-> {
                                    Toast.makeText(SIgnUpActivity.this, "SignUp Success", Toast.LENGTH_SHORT).show();
                                    successSignUp();
                                });
                            } catch (IOException e) {
                                new Handler(Looper.getMainLooper()).post(()-> {
                                    Toast.makeText(SIgnUpActivity.this, "Failed to save Session", Toast.LENGTH_SHORT).show();
                                });
                            }

                        } else if (networkResult instanceof NetworkResult.Error) {
                        System.out.println("Register Error");
                            NetworkResult.Error<RegisterResponse> registerResponseError= (NetworkResult.Error<RegisterResponse>) networkResult;
                            ErrorBodyResponse registerResponse=registerResponseError.getData();
                            String errorCode=registerResponse.getError().getCode();
//                        anonymoutText.setText();
                            String friendlyError="";
                            switch (errorCode) {
                                case AUTH_EMAIL_ALREADY_IN_USE:
                                    friendlyError=(getString(R.string.register_error_email_already_in_use));
                                    break;
                                default:
                                    friendlyError=registerResponse.getError().getName();
                                    break;
                            }

                            String finalFriendlyError = friendlyError;
                            new Handler(Looper.getMainLooper()).post(()->{

                                registerButton.setState(PrimaryButton.State.ENABLED);
                                Toast.makeText(SIgnUpActivity.this, finalFriendlyError, Toast.LENGTH_SHORT).show();
                            });

                        } else if (networkResult instanceof NetworkResult.Exception) {
                        System.out.println("Register Exception");
                            NetworkResult.Exception exception= (NetworkResult.Exception) networkResult;


                            exception.getE().printStackTrace();
                            new Handler(Looper.getMainLooper()).post(()->{

                                registerButton.setState(PrimaryButton.State.ENABLED);
                                Toast.makeText(SIgnUpActivity.this, exception.getE().toString(), Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                }
            });
        }
        else
        {
            registerButton.setState(PrimaryButton.State.ENABLED);
        }


    }

    private void successSignUp() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private boolean validateData() {
        if(fullnameField.getText().toString().isEmpty()||fullnameField.getText().toString().trim().length()<2)
        {
            fullnameField.setError("You Must Enter your Name");
            Toast.makeText(this, "You Must Enter your Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        fullnameField.setError(null);

        if(!Patterns.EMAIL_ADDRESS.matcher(emailField.getText().toString()).matches())
        {
            emailField.setError("Invalid Email");
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        emailField.setError(null);

        if(passwordField.getText().toString().length()<6) {
            passwordField.setError(getString(R.string.login_error_password_length));
            Toast.makeText(this, getString(R.string.login_error_password_length), Toast.LENGTH_SHORT).show();
            return false;
        }
        passwordField.setError(null);

        if(!passwordField.getText().toString().equals(confirmPasswordField.getText().toString()))
        {
            Toast.makeText(this, getString(R.string.login_error_password_confirm), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!checkbox_accept_terms_and_conditions.isChecked()) {
            Toast.makeText(this, "You Must First Read and Accept our Terms And Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void configureTexts() {

        registerButton.setText(getString(R.string.login_signup_title));


    }

    private void initComponents() {

        loginButton = findViewById(R.id.tv_sung_up_sing_up_prompt);
        registerButton = findViewById(R.id.btn_sing_up);

        fullnameField=findViewById(R.id.et_fullname);
        emailField=findViewById(R.id.et_email);

        passwordField=findViewById(R.id.et_password);
        confirmPasswordField=findViewById(R.id.et_repeat_password);

        phoneTextField=findViewById(R.id.et_phone);


        checkbox_accept_terms_and_conditions=findViewById(R.id.checkbox_accept_terms_and_conditions);
        readTermsAndConditions=findViewById(R.id.read_terms_and_conditions);

    }
}