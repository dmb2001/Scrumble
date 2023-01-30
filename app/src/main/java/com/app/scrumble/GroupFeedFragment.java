package com.app.scrumble;

import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

public class GroupFeedFragment extends BaseFragment{

    public static GroupFeedFragment newInstance() {

        Bundle args = new Bundle();

        GroupFeedFragment fragment = new GroupFeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_group_feed, container, false);
//        RecyclerView list = parentLayout.findViewById(R.id.group_item_list);
//        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//        list.setAdapter(new FeedAdapter());
        return parentLayout;
    }

    @Override
    public void onResume() {
        showAsNavigationContent(
                MainNavigation.newInstance());
        super.onResume();
    }

    @Override
    public String name() {
        return null;
    }

//    private final class FeedAdapter extends Adapter<GroupFeedItemViewHolder>{
//
//        @NonNull
//        @Override
//        public GroupFeedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            return new GroupFeedItemViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.group_feed_item, parent, false));
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull GroupFeedItemViewHolder holder, int position) {
//            Group group = getGroups().get(position);
//
//            holder.name.setText(group.getName());
//            holder.userCount.setText(group.getMembers().size() + " members");
//
//            Glide
//                    .with(getContext())
//                    .load(group.getGroupPhotoResourceID())
//                    .centerCrop()
//                    .format(DecodeFormat.PREFER_RGB_565)
//                    .into(holder.groupProfilePicture);
//
//            Glide
//                    .with(getContext())
//                    .load(group.getRecentPosts().get(0).getEntries().get(0).getImageResource())
//                    .centerCrop()
//                    .format(DecodeFormat.PREFER_RGB_565)
//                    .into(holder.previewImage1);
//
//            Glide
//                    .with(getContext())
//                    .load(group.getRecentPosts().get(1).getEntries().get(0).getImageResource())
//                    .centerCrop()
//                    .format(DecodeFormat.PREFER_RGB_565)
//                    .into(holder.previewImage2);
//
//            holder.moreButton.setOnClickListener(
//                    new OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            showAsMainContent(GroupFragment.newInstance(group.getName()), true);
//                        }
//                    }
//            );
//        }
//
//        @Override
//        public int getItemCount() {
//            return getGroups().size();
//        }
//    }

    private static final class GroupFeedItemViewHolder extends ViewHolder{

        private ImageView groupProfilePicture;

        private TextView name;
        private TextView userCount;
        private TextView lastActiveTime;

        private ImageView previewImage1;
        private ImageView previewImage2;

        private TextView moreButton;

        public GroupFeedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            groupProfilePicture = itemView.findViewById(R.id.group_profile_picture);

            name = itemView.findViewById(R.id.group_name);
            userCount = itemView.findViewById(R.id.member_count);
            lastActiveTime = itemView.findViewById(R.id.last_activity_time);

            previewImage1 = itemView.findViewById(R.id.image_preview_large);
            previewImage2 = itemView.findViewById(R.id.image_preview_small);

            moreButton = itemView.findViewById(R.id.option_view_more);
        }
    }


}
