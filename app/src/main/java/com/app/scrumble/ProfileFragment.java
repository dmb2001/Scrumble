package com.app.scrumble;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.user.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends BaseFragment{

    private static final String KEY_USER_ID = "KEY_USER_ID";

    private RecyclerView recentPostsList;

    private ImageView profilePicture;
    private TextView usernameLabel;
    private TextView recentPostsLabel;

    private Button followButton;

    public static ProfileFragment newInstance(long userID) {
        Bundle args = new Bundle();
        args.putLong(KEY_USER_ID, userID);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameLabel = parentLayout.findViewById(R.id.label_username);
        recentPostsLabel = parentLayout.findViewById(R.id.label_recent_posts);
        profilePicture = parentLayout.findViewById(R.id.profile_picture_blank);

        followButton = parentLayout.findViewById(R.id.button_follow);

        recentPostsList = parentLayout.findViewById(R.id.user_post_list);
        recentPostsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recentPostsList.setVisibility(View.INVISIBLE);

        return parentLayout;
    }


    @Override
    public void onResume() {
        hideNavigationBar();
        super.onResume();
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe()){
                            final User user = getUserDAO().queryUserByID(getArguments().getLong(KEY_USER_ID));
                            List<User> singleUserList = new ArrayList<>();
                            singleUserList.add(user);
                            List<Scrapbook> recentPosts = getScrapBookDAO().getRecentScrapbooksFor(singleUserList, 25);
                            boolean following = false;
                            if(getCurrentUser().getId() != user.getId()){
                                following = getUserDAO().checkIfFollowing(user.getId(), getCurrentUser().getId());
                            }
                            boolean finalFollowing = following;
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                onDataQueried(user, recentPosts, finalFollowing);
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    private void onDataQueried(User user, List<Scrapbook> recentPosts, boolean following){
        Log.d("DEBUGGING", "There were " + recentPosts.size() + " recent posts for user: @" + user.getUsername());
        String profilePicURL = URLStringBuilder.buildProfilePictureLocation(user.getId());
        Glide
                .with(getContext())
                .load(Uri.parse(profilePicURL))
                .centerCrop()
                .circleCrop()
                .fallback(R.color.cardview_dark_background)
                .fallback(R.drawable.ic_blank_profile_pic_grey_background)
                .error(R.drawable.ic_blank_profile_pic_grey_background)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(profilePicture);

        if(getCurrentUser().getId() != user.getId()){
            if(following){
                followButton.setText("unfollow");
            }else {
                followButton.setText("follow");
            }
            followButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!following){
                                followButton.setText("unfollow");
                                runInBackground(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                getUserDAO().follow(user.getId(), getCurrentUser().getId());
                                            }
                                        }
                                );
                            }
                        }
                    }
            );
            followButton.setVisibility(View.VISIBLE);
        }
        usernameLabel.setText("@" + user.getUsername());
//            Glide
//                    .with(getContext())
//                    .load(R.drawable.image_user_pp_3)
//                    .centerCrop()
//                    .format(DecodeFormat.PREFER_RGB_565)
//                    .into(profilePicture);

        if(recentPosts.isEmpty()){
            recentPostsLabel.setText("no posts yet...");
        }else {
            recentPostsLabel.setText("recent posts:");
            recentPostsList.setAdapter(new PostFeedAdapter(recentPosts, (MainActivity) getActivity()));
            recentPostsList.getAdapter().notifyDataSetChanged();
            recentPostsList.setVisibility(View.VISIBLE);
        }
        recentPostsLabel.setVisibility(View.VISIBLE);

    }

    @Override
    public String name() {
        return null;
    }


}
