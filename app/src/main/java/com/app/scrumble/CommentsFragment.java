package com.app.scrumble;

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
                            Scrapbook scrapbook = getScrapBookDAO().queryScrapbookByID(getArguments().getLong(KEY_SCRAPBOOK_ID));
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                CommentsFragment.this.scrapbook = scrapbook;
                                                Log.d("DEBUGGING", "Original scrapbook had: " + scrapbook.getCommentCount());
                                                List<Comment> orderedComments = new ArrayList<>();
                                                if(scrapbook.getCommentCount() > 0){
                                                    for(Comment comment : scrapbook.getComments()){
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
        }

    }
}
