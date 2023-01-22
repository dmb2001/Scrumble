package com.app.scrumble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.Comment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

public class CommentsFragment extends BaseFragment{

    public static final String NAME = "COMMENTS";

    public static CommentsFragment newInstance() {
        Bundle args = new Bundle();
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_comments, container, false);
        for(Comment comment : getComments()){
            populateComment(parentLayout, comment);
        }
        parentLayout.findViewById(R.id.button_post_top_level_comment).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(AddCommentFragment.newInstance(null), true);
                    }
                }
        );
        return parentLayout;
    }

    private void populateComment(View root, Comment comment){

        ImageView profilePicture = null;
        TextView username = null;
        TextView commentBody = null;

        OnClickListener flagButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "You have reported " + "@" + comment.getAuthor().getUsername() + "'s comment", Toast.LENGTH_SHORT).show();
            }
        };

        OnClickListener replyButtonListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                showAsMainContent(AddCommentFragment.newInstance(comment.getResourceID()), true);
            }
        };

        if(comment.getResourceID() == R.string.comment_1){
            profilePicture = root.findViewById(R.id.comment_1).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_1).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_1).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_1).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_1).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }else if(comment.getResourceID() == R.string.comment_2){
            profilePicture = root.findViewById(R.id.comment_2).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_2).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_2).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_2).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_2).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }else if(comment.getResourceID() == R.string.comment_3){
            profilePicture = root.findViewById(R.id.comment_3).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_3).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_3).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_3).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_3).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }else if(comment.getResourceID() == R.string.comment_4){
            profilePicture = root.findViewById(R.id.comment_4).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_4).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_4).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_4).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_4).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }else if(comment.getResourceID() == R.string.comment_5){
            profilePicture = root.findViewById(R.id.comment_5).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_5).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_5).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_5).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_5).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }else if(comment.getResourceID() == R.string.comment_6){
            profilePicture = root.findViewById(R.id.comment_6).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_6).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_6).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_6).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_6).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }else if(comment.getResourceID() == R.string.comment_7){
            profilePicture = root.findViewById(R.id.comment_7).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_7).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_7).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_7).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_7).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }else if(comment.getResourceID() == R.string.comment_8){
            profilePicture = root.findViewById(R.id.comment_8).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_8).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_8).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_8).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_8).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }else if(comment.getResourceID() == R.string.comment_9){
            profilePicture = root.findViewById(R.id.comment_9).findViewById(R.id.profile_picture);
            username = root.findViewById(R.id.comment_9).findViewById(R.id.user_name);
            commentBody = root.findViewById(R.id.comment_9).findViewById(R.id.comment_body);
            root.findViewById(R.id.comment_9).findViewById(R.id.button_flag).setOnClickListener(flagButtonClickListener);
            root.findViewById(R.id.comment_9).findViewById(R.id.button_reply).setOnClickListener(replyButtonListener);
        }

        Glide
                .with(getContext())
                .load(comment.getAuthor().getProfilePictureResourceID())
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(profilePicture);

        profilePicture.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(
                                ProfileFragment.newInstance(comment.getAuthor().getUsername()), true
                        );
                    }
                });
        username.setText("@" + comment.getAuthor().getUsername());
        commentBody.setText(comment.getResourceID());

    }

    @Override
    public void onResume() {
        hideNavigationBar();
        super.onResume();
    }

    @Override
    public String name() {
        return NAME;
    }
}
