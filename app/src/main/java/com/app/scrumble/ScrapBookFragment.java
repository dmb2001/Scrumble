package com.app.scrumble;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

import com.app.scrumble.model.group.scrapbook.Entry;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.app.scrumble.model.group.scrapbook.Tag;

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
        tagsField.append("Tags:");
        if(scrapbook.getTags() != null){
            for(Tag tag : scrapbook.getTags()){
                if (!tag.isHidden()) {
                    tagsField.append(" " + tag.getName() + ",");
                }
            }
        }else{
            tagsField.append(" none");
        }
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
