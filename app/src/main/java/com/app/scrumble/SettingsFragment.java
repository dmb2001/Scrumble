package com.app.scrumble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.user.User;

public class SettingsFragment extends BaseFragment{

    private static final String KEY_USER_ID = "KEY_USER_ID";

    private EditText passwordInput;
    private EditText emailInput;
    private EditText nameInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_settings, container, false);
        OnClickListener clickListener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setCurrentUser(null);
            }
        };
        parentLayout.findViewById(R.id.button_log_out).setOnClickListener(clickListener);
        parentLayout.findViewById(R.id.button_delete_account).setOnClickListener(clickListener);

        passwordInput = parentLayout.findViewById(R.id.input_password);
        emailInput = parentLayout.findViewById(R.id.input_email);
        nameInput = parentLayout.findViewById(R.id.input_name);
        return parentLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe()){
                            User user = getUserDAO().queryUserByID(getCurrentUser().getId());
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                passwordInput.setText(user.getPassword());
                                                emailInput.setText(user.getEmail());
                                                nameInput.setText(user.getName());
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    @Override
    public String name() {
        return null;
    }

}
