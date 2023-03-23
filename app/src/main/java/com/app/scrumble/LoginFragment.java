package com.app.scrumble;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.user.User;

import java.util.Objects;

public class LoginFragment extends BaseFragment{

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private EditText usernameField;
    private EditText passwordField;

    private Button loginButton;
    private Button registerButton;

    public static LoginFragment newInstance() {
        Bundle args = new Bundle();
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_login, container, false);
        usernameField = parentLayout.findViewById(R.id.username_input);
        passwordField = parentLayout.findViewById(R.id.password_input);

        loginButton = parentLayout.findViewById(R.id.button_submit);
        loginButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("DEBUGGING", "Login clicked");
                        hideKeyBoard(view);
                        if(usernameField.getText().toString() == null || usernameField.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "please enter your username", Toast.LENGTH_SHORT).show();
                        }else if(passwordField.getText().toString() == null || passwordField.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "please enter your password", Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("DEBUGGING", "relevant fields provided");
                            runInBackground(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                User registeredUser = getUserDAO().queryUserByUsername(usernameField.getText().toString().trim());
                                                boolean incorrectInfo = false;
                                                if(registeredUser == null || !Objects.equals(passwordField.getText().toString(), registeredUser.getPassword())){
                                                    incorrectInfo = true;
                                                }
                                                boolean finalIncorrectInfo = incorrectInfo;
                                                runOnUIThread(
                                                        new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if(isSafe()){
                                                                    if(finalIncorrectInfo){
                                                                        Toast.makeText(getContext(), "The username or password you entered were incorrect!", Toast.LENGTH_SHORT).show();
                                                                    }else{
                                                                        setCurrentUser(registeredUser);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                );
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
        registerButton = parentLayout.findViewById(R.id.button_nav_create_account);
        registerButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(
                                RegisterFragment.newInstance(), true);
                    }
                }
        );
        return parentLayout;
    }

    @Override
    public void onResume() {
        hideNavigationBar();
        super.onResume();
    }

    @Override
    public String name() {
        return null;
    }

}
