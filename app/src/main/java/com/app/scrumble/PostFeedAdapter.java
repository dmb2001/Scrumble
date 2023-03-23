package com.app.scrumble;

import android.net.Uri;
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
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.app.scrumble.PostFeedAdapter.FeedItemViewHolder;
import com.app.scrumble.model.group.scrapbook.Entry;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class PostFeedAdapter extends Adapter<FeedItemViewHolder> {

    private final MainActivity mainActivity;

    private final List<Scrapbook> recentScrapbooks;

    public PostFeedAdapter(List<Scrapbook> scrapbooks, MainActivity mainActivity){
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

        int randomDistance = new Random().nextInt(800 - 600) + 600;
            holder.distance.setText(randomDistance + " metres away");

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

        String profilePicURL = URLStringBuilder.buildProfilePictureLocation(scrapbook.getOwner().getId());
        Glide
                .with(mainActivity)
                .load(Uri.parse(profilePicURL))
                .centerCrop()
                .circleCrop()
                .fallback(R.drawable.ic_blank_profile_pic_grey_background)
                .error(R.drawable.ic_blank_profile_pic_grey_background)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(holder.profilePicture);

        if(scrapbook.getEntryCount() >= 2){
            String largeImageURL = URLStringBuilder.buildMemoryLocation(scrapbook.getOwner().getId(), scrapbook.getID(), scrapbook.getEntries().get(0).getID());
            String smallImageURL = URLStringBuilder.buildMemoryLocation(scrapbook.getOwner().getId(), scrapbook.getID(), scrapbook.getEntries().get(1).getID());

            Glide
                    .with(mainActivity)
                    .load(Uri.parse(largeImageURL))
                    .centerCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(holder.preview_large);

            Glide
                    .with(mainActivity)
                    .load(Uri.parse(smallImageURL))
                    .centerCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(holder.preview_small);
        }
    }

    @Override
    public int getItemCount() {
        int count = recentScrapbooks == null ? 0 : recentScrapbooks.size();
        Log.d("DEBUGGING", "in PostFeedAdapter, coutning: " + count + " scrapbooks to display");
        return count;
    }

    public static final class FeedItemViewHolder extends ViewHolder {

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
