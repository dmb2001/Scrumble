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
import androidx.appcompat.widget.SwitchCompat;

import com.app.scrumble.model.user.User;

import java.util.UUID;

public class RegisterFragment extends BaseFragment{

    private final long newUserID =  UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

    public static RegisterFragment newInstance() {
        Bundle args = new Bundle();
        RegisterFragment fragment = new RegisterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_account_creation, container, false);

        EditText nameInput = parentLayout.findViewById(R.id.input_name);
        EditText emailInput = parentLayout.findViewById(R.id.input_email);
        EditText usernameInput = parentLayout.findViewById(R.id.input_username);
        EditText passwordInput = parentLayout.findViewById(R.id.input_password);
        EditText passwordConfirmationInput = parentLayout.findViewById(R.id.input_password);

        SwitchCompat agreementSwitch = parentLayout.findViewById(R.id.toggle_agreement_read);

        Button submitButton = parentLayout.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(nameInput.getText() == null || nameInput.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "please enter your name before tapping \"register\"", Toast.LENGTH_SHORT).show();
                        }else if(usernameInput.getText() == null || usernameInput.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "please enter your chosen username before tapping \"register\"", Toast.LENGTH_SHORT).show();
                        }else if(emailInput.getText() == null || emailInput.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "please enter your email address before tapping \"register\"", Toast.LENGTH_SHORT).show();
                        }else if(passwordInput.getText() == null || passwordInput.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "please enter your chosen password before tapping \"register\"", Toast.LENGTH_SHORT).show();
                        }else if(passwordConfirmationInput.getText() == null || passwordConfirmationInput.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "please confirm your chosen password before tapping \"register\"", Toast.LENGTH_SHORT).show();
                        }else if(!passwordInput.getText().toString().trim().equals(passwordConfirmationInput.getText().toString().trim())){
                            Toast.makeText(getContext(), "please confirm that both your password and password confirmation match before tapping \"register\"", Toast.LENGTH_SHORT).show();
                        }else if(!agreementSwitch.isChecked()){
                            Toast.makeText(getContext(), "you must agree to the Scrumble end user agreement", Toast.LENGTH_SHORT).show();
                        }else{
                            User newUser = new User(nameInput.getText().toString().trim(), emailInput.getText().toString().trim(), passwordInput.getText().toString().trim(), usernameInput.getText().toString().trim(), newUserID, User.TYPE_USER);
                            runInBackground(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isSafe()) {
                                                getUserDAO().create(newUser);
                                                runOnUIThread(
                                                        new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if(isSafe()){
                                                                    hideKeyBoard(view);
                                                                    setCurrentUser(newUser);
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

        return parentLayout;
    }

    @Override
    public String name() {
        return null;
    }

}
