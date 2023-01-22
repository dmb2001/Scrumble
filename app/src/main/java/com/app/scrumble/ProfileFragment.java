package com.app.scrumble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;

import com.app.scrumble.model.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

public class ProfileFragment extends BaseFragment{

    private static final String KEY_USERNAME = "KEY_USERNAME";

    private ImageView profilePicture;

    private EditText emailInput;
    private EditText nameInput;
    private EditText usernameInput;
    private EditText passwordInput;

    public static ProfileFragment newInstance(String username) {
        Bundle args = new Bundle();
        args.putString(KEY_USERNAME, username);
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

        User user = getUserByUsername(getArguments().getString(KEY_USERNAME));
        if(getCurrentUser() != null && getCurrentUser().getUsername().equals(user.getUsername())){
//            we are looking at the logged in user's profile
            emailInput.setVisibility(View.VISIBLE);
            nameInput.setVisibility(View.VISIBLE);
            passwordInput.setVisibility(View.VISIBLE);

            emailInput.setText(getCurrentUser().getEmail());
            nameInput.setText(getCurrentUser().getName());
            passwordInput.setText(getCurrentUser().getPassword());

            OnClickListener listener = new OnClickListener() {
                @Override
                public void onClick(View view) {
                    popEntireBackStack();
                    showAsMainContent(
                            LoginFragment.newInstance(), false
                    );
                }
            };

            parentLayout.findViewById(R.id.button_log_out).setOnClickListener(listener);
            parentLayout.findViewById(R.id.button_delete_account).setOnClickListener(listener);
        }else{
            usernameInput.setFocusable(false);
            parentLayout.findViewById(R.id.account_controls).setVisibility(View.INVISIBLE);
        }

        usernameInput.setText(user.getUsername());
        Glide
                .with(getContext())
                .load(user.getProfilePictureResourceID())
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(profilePicture);
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
