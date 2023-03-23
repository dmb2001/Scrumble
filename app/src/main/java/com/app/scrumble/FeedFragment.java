package com.app.scrumble;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.user.User;

import java.util.List;

public class FeedFragment extends BaseFragment{

    public static final String NAME_FEED = "FEED";

    private RecyclerView feed;

    public static FeedFragment newInstance() {
        Bundle args = new Bundle();
        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_feed, container, false);
        feed = parentLayout.findViewById(R.id.feed_item_list);
        feed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        feed.setVisibility(View.INVISIBLE);
        return parentLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        showAsNavigationContent(MainNavigation.newInstance());
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe()){
                            List<User> following = getUserDAO().getFollowing(getCurrentUser());
                            Log.d("DEBUGGING", "The user is following: " + following.size() + " users");
                            List<Scrapbook> followingScrapbooks = getScrapBookDAO().getRecentScrapbooksFor(following, 3);
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                Log.d("DEBUGGING", "ready to show feed items");

                                                feed.setAdapter(new PostFeedAdapter(followingScrapbooks, (MainActivity) getActivity()));
                                                feed.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    @Override
    public String name() {
        return NAME_FEED;
    }

}
