package com.app.scrumble;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.Comment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

public class AddCommentFragment extends BaseFragment{

    public static final String KEY_COMMENT_RESOURCE_ID = "KEY_COMMENT_ID";

    public static AddCommentFragment newInstance(Integer parentCommentID) {
        Bundle args = new Bundle();
        if(parentCommentID != null){
            args.putInt(KEY_COMMENT_RESOURCE_ID, parentCommentID);
        }
        AddCommentFragment fragment = new AddCommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_submit_comment, container, false);
        parentLayout.findViewById(R.id.parent_comment).findViewById(R.id.button_reply).setVisibility(View.INVISIBLE);
        parentLayout.findViewById(R.id.parent_comment).findViewById(R.id.button_flag).setVisibility(View.INVISIBLE);
        EditText replyBody = parentLayout.findViewById(R.id.input_comment);
        if(getArguments().containsKey(KEY_COMMENT_RESOURCE_ID)){
            Comment parentComment = getCommentByResourceID(getArguments().getInt(KEY_COMMENT_RESOURCE_ID));
            ImageView profilePicture =  parentLayout.findViewById(R.id.parent_comment).findViewById(R.id.profile_picture);
            Glide
                    .with(getContext())
                    .load(parentComment.getAuthor().getProfilePictureResourceID())
                    .centerCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(profilePicture);

            TextView commentAuthor = parentLayout.findViewById(R.id.parent_comment).findViewById(R.id.user_name);
            commentAuthor.setText("@" + parentComment.getAuthor());

            replyBody.setHint("Enter your reply to @" + parentComment.getAuthor().getUsername() + "'s comment");

            TextView commentBody = parentLayout.findViewById(R.id.parent_comment).findViewById(R.id.comment_body);
            commentBody.setText(parentComment.getResourceID());
        }else{
            parentLayout.findViewById(R.id.parent_comment).setVisibility(View.GONE);
        }

        Button submitButton = parentLayout.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(replyBody.getText() == null || replyBody.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "Your comment cannot be empty!", Toast.LENGTH_SHORT).show();
                        }else{
                            hideKeyBoard(view);
                            Toast.makeText(getContext(), "Your comment was successfully submitted!", Toast.LENGTH_SHORT).show();
                            popBackStack();
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
