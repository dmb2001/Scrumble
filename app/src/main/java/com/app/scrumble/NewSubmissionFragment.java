package com.app.scrumble;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.app.scrumble.model.group.scrapbook.Entry;
import com.app.scrumble.model.group.scrapbook.Location;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.group.scrapbook.Scrapbook.ScrapBookBuilder;
import com.app.scrumble.model.group.scrapbook.Tag;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NewSubmissionFragment extends BaseFragment{

    public static final String NAME = "NEW_SUBMISSION";
    private static final String KEY_LAT = "KEY_LAT";
    private static final String KEY_LONG = "KEY_LANG";

    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;

    private Button addMemoryButton;
    private Button submitButton;

    private Button addToGroupButton;

    private EditText titleField;
    private EditText descriptionField;

    private Group captionEditingControls;
    private EditText captionInputField;

    private RecyclerView memoryCarousel;
    private LayoutManager layoutManager;
    private MemoryCarouselAdapter adapter;
    private List<UserSelection> userSelections;
    private UserSelection editing;

    private ImageButton addTagButton;
    private TextView tagsList;

    private Button confirmCaptionButton;

    private int tagCount = 0;
    private Set<Tag> tags = new HashSet<Tag>();

    private final long uniqueID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

    private Location location;

    private ArrayList<com.app.scrumble.model.group.Group> selectedGroups = new ArrayList<com.app.scrumble.model.group.Group>();

    public static NewSubmissionFragment newInstance(Location location) {
        Bundle args = new Bundle();
        args.putDouble(KEY_LAT, location.getLatitude());
        args.putDouble(KEY_LONG, location.getLongitude());
        NewSubmissionFragment fragment = new NewSubmissionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickMultipleMedia =
                registerForActivityResult(new PickMultipleVisualMedia(5), uris -> {
                    if(uris != null && uris.size() > 0){
                        userSelections = new ArrayList<>();
                        for (Uri uri : uris){
                            UserSelection selection = new UserSelection();
                            selection.entry = new Entry(null, System.currentTimeMillis(), null);
                            selection.imageLocation = uri;
                            userSelections.add(selection);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_new_submission, container, false);
        submitButton = parentLayout.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!inputHasBeenProvidedTo(titleField)){
                            Toast.makeText(getContext(), "You must enter a title for this scrapbook", Toast.LENGTH_LONG).show();
                        }else if(!inputHasBeenProvidedTo(descriptionField)){
                            Toast.makeText(getContext(), "You must enter a description for this scrapbook", Toast.LENGTH_LONG).show();
                        }else if(!aMemoryHasBeenProvided()){
                            Toast.makeText(getContext(), "You must select at least three memories for this scrapbook", Toast.LENGTH_LONG).show();
                        }else{
                            runInBackground(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){

                                                List<Entry> entries = null;
                                                if(userSelections != null){
                                                    entries = new ArrayList<>();
                                                    for (UserSelection userSelection : userSelections){
                                                        entries.add(userSelection.entry);
                                                    }
                                                }
                                                int debugSize = userSelections == null ? 0 : userSelections.size();
                                                Log.d("DEBUGGING", "The user made: " + debugSize + "selections");
                                                Scrapbook scrapbook =
                                                        new ScrapBookBuilder()
                                                                .withTitle(titleField.getText().toString())
                                                                .withDescription(descriptionField.getText().toString())
                                                                .withLocation(getLocation())
                                                                .withTags(tags)
                                                                .withOwner(getCurrentUser()).withTimeStamp(System.currentTimeMillis())
                                                                .withEntries(entries)
                                                                .build();

                                                Log.d("DEBUGGING:", "tags made: " + scrapbook.getTags().toString());

                                                getScrapBookDAO().createScrapbook(scrapbook);

                                                //Go through all selected groups, and use the groupDAO to associate the scrapbook with them
                                                for (int i = 0; i < selectedGroups.size(); i++) {
                                                    getGroupDAO().postToGroup(scrapbook.getID(),selectedGroups.get(0).getID());
                                                }

                                                if(userSelections != null){
                                                    for (UserSelection selection : userSelections){
                                                        String objectKey = URLStringBuilder.buildMemoryKey(getCurrentUser().getId(), scrapbook.getID(), selection.entry.getID());
                                                        getImageUploader().upload(selection.imageLocation, objectKey);
                                                    }
                                                }

                                                runOnUIThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if(isSafe()){
                                                            Toast.makeText(getContext(), "Your scrapbook has been submitted!", Toast.LENGTH_LONG).show();
                                                            popBackStack();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );

        addMemoryButton = parentLayout.findViewById(R.id.button_add_entry);
        addMemoryButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = (ActivityResultContracts.PickVisualMedia.VisualMediaType) ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
                        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                                .setMediaType(mediaType)
                                .build();
                        pickMultipleMedia.launch(request);

                    }
                }
        );

        captionInputField = parentLayout.findViewById(R.id.caption_edit_field);
        confirmCaptionButton = parentLayout.findViewById(R.id.button_confirm_caption);
        confirmCaptionButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(editing != null){
                            captionEditingControls.setVisibility(View.INVISIBLE);
                            editing.entry.setCaption(captionInputField.getText().toString());
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
        );
        captionEditingControls = parentLayout.findViewById(R.id.caption_editing_controls);

        titleField = parentLayout.findViewById(R.id.input_title);
        descriptionField = parentLayout.findViewById(R.id.input_description);

        tagsList = parentLayout.findViewById(R.id.tag_list);

        addTagButton = parentLayout.findViewById(R.id.button_add_tag);
        addTagButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder tagDialogBuilder = new AlertDialog.Builder(getContext());
                        tagDialogBuilder.setCancelable(true);
                        tagDialogBuilder.setTitle("Add Tag");

                        final EditText tagInput = new EditText(getContext());
                        tagInput.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
                        tagDialogBuilder.setView(tagInput);

                        tagDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String tagText = tagInput.getText().toString();
                                Tag newTag = new Tag(tagText);
                                if (!tagText.isEmpty()) { /*TODO: Add check for identical tag*/
                                    if (!tagsList.getText().equals("")) tagsList.append(", " + tagText); else tagsList.append(tagText);
                                    tags.add(newTag);
                                }
                            }
                        });

                        tagDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        Dialog tagDialog = tagDialogBuilder.create();
                        tagDialog.show();
                    }
                }
        );

        addToGroupButton = parentLayout.findViewById(R.id.button_add_to_group);

        runInBackground(new Runnable() {
                            @Override
                            public void run() {
                                List<com.app.scrumble.model.group.Group> userGroups = getGroupDAO().queryUserGroups(getCurrentUser().getId());//TODO this needs to run in the background or it will crash, setting it to null temporarliity

                                runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //If the user is not part of any groups(i.e. userGroups == null),
                                        //do not show the button for adding the Scrapbook to groups
                                        if (userGroups == null) {
                                            addToGroupButton.setVisibility(View.INVISIBLE);
                                        } else {
                                            addToGroupButton.setVisibility(View.VISIBLE);
                                        }

                                        addToGroupButton.setOnClickListener(
                                                new OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                        Log.d("DEBUGGING:", "Creating Group Addition popup!");

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                                        String[] groupNames = new String[userGroups.size()];

                                                        //Go through every group and put its name into the groupNames array
                                                        for (int i = 0; i < userGroups.size(); i++) {
                                                            groupNames[i] = userGroups.get(i).getName();
                                                        }

                                                        builder.setTitle("Post Scrapbook to Group(s)")
                                                                .setMultiChoiceItems(groupNames, null, new DialogInterface.OnMultiChoiceClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                                                        if (b == true) {
                                                                            selectedGroups.add(userGroups.get(i));
                                                                        } else {
                                                                            selectedGroups.remove(userGroups.get(i));
                                                                        }
                                                                    }
                                                                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                                            }
                                                                        }
                                                                )
                                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        selectedGroups.clear();
                                                                    }
                                                                });

                                                        Dialog groupDialog = builder.create();
                                                        groupDialog.show();
                                                    }
                                                }
                                        );
                                    }
                                });

                            }
                        }
        );

        memoryCarousel = parentLayout.findViewById(R.id.memory_carousel);
        layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        memoryCarousel.setLayoutManager(layoutManager);
        memoryCarousel.setAdapter((adapter == null ? (adapter = new MemoryCarouselAdapter()) : adapter));

        return parentLayout;
    }

    private Location getLocation(){
        if(location == null){
            location = new Location(getArguments().getDouble(KEY_LAT), getArguments().getDouble(KEY_LONG));
        }
        return location;
    }

    private boolean inputHasBeenProvidedTo(EditText input){
        return input.getText() != null && input.getText().toString().trim().length() != 0;
    }

    private boolean aMemoryHasBeenProvided(){
        return userSelections != null && userSelections.size() >= 3;
    }

    @Override
    public void onResume() {
        super.onResume();
        hideNavigationBar();
    }

    @Override
    public String name() {
        return NAME;
    }

    private class MemoryCarouselAdapter extends Adapter<MemoryViewHolder>{

        @NonNull
        @Override
        public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MemoryViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.memory_carousel_item,
                            parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {

            UserSelection selection = userSelections.get(position);
            Glide
                    .with(getContext())
                    .load(selection.imageLocation)
                    .centerCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(holder.thumbnail);
            holder.caption.setText(selection.entry.getCaption() == null ? "tap to add a caption" : selection.entry.getCaption());
            holder.parent.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(captionEditingControls.getVisibility() == View.INVISIBLE){
                        captionEditingControls.setVisibility(View.VISIBLE);
                        editing = userSelections.get(holder.getAdapterPosition());
                        captionInputField.setText(editing.entry.getCaption());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return userSelections == null ? 0 : userSelections.size();
        }

    }

    private class MemoryViewHolder extends ViewHolder{

        private View parent;
        private ImageView thumbnail;
        private TextView caption;

        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.item);
            thumbnail = itemView.findViewById(R.id.memory_thumbnail);
            caption = itemView.findViewById(R.id.caption);
        }

    }

    private static class UserSelection{

        private Entry entry;
        private Uri imageLocation;

    }

}
