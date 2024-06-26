package com.app.scrumble;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.app.scrumble.model.group.scrapbook.Comment;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends BaseFragment{

    public static final String NAME = "COMMENTS";
    private static final String KEY_SCRAPBOOK_ID = "KEY_SCRAPBOOK_ID";
    private static final String KEY_REPLY_ID = "KEY_REPLY_ID";

    private Scrapbook scrapbook;
    private List<Comment> orderedComments;

    private RecyclerView commentsList;
    private LinearLayoutManager layoutManager;
    private CommentsListAdapter adapter;
    private long replyID;

    private TextView noCommentsLabel;

    public static CommentsFragment newInstance(long scrapbookID) {
        Bundle args = new Bundle();
        args.putLong(KEY_SCRAPBOOK_ID, scrapbookID);
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CommentsFragment newReplyInstance(long scrapbookID, long replyID) {
        CommentsFragment fragment = CommentsFragment.newInstance(scrapbookID);
        Bundle args = fragment.getArguments();
        args.putLong(KEY_REPLY_ID, replyID);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_comments, container, false);
        parentLayout.findViewById(R.id.button_post_top_level_comment).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(AddCommentFragment.newInstance(null, getArguments().getLong(KEY_SCRAPBOOK_ID)), true);
                    }
                }
        );

        commentsList = parentLayout.findViewById(R.id.comment_list);
        layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        commentsList.setLayoutManager(layoutManager);
        commentsList.setAdapter((adapter == null ? (adapter = new CommentsListAdapter()) : adapter));

        noCommentsLabel = parentLayout.findViewById(R.id.label_no_comments);

        return parentLayout;
    }

    private void onCommentsLoaded(){
        Log.d("DEBUGGING", "Comments loaded!");
        if(orderedComments == null){
            Log.d("DEBUGGING", "Comments were null!");
        }else if(orderedComments.size() == 0){
            Log.d("DEBUGGING", "Comments were empty!");
        }else{
            Log.d("DEBUGGING", "There are: " + orderedComments.size() + " comments");
            noCommentsLabel.setVisibility(View.INVISIBLE);
            commentsList.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        hideNavigationBar();
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe()){
                            long replyID = getArguments().getLong(KEY_REPLY_ID);
                            Log.d("DEBUGGING", "Reply ID: " + replyID);
                            Scrapbook scrapbook = getScrapBookDAO().queryScrapbookByID(getArguments().getLong(KEY_SCRAPBOOK_ID));

                            if (replyID != 0) {
                                Comment replyComment = null;
                                for (Comment comment : scrapbook.getComments()) {
                                    replyComment = comment.findCommentById(replyID);
                                    if (replyComment != null) {
                                        break;
                                    }
                                }
                                if (replyComment != null) {
                                    scrapbook = new Scrapbook.ScrapBookBuilder()
                                            .withID(scrapbook.getID())
                                            .withOwner(scrapbook.getOwner())
                                            .withLocation(scrapbook.getLocation())
                                            .withComments(replyComment.toList(0))
                                            .build();
                                }
                            }

                            Scrapbook finalScrapbook = scrapbook;
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                CommentsFragment.this.scrapbook = finalScrapbook;
                                                CommentsFragment.this.replyID = replyID;

                                                Log.d("DEBUGGING", "Original scrapbook had: " + finalScrapbook.getCommentCount());
                                                List<Comment> orderedComments = new ArrayList<>();
                                                if(finalScrapbook.getCommentCount() > 0){
                                                    for(Comment comment : finalScrapbook.getComments()){
                                                        orderedComments.addAll(comment.toList(3));
                                                    }
                                                }
                                                CommentsFragment.this.orderedComments = orderedComments;
                                                onCommentsLoaded();
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
        super.onResume();
    }

    @Override
    public String name() {
        return NAME;
    }

    private class CommentsListAdapter extends Adapter<CommentViewHolder>{

        private static final int DEPTH_0 = 0;
        private static final int DEPTH_1 = 1;
        private static final int DEPTH_2 = 2;
        private static final int DEPTH_3 = 3;

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType == DEPTH_0){
                return new CommentViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.comment_depth_0,
                                parent, false));
            }else if(viewType == DEPTH_1){
                return new CommentViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.comment_depth_1,
                                parent, false));
            }else if(viewType == DEPTH_2){
                return new CommentViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.comment_depth_2,
                                parent, false));
            }else if(viewType == DEPTH_3){
                return new CommentViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.comment_depth_3,
                        parent, false));
            }else{
                throw new IllegalStateException();
            }
        }

        @Override
        public int getItemViewType(int position) {
            int depth = -1;
            for(Comment comment : scrapbook.getComments()){
                depth = comment.getDepth(orderedComments.get(position));
                if(depth != -1){
                    break;
                }
            }
            if(depth == -1){
                throw new IllegalArgumentException();
            }
            return depth;
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = orderedComments.get(position);
            holder.comment = comment;
            holder.authorField.setText("@" + comment.getAuthor());
            holder.contentField.setText(comment.getContent());
            if (comment.getChildren() == null || comment.getChildren().size() == 0) {
                holder.hideShowChildren();
            }
            String profilePicURL = URLStringBuilder.buildProfilePictureLocation(comment.getAuthor().getId());
            Glide
                    .with(getContext())
                    .load(Uri.parse(profilePicURL))
                    .centerCrop()
                    .circleCrop()
                    .fallback(R.drawable.ic_blank_profile_pic_grey_background)
                    .error(R.drawable.ic_blank_profile_pic_grey_background)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(holder.profilePicture);
        }


        @Override
        public int getItemCount() {
            return scrapbook == null ? 0 : orderedComments.size();
        }

    }

    private class CommentViewHolder extends ViewHolder{

        private Comment comment;
        private final ImageView profilePicture;
        private final TextView authorField;
        private final TextView contentField;
        private final TextView replyButton;
        private final TextView childrenButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            profilePicture.setImageResource(R.drawable.image_user_pp_3);
            authorField = itemView.findViewById(R.id.user_name);
            contentField = itemView.findViewById(R.id.comment_body);
            replyButton = itemView.findViewById(R.id.button_reply);
            replyButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showAsMainContent(AddCommentFragment.newInstance(comment.getID(), getArguments().getLong(KEY_SCRAPBOOK_ID)), true);
                        }
                    }
            );
            childrenButton = itemView.findViewById(R.id.button_children);
            if (childrenButton != null) childrenButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showAsMainContent(CommentsFragment.newReplyInstance(getArguments().getLong(KEY_SCRAPBOOK_ID), comment.getID()), true);
                        }
                    }
            );

        }

        private void hideShowChildren() {
            if (childrenButton != null) {
                 childrenButton.setVisibility(View.GONE);
            }
        }

    }
}
