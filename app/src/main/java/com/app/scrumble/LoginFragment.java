package com.app.scrumble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.User;

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
                        User dummyUser = new User("J.Doe", "example@example.com", "password", "Scrapbooker01", R.drawable.ic_blank_profile_pic_grey_background);
                        setCurrentUser(dummyUser);
                        hideKeyBoard(view);
                        showAsMainContent(MapFragment.newInstance(), false);
//                        if(usernameField.getText().toString() == null || usernameField.getText().toString().trim().length() == 0){
//                            Toast.makeText(getContext(), "please enter your username", Toast.LENGTH_SHORT).show();
//                        }else if(passwordField.getText().toString() == null || passwordField.getText().toString().trim().length() == 0){
//                            Toast.makeText(getContext(), "please enter your password", Toast.LENGTH_SHORT).show();
//                        }else if(!USERNAME.equalsIgnoreCase(usernameField.getText().toString().trim()) || !PASSWORD.equalsIgnoreCase(passwordField.getText().toString().trim())){
//                            Toast.makeText(getContext(), "The username or password you entered were incorrect!", Toast.LENGTH_SHORT).show();
//                        }else{
//                            showAsMainContent(MapFragment.newInstance(), false);
//                        }
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
