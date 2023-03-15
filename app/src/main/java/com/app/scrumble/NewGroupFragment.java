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
import com.app.scrumble.model.group.scrapbook.Location;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewGroupFragment extends BaseFragment{
    @Override
    public String name() {
        return null;
    }

    private EditText groupNameField;
    private Button confirmButton;

    private final long uniqueID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

    private boolean inputHasBeenProvidedTo(EditText input){
        return input.getText() != null && input.getText().toString().trim().length() != 0;
    }
    public static NewGroupFragment newInstance() {
        NewGroupFragment fragment = new NewGroupFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_new_group, container, false);
        confirmButton = parentLayout.findViewById(R.id.button_confirm_group);

        groupNameField = parentLayout.findViewById(R.id.input_group_name);

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
                                                Log.d("DEBUGGING","Getting Group name...");
                                                String groupName = groupNameField.getText().toString();
                                                Log.d("DEBUGGING","Got Group name!");

                                                //Create an arrayList of members, adding the creator to it
                                                List<User> groupMembers = new ArrayList<User>();
                                                groupMembers.add(getCurrentUser());

                                                Group newGroup = new Group(uniqueID,groupName,groupMembers);

                                                getGroupDAO().createGroup(newGroup);
                                                Log.d("DEBUGGING", "The user created a group named: " + groupName);
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
