package com.app.scrumble;

import android.os.Bundle;
import android.text.format.DateUtils;
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

import com.app.scrumble.FeedFragment.FeedItemViewHolder;
import com.app.scrumble.model.Group;
import com.app.scrumble.model.Scrapbook;
import com.app.scrumble.model.User;

public class GroupFragment extends BaseFragment{

    private static final String KEY_NAME = "KEY_NAME";

    public static GroupFragment newInstance(String name) {
        Bundle args = new Bundle();
        args.putString(KEY_NAME, name);
        GroupFragment fragment = new GroupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Group group = getGroupByName(getArguments().getString(KEY_NAME));
        View parentLayout = inflater.inflate(R.layout.fragment_group, container, false);

        ImageView groupProfilePicture = parentLayout.findViewById(R.id.group_profile_picture);
        groupProfilePicture.setImageResource(group.getGroupPhotoResourceID());

        TextView groupTitle = parentLayout.findViewById(R.id.group_name);
        groupTitle.setText(group.getName());

        TextView memberCount = parentLayout.findViewById(R.id.member_count);
        memberCount.setText(group.getMembers().size() + " members");

        TextView membersList = parentLayout.findViewById(R.id.member_list);
        membersList.append("Members:");
        for(User member : group.getMembers()){
            membersList.append(" @" + member.getUsername() + ",");
        }

        RecyclerView list = parentLayout.findViewById(R.id.scrapbook_feed);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        list.setAdapter(new PostAdapter(group));

        return parentLayout;
    }

    private class PostAdapter extends Adapter<GroupFeedItemHolder>{

        private Group group;

        private PostAdapter(Group group){
            this.group = group;
        }

        @NonNull
        @Override
        public GroupFeedItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GroupFeedItemHolder(LayoutInflater.from(getContext()).inflate(R.layout.group_feed_post, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull GroupFeedItemHolder holder, int position) {

            Scrapbook scrapbook = group.getRecentPosts().get(position);
            holder.profilePicture.setImageResource(scrapbook.getOwner().getProfilePictureResourceID());

            holder.preview_large.setImageResource(scrapbook.getEntries().get(0).getImageResource());
            holder.preview_small.setImageResource(scrapbook.getEntries().get(1).getImageResource());

            holder.distance.setText(scrapbook.getLocation().distanceFrom(currentLocation) + " miles away");

            holder.username.setText("@" + scrapbook.getOwner().getUsername());
            holder.postDate.setText(DateUtils.getRelativeTimeSpanString(scrapbook.getLastUpdate(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));

            holder.viewMoreOption.setText("+ " + (group.getRecentPosts().get(position).getEntries().size() - 2) + " more");
            holder.viewMoreOption.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showAsMainContent(ScrapBookFragment.newInstance(group.getRecentPosts().get(position).getID()), true);
                        }
                    }
            );

            holder.commentsButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showAsMainContent(CommentsFragment.newInstance(), true);
                        }
                    }
            );
            holder.likeButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), "You have liked " + group.getRecentPosts().get(position).getOwner().getUsername() + "'s scrapbook", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            holder.reactionButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), "You have reacted to " + group.getRecentPosts().get(position).getOwner().getUsername() + "'s scrapbook", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return group.getRecentPosts() == null ? 0 : group.getRecentPosts().size();
        }

    }

    private static final class GroupFeedItemHolder extends ViewHolder{

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

        public GroupFeedItemHolder(@NonNull View itemView) {
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

    @Override
    public void onResume() {
        hideNavigationBar();
        super.onResume();
    }

    @Override
    public String name() {
        return null;
    }
}
