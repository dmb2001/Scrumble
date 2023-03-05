package com.app.scrumble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class FeedFragment extends BaseFragment{

    public static final String NAME_FEED = "FEED";

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
//        RecyclerView feed = parentLayout.findViewById(R.id.feed_item_list);
//        feed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//        feed.setAdapter(new FeedAdapter(getScrapbooks(), getCurrentLocation(), (MainActivity) getActivity()));
        return parentLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        showAsNavigationContent(MainNavigation.newInstance());
    }

    @Override
    public String name() {
        return NAME_FEED;
    }

//    public final class FeedAdapter extends Adapter<FeedItemViewHolder>{
//
//        private final MainActivity mainActivity;
//
//        private final Location currentLocation;
//        private final List<Scrapbook> recentScrapbooks;
//
//        private FeedAdapter(List<Scrapbook> scrapbooks, Location currentLocation, MainActivity mainActivity){
//            this.recentScrapbooks = scrapbooks;
//            this.currentLocation = currentLocation;
//            this.mainActivity = mainActivity;
//        }
//
//        @NonNull
//        @Override
//        public FeedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            return new FeedItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false));
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull FeedItemViewHolder holder, int position) {
//
//            Scrapbook scrapbook = recentScrapbooks.get(position);
//            Glide
//                    .with(getContext())
//                    .load(scrapbook.getOwner().getProfilePictureResourceID())
//                    .centerCrop()
//                    .format(DecodeFormat.PREFER_RGB_565)
//                    .into(holder.profilePicture);
//
//            Glide
//                    .with(getContext())
//                    .load(scrapbook.getEntries().get(0).getImageResource())
//                    .centerCrop()
//                    .format(DecodeFormat.PREFER_RGB_565)
//                    .into(holder.preview_large);
//
//            Glide
//                    .with(getContext())
//                    .load(scrapbook.getEntries().get(1).getImageResource())
//                    .centerCrop()
//                    .format(DecodeFormat.PREFER_RGB_565)
//                    .into(holder.preview_small);
//
//            holder.distance.setText(scrapbook.getLocation().distanceFrom(currentLocation) + " miles away");
//
//            holder.username.setText("@" + scrapbook.getOwner().getUsername());
//            holder.postDate.setText(DateUtils.getRelativeTimeSpanString(scrapbook.getLastUpdate(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
//
//            holder.viewMoreOption.setText("+ " + (recentScrapbooks.get(position).getEntries().size() - 2) + " more");
//            holder.viewMoreOption.setOnClickListener(
//                    new OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            mainActivity.showAsMainContent(ScrapBookFragment.newInstance(recentScrapbooks.get(position).getID()), true);
//                        }
//                    }
//            );
//
//            holder.commentsButton.setOnClickListener(
//                    new OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            mainActivity.showAsMainContent(CommentsFragment.newInstance(), true);
//                        }
//                    }
//            );
//            holder.likeButton.setOnClickListener(
//                    new OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Toast.makeText(mainActivity.getApplicationContext(), "You have liked " + recentScrapbooks.get(position).getOwner().getUsername() + "'s scrapbook", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//            );
//
//            holder.reactionButton.setOnClickListener(
//                    new OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Toast.makeText(mainActivity.getApplicationContext(), "You have reacted to " + recentScrapbooks.get(position).getOwner().getUsername() + "'s scrapbook", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//            );
//        }
//
//        @Override
//        public int getItemCount() {
//            return recentScrapbooks == null ? 0 : recentScrapbooks.size();
//        }
//
//    }

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
