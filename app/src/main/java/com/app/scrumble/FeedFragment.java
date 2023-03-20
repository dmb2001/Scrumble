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

import com.app.scrumble.model.group.scrapbook.Location;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.user.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

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
                            List<Scrapbook> followingScrapbooks = getScrapBookDAO().getRecentScrapbooksFor(following, 3);
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                Log.d("DEBUGGING", "ready to show feed items");
                                                feed.setAdapter(new FeedAdapter(followingScrapbooks, (MainActivity) getActivity()));
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

    public static final class FeedAdapter extends Adapter<FeedItemViewHolder> {

        private final MainActivity mainActivity;

        private final List<Scrapbook> recentScrapbooks;

        private FeedAdapter(List<Scrapbook> scrapbooks, MainActivity mainActivity){
            this.recentScrapbooks = scrapbooks;
            this.mainActivity = mainActivity;
        }

        @NonNull
        @Override
        public FeedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FeedItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FeedItemViewHolder holder, int position) {

            int currentPosition = holder.getAdapterPosition();
            Scrapbook scrapbook = recentScrapbooks.get(currentPosition);

//            holder.distance.setText(scrapbook.getLocation().distanceFrom(currentLocation) + " miles away");

            holder.username.setText("@" + scrapbook.getOwner().getUsername());
            holder.postDate.setText(DateUtils.getRelativeTimeSpanString(scrapbook.getLastUpdate(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));

            int furtherEntryCount = Math.max(0, recentScrapbooks.get(currentPosition).getEntries().size() - 2);
            holder.viewMoreOption.setText("+ " + furtherEntryCount + " more");
            holder.viewMoreOption.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mainActivity.showAsMainContent(ScrapBookFragment.newInstance(recentScrapbooks.get(currentPosition).getID()), true);
                        }
                    }
            );

            holder.commentsButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mainActivity.showAsMainContent(CommentsFragment.newInstance(recentScrapbooks.get(currentPosition).getID()), true);
                        }
                    }
            );
            holder.likeButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(mainActivity.getApplicationContext(), "You have liked " + recentScrapbooks.get(currentPosition).getOwner().getUsername() + "'s scrapbook", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            holder.reactionButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(mainActivity.getApplicationContext(), "You have reacted to " + recentScrapbooks.get(currentPosition).getOwner().getUsername() + "'s scrapbook", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return recentScrapbooks == null ? 0 : recentScrapbooks.size();
        }

    }

    public static final class FeedItemViewHolder extends ViewHolder{

        private final ImageView profilePicture;

        private final ImageView preview_large;
        private final ImageView preview_small;

        private final TextView distance;

        private final TextView username;
        private final TextView postDate;

        private final TextView viewMoreOption;

        private final ImageButton commentsButton;
        private final ImageButton likeButton;
        private final ImageButton reactionButton;

        public FeedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.profilePicture = itemView.findViewById(R.id.profile_picture);

            this.preview_large = itemView.findViewById(R.id.image_preview_large);
            this.preview_small = itemView.findViewById(R.id.image_preview_small);

            this.distance = itemView.findViewById(R.id.distance);

            this.username = itemView.findViewById(R.id.user_name);
            this.postDate = itemView.findViewById(R.id.date);

            this.viewMoreOption = itemView.findViewById(R.id.option_view_more);
            this.commentsButton = itemView.findViewById(R.id.button_comments);

            this.likeButton = itemView.findViewById(R.id.button_like);
            this.reactionButton = itemView.findViewById(R.id.button_reactions);
        }
    }

}
