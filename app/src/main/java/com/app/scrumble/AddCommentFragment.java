package com.app.scrumble;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.group.scrapbook.Comment;

import java.util.List;
import java.util.UUID;

public class AddCommentFragment extends BaseFragment{

    public static final String KEY_PARENT_COMMENT_ID = "KEY_COMMENT_ID";
    public static final String KEY_SCRAPBOOK_ID = "KEY_SCRAPBOOK_ID";

    public static AddCommentFragment newInstance(Long parentCommentID, long scrapbookID) {
        Bundle args = new Bundle();
        if(parentCommentID != null){
            args.putLong(KEY_PARENT_COMMENT_ID, parentCommentID);
        }
        args.putLong(KEY_SCRAPBOOK_ID, scrapbookID);
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
//        if(getArguments().containsKey(KEY_PARENT_COMMENT_ID)){
//            ImageView profilePicture =  parentLayout.findViewById(R.id.parent_comment).findViewById(R.id.profile_picture);
//            Glide
//                    .with(getContext())
//                    .load(parentComment.getAuthor().getProfilePictureResourceID())
//                    .centerCrop()
//                    .format(DecodeFormat.PREFER_RGB_565)
//                    .into(profilePicture);
//
//            TextView commentAuthor = parentLayout.findViewById(R.id.parent_comment).findViewById(R.id.user_name);
//            commentAuthor.setText("@" + parentComment.getAuthor());
//
//            replyBody.setHint("Enter your reply to @" + parentComment.getAuthor().getUsername() + "'s comment");
//
//            TextView commentBody = parentLayout.findViewById(R.id.parent_comment).findViewById(R.id.comment_body);
//            commentBody.setText(parentComment.getResourceID());
//        }else{
//            parentLayout.findViewById(R.id.parent_comment).setVisibility(View.GONE);
//        }

        Button submitButton = parentLayout.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(replyBody.getText() == null || replyBody.getText().toString().trim().length() == 0){
                            Toast.makeText(getContext(), "Your comment cannot be empty!", Toast.LENGTH_SHORT).show();
                        }else{
                            hideKeyBoard(view);
                            runInBackground(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            Long parentID = getArguments().containsKey(KEY_PARENT_COMMENT_ID) ? getArguments().getLong(KEY_PARENT_COMMENT_ID) : null;
                                            getScrapBookDAO().createComment(
                                                    new Comment(UUID.randomUUID().getMostSignificantBits(), System.currentTimeMillis(), replyBody.getText().toString(), getCurrentUser()),
                                                    getArguments().getLong(KEY_SCRAPBOOK_ID),
                                                    parentID);
                                            runOnUIThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    popBackStack();
                                                }
                                            });
                                        }
                                    }
                            );
                        }
                    }
                }
        );

        return parentLayout;
    }

    private void onParentCommentLoaded(Comment parentComment){
        Log.d("DEBUGGING", "parent comment loaded");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getArguments().containsKey(KEY_PARENT_COMMENT_ID)){
            runInBackground(
                    new Runnable() {
                        @Override
                        public void run() {
                            List<Comment> comments = getScrapBookDAO().queryScrapbookByID(getArguments().getLong(KEY_SCRAPBOOK_ID)).getComments();
                            Comment parentComment = null;
                            if(comments != null){
                                for(Comment comment : comments){
                                    parentComment = comment.findCommentById(getArguments().getLong(KEY_PARENT_COMMENT_ID));
                                }
                            }
                            Comment finalParentComment = parentComment;
                            runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    onParentCommentLoaded(finalParentComment);
                                }
                            });
                        }
                    }
            );
        }
    }

    @Override
    public String name() {
        return null;
    }
}
