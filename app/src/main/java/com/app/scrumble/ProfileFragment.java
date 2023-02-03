package com.app.scrumble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;

import com.app.scrumble.model.user.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

public class ProfileFragment extends BaseFragment{

    private static final String KEY_USER_ID = "KEY_USER_ID";

    private ImageView profilePicture;

    private EditText emailInput;
    private EditText nameInput;
    private EditText usernameInput;
    private EditText passwordInput;

    private Button logoutButton;
    private Group accountControls;

    public static ProfileFragment newInstance(long userID) {
        Bundle args = new Bundle();
        args.putLong(KEY_USER_ID, userID);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_profile, container, false);

        nameInput = parentLayout.findViewById(R.id.input_name);
        emailInput = parentLayout.findViewById(R.id.input_email);
        passwordInput = parentLayout.findViewById(R.id.input_password);

        usernameInput = parentLayout.findViewById(R.id.input_username);
        profilePicture = parentLayout.findViewById(R.id.profile_picture_blank);

        logoutButton = parentLayout.findViewById(R.id.button_log_out);
        accountControls = parentLayout.findViewById(R.id.account_controls);

        return parentLayout;
    }

    private void onUserQueried(User user){
        if(getCurrentUser() != null && getCurrentUser().getUsername().equals(user.getUsername())){
//            we are looking at the logged in user's profile
            emailInput.setVisibility(View.VISIBLE);
            nameInput.setVisibility(View.VISIBLE);
            passwordInput.setVisibility(View.VISIBLE);

            usernameInput.setText(user.getUsername());
            emailInput.setText(getCurrentUser().getEmail());
            nameInput.setText(getCurrentUser().getName());
            passwordInput.setText(getCurrentUser().getPassword());

            OnClickListener listener = new OnClickListener() {
                @Override
                public void onClick(View view) {
                    setCurrentUser(null);
                }
            };

            logoutButton.setOnClickListener(listener);

            Glide
                    .with(getContext())
                    .load(R.drawable.image_user_pp_3)
                    .centerCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(profilePicture);
        }else{
            usernameInput.setFocusable(false);
            accountControls.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onResume() {
        hideNavigationBar();
        super.onResume();
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe()){
                            final User user = getUserDAO().queryUserByID(getArguments().getLong(KEY_USER_ID));
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                onUserQueried(user);
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
