package com.app.scrumble;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.SnapHelper;

import com.app.scrumble.model.group.Group;
import com.app.scrumble.model.group.scrapbook.Entry;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.group.scrapbook.Tag;

import java.util.List;

public class ScrapBookFragment extends BaseFragment {

    public static final String NAME = "SCRAP_BOOK";
    private static final String KEY_SCRAPBOOK_ID = "KEY_SCRAPBOOK_ID";
    private TextView titleField;
    private TextView usernameField;
    private TextView dateField;
    private TextView tagsField;
    private TextView description;
    private ImageView profilePicture;

    private Scrapbook scrapbook;

    private TextView noGroupText;
    private Button seeGroupsButton;

    LayoutManager layoutManager;
    RecyclerView carousel;


    public static ScrapBookFragment newInstance(long scrapbook) {

        Bundle args = new Bundle();
        args.putLong(KEY_SCRAPBOOK_ID, scrapbook);
        ScrapBookFragment fragment = new ScrapBookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = LayoutInflater.from(getContext()).inflate(R.layout.fragment_scrapbook, container, false);

        titleField = parentLayout.findViewById(R.id.title);
        usernameField = parentLayout.findViewById(R.id.user_name);
        dateField = parentLayout.findViewById(R.id.date);
        profilePicture = parentLayout.findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(
                                ProfileFragment.newInstance(scrapbook.getOwner().getId()), true
                        );
                    }
                }
        );
        tagsField = parentLayout.findViewById(R.id.tags);
        description = parentLayout.findViewById(R.id.description);
        carousel = parentLayout.findViewById(R.id.image_carousel);

        //Get group-related views
        noGroupText = parentLayout.findViewById(R.id.text_no_groups);
        seeGroupsButton = parentLayout.findViewById(R.id.button_see_groups);

        return parentLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        showAsNavigationContent(ScrapbookNavigationFragment.newInstance(getArguments().getLong(KEY_SCRAPBOOK_ID)));
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe()){
                            scrapbook = getScrapBookDAO().queryScrapbookByID(getArguments().getLong(KEY_SCRAPBOOK_ID));
                            Log.d("DEBUGGING", "the scrapbook has: " + (scrapbook.getEntries() == null ? 0 : scrapbook.getEntries().size()) + " entries");
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                populateContent();
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    private void populateContent(){
        titleField.setText(scrapbook.getTitle());
        usernameField.setText("@" + scrapbook.getOwner().getUsername());
        description.setText(scrapbook.getDescription());
        if(scrapbook.getLastUpdate() == Scrapbook.NEVER_UPDATED){
            dateField.setText("Never updated");
        }else{
            dateField.setText(DateUtils.getRelativeTimeSpanString(scrapbook.getLastUpdate(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        }
        tagsField.setText("");
        if(!scrapbook.getTags().isEmpty()){
            for(Tag tag : scrapbook.getTags()){
                if (!tag.isHidden()) {
                    if (!tagsField.getText().equals("")) tagsField.append(", " + tag.getName()); else tagsField.append(tag.getName());
                }
            }
        }else{
            tagsField.setText("none");
        }
        tagsField.setText("Tags: " + tagsField.getText());

        profilePicture.setImageResource(R.drawable.image_user_pp_3);
        if (layoutManager == null){
            layoutManager =
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            carousel.setLayoutManager(layoutManager);
            carousel.setHasFixedSize(true);
            SnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(carousel);
            carousel.setAdapter(new CarouselAdapter(scrapbook));
        }

        //Populate group-related content
        //List<Group> relatedGroups = getGroupDAO().queryGroupsContainingScrapbookID(scrapbook.getID()); TODO: Must do networking in background thread, so null for now
        List<Group> relatedGroups = null;

        if (relatedGroups == null) {
            seeGroupsButton.setVisibility(View.INVISIBLE);
            noGroupText.setVisibility(View.VISIBLE);
        } else {
            seeGroupsButton.setVisibility(View.VISIBLE);
            noGroupText.setVisibility(View.INVISIBLE);

            seeGroupsButton.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Group[] selectedGroup = {null}; //Initialize a variable for a group selected from the list

                            //Go through every group and put its name into the groupNames array
                            String[] groupNames = new String[relatedGroups.size()];

                            for (int i = 0; i < relatedGroups.size(); i++) {
                                groupNames[i] = relatedGroups.get(i).getName();
                            }

                            Log.d("DEBUGGING:","Acquired "+Integer.toString(relatedGroups.size())+" Groups!");

                            Log.d("DEBUGGING:", "Creating Scrapbook Groups popup!");

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setTitle("Press on a Group's Name to see its Information")
                                    .setItems(groupNames, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                selectedGroup[0] = relatedGroups.get(i);
                                                Log.d("DEBUGGING:","Pressed on "+selectedGroup[0].getName()+"!");
                                                showAsMainContent(
                                                        GroupFragment.newInstance(selectedGroup[0].getID())
                                                ,true);



                                        }
                                    }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            }
                                    );

                            Dialog groupsDialog = builder.create();
                            groupsDialog.show();
                        }
                    }
            );

        }
    }

    @Override
    public String name() {
        return NAME;
    }


    private class CarouselAdapter extends Adapter<CarouselItemViewHolder> {

        private Scrapbook scrapbook;

        private CarouselAdapter(Scrapbook scrapbook) {
            this.scrapbook = scrapbook;
        }

        @NonNull
        @Override
        public CarouselItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CarouselItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.scrapbook_carousel_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CarouselItemViewHolder holder, int position) {
            Entry entry = scrapbook.getEntries().get(position);
            holder.image.setImageResource(R.color.cardview_dark_background);

            holder.image.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showAsMainContent(
                                    EntryFragment.newInstance(scrapbook.getID(), scrapbook.getEntries().get(position).getID()), true);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return scrapbook.getEntries() == null ? 0 : scrapbook.getEntries().size();
        }
    }

    private static class CarouselItemViewHolder extends ViewHolder{

        private ImageView image;

        public CarouselItemViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.artwork);
        }
    }

}
