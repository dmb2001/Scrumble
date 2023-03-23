package com.app.scrumble;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.app.scrumble.model.group.Group;
import com.app.scrumble.model.group.scrapbook.Location;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.user.User;

import java.util.List;

public class GroupFragment extends BaseFragment{

    private static final String GROUP_KEY = "GROUP";
    private static final String LATITUDE_KEY = "LATITUDE";
    private static final String LONGITUDE_KEY = "LONGITUDE";

    private Button joinGroupButton;
    private Button leaveGroupButton;

    private TextView groupOwnerText;

    public static GroupFragment newInstance(long groupID) {
        Bundle args = new Bundle();
        args.putLong(GROUP_KEY, groupID);

        GroupFragment fragment = new GroupFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_group, container, false);

        Group group = getGroupDAO().queryGroupByID(getArguments().getLong(GROUP_KEY));
        ImageView groupProfilePicture = parentLayout.findViewById(R.id.group_profile_picture);

        Location location = new Location(getArguments().getDouble(LATITUDE_KEY),getArguments().getDouble(LONGITUDE_KEY));

        //groupProfilePicture.setImageResource(group.getGroupPhotoResourceID());
        TextView groupTitle = parentLayout.findViewById(R.id.group_name);
        groupTitle.setText(group.getName());

        RecyclerView list = parentLayout.findViewById(R.id.scrapbook_feed);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        list.setAdapter(new PostAdapter(group));

        TextView memberCount = parentLayout.findViewById(R.id.member_count);
        memberCount.setText(group.getMembers().size() + " members");

        TextView membersList = parentLayout.findViewById(R.id.member_list);
        membersList.append("Members:");
        for(User member : group.getMembers()){
            membersList.append(" @" + member.getUsername() + ",");
        }

        //Initialize Buttons and Text for Leaving and Joining a group
        joinGroupButton = parentLayout.findViewById(R.id.button_join_group);
        leaveGroupButton = parentLayout.findViewById(R.id.button_leave_group);
        groupOwnerText = parentLayout.findViewById(R.id.text_owner);

        //If the current user is not the first user in the Group's members(i.e. group owner) and is part of the group,
        //make the Leaving group button visible
        if (getCurrentUser().getId() != group.getGroupOwnerID() && group.isMember(getCurrentUser())) {
            Log.d("DEBUGGING","ID "+Long.toString(getCurrentUser().getId())+" is not same as ID of owner: "
                    +Long.toString(group.getGroupOwnerID())+", and they are also a member!");
            joinGroupButton.setVisibility(View.INVISIBLE);
            leaveGroupButton.setVisibility(View.VISIBLE);
            groupOwnerText.setVisibility(View.INVISIBLE);
        } else if (!group.isMember(getCurrentUser())) {
            //Otherwise, if the current user is not a member of this group, make the Join Group button visible
            Log.d("DEBUGGING","User not a member, making joining visible!");
            joinGroupButton.setVisibility(View.VISIBLE);
            leaveGroupButton.setVisibility(View.INVISIBLE);
            groupOwnerText.setVisibility(View.INVISIBLE);
        } else {
            //Otherwise, hide both buttons and show the user they're the owner - transferring
            //ownership has not been implemented yet
            Log.d("DEBUGGING","User the owner!");
            joinGroupButton.setVisibility(View.INVISIBLE);
            leaveGroupButton.setVisibility(View.INVISIBLE);
            groupOwnerText.setVisibility(View.VISIBLE);
        }
        //Set onclick listener for joining a group
        joinGroupButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getGroupDAO().joinGroup(getCurrentUser().getId(),group.getID());
                getActivity().onBackPressed();
                Log.d("DEBUGGING", "Joined group!");
            }
        });

        //Set onclick listener for leaving a group
        leaveGroupButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getGroupDAO().leaveGroup(getCurrentUser().getId(),group.getID());
                getActivity().onBackPressed();
                Log.d("DEBUGGING", "Left group!");
            }
        });

        return parentLayout;
    }

    private class PostAdapter extends Adapter<GroupFeedItemHolder>{

        private Group group;

        private List<Scrapbook> groupPosts;

        private PostAdapter(Group group){
            this.group = group;
            this.groupPosts = getGroupDAO().queryScrapbooksInGroup(group.getID());
            if (groupPosts == null || groupPosts.size() == 0) {
                Log.d("DEBUGGING:","No scrapbooks associated with Group found!");
            } else if (groupPosts.size() > 0) {
                Log.d("DEBUGGING:","Found "+Integer.toString(groupPosts.size())+" posts associated with Group!");
            }
        }

       @NonNull
       @Override
        public GroupFeedItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GroupFeedItemHolder(LayoutInflater.from(getContext()).inflate(R.layout.group_feed_post, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull GroupFeedItemHolder holder, int position) {
            Scrapbook scrapbook = groupPosts.get(holder.getAdapterPosition());
            Log.d("DEBUGGING:","Got Scrapbook ID "+Long.toString(scrapbook.getID())+"!");
            //holder.profilePicture.setImageResource(scrapbook.getOwner().getProfilePictureResourceID());
            //holder.preview_large.setImageResource(scrapbook.getEntries().get(0).getImageResource());
            //holder.preview_small.setImageResource(scrapbook.getEntries().get(1).getImageResource());
            //holder.distance.setText(scrapbook.getLocation().distanceFrom(currentLocation) + " miles away");

            holder.username.setText("@" + scrapbook.getOwner().getUsername());
            holder.postDate.setText(DateUtils.getRelativeTimeSpanString(scrapbook.getLastUpdate(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));

            //holder.viewMoreOption.setText("+ " + (scrapbook.getEntries().size() - 2) + " more");
            holder.viewMoreOption.setText("View Scrapbook in Detail");
            holder.viewMoreOption.setOnClickListener(
                new OnClickListener() {
                    @Override
                        public void onClick(View view) {
                                showAsMainContent(ScrapBookFragment.newInstance(scrapbook.getID()), true);
                            }
                        }
                );

            holder.commentsButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(CommentsFragment.newInstance(scrapbook.getID()), true);
                        }
                    }
                );
            holder.likeButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), "You have liked " + scrapbook.getOwner().getUsername() + "'s scrapbook", Toast.LENGTH_SHORT).show();
                            }
                    }
                );

            holder.reactionButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), "You have reacted to " + scrapbook.getOwner().getUsername() + "'s scrapbook", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return groupPosts == null ? 0 : groupPosts.size();
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
