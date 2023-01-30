package com.app.scrumble;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class PermissionRequestFragment extends BaseFragment {

    private static final String NAME = "PERMISSION_REQUEST";

    private final ActivityResultLauncher<String[]> launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            //check if both permissions have been granted. If user is logged in -> show map screen. if no user logged in -> show login screen
            for(Boolean granted : result.values()){
                if(!granted){
                    return;
                }
            }
            if(getCurrentUser() == null){
                showAsMainContent(LoginFragment.newInstance(), false);
            }else{
                showAsMainContent(MapFragment.newInstance(), false);
            }
        }
    });

    public static PermissionRequestFragment newInstance(){
        Bundle args = new Bundle();
        PermissionRequestFragment fragment = new PermissionRequestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_permissions, container, false);
        Button grantPermissionButton = parentLayout.findViewById(R.id.button_grant);
        grantPermissionButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launcher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION});
                    }
                }
        );
        return parentLayout;
    }

    @Override
    public String name() {
        return NAME;
    }

}
