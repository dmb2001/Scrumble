package com.app.scrumble;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.scrumble.model.group.Group;
import com.app.scrumble.model.group.scrapbook.Entry;
import com.app.scrumble.model.group.scrapbook.Scrapbook;

import java.util.ArrayList;
import java.util.List;

public class NewGroupFragment extends BaseFragment{
    @Override
    public String name() {
        return null;
    }

    private EditText groupNameField;
    private Button confirmButton;

    private boolean inputHasBeenProvidedTo(EditText input){
        return input.getText() != null && input.getText().toString().trim().length() != 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_new_submission, container, false);
        confirmButton = parentLayout.findViewById(R.id.button_submit);
        confirmButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!inputHasBeenProvidedTo(groupNameField)){
                            Toast.makeText(getContext(), "You must enter a name for your group", Toast.LENGTH_LONG).show();
                        }else{
                            runInBackground(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                String groupName = groupNameField.getText().toString();
                                                Log.d("DEBUGGING", "The user created a group named: " + groupName);
                                                Group group = new Group(groupName);

                                                getGroupDAO().createGroup(group);
                                                runOnUIThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if(isSafe()){
                                                            Toast.makeText(getContext(), "Your group has been created!", Toast.LENGTH_LONG).show();
                                                            popBackStack();
                                                        }
                                                    }
                                                });
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
}
