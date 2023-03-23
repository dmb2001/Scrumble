package com.app.scrumble;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.User.UserBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import java.util.ArrayList;
import java.util.UUID;

public class RegisterFragment extends BaseFragment{

    private final long newUserID =  UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

    private ImageView profilePictureImageView;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri profilePictureSelection;

    public static RegisterFragment newInstance() {
        Bundle args = new Bundle();
        RegisterFragment fragment = new RegisterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(),
                        new ActivityResultCallback<Uri>() {
                            @Override
                            public void onActivityResult(Uri uri) {
                                if (uri != null){
                                    profilePictureSelection = uri;
                                    Glide
                                            .with(getContext())
                                            .load(profilePictureSelection)
                                            .centerCrop()
                                            .circleCrop()
                                            .fallback(R.color.cardview_dark_background)
                                            .format(DecodeFormat.PREFER_RGB_565)
                                            .into(profilePictureImageView);
                                }
                            }
                        });
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

        profilePictureImageView = parentLayout.findViewById(R.id.profile_picture_blank);
        profilePictureImageView.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imagePickerLauncher.launch("image/*");
                    }
                }
        );

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
                            User newUser = new UserBuilder()
                                    .withName(nameInput.getText().toString().trim())
                                    .withEmail(emailInput.getText().toString().trim())
                                    .withPassword(passwordInput.getText().toString().trim())
                                    .withUsername(usernameInput.getText().toString().trim())
                                    .withUserType(User.TYPE_USER)
                                    .build();

                            runInBackground(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isSafe()) {
                                                getUserDAO().create(newUser);
                                                if (profilePictureSelection != null){
                                                    String objectKey = URLStringBuilder.buildProfilePictureKey(newUser.getId());
                                                    getImageUploader().upload(profilePictureSelection, objectKey);
                                                }
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
