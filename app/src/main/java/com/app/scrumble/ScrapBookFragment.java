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

import com.app.scrumble.model.Entry;
import com.app.scrumble.model.Location;
import com.app.scrumble.model.Scrapbook;
import com.app.scrumble.model.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import java.util.concurrent.TimeUnit;

public class ScrapBookFragment extends BaseFragment {

    public static final String NAME = "SCRAP_BOOK";

    private static final String KEY_SCRAPBOOK_ID = "KEY_SCRAPBOOK_ID";

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

        Log.d("DEBUGGING", "scrapbook ID is: " + getArguments().getLong(KEY_SCRAPBOOK_ID));

        Scrapbook scrapbook = getScrapbookByID(getArguments().getLong(KEY_SCRAPBOOK_ID));

        RecyclerView carousel = parentLayout.findViewById(R.id.image_carousel);
        LayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        carousel.setLayoutManager(layoutManager);
        carousel.setHasFixedSize(true);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(carousel);
        carousel.setAdapter(new CarouselAdapter(scrapbook));

        TextView title = parentLayout.findViewById(R.id.title);
        title.setText(
                scrapbook.getTitle());

        TextView username = parentLayout.findViewById(R.id.user_name);
        username.setText("@" + scrapbook.getOwner().getUsername());

        TextView date = parentLayout.findViewById(R.id.date);
        if(scrapbook.getLastUpdate() == Scrapbook.NEVER_UPDATED){
            date.setText("Never updated");
        }else{
            date.setText(DateUtils.getRelativeTimeSpanString(scrapbook.getLastUpdate(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        }

        ImageView profilePicture = parentLayout.findViewById(R.id.profile_picture);
        profilePicture.setImageResource(scrapbook.getOwner().getProfilePictureResourceID());
        profilePicture.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(
                                ProfileFragment.newInstance(scrapbook.getOwner().getUsername()), true
                        );
                    }
                }
        );

        TextView tags = parentLayout.findViewById(R.id.tags);
        tags.append("Tags:");
        for(String tag : scrapbook.getTags()){
            tags.append(" " + tag + ",");
        }

        TextView description = parentLayout.findViewById(R.id.description);
        description.setText(scrapbook.getDescription());

        return parentLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        showAsNavigationContent(ScrapbookNavigationFragment.newInstance(getArguments().getLong(KEY_SCRAPBOOK_ID)));
    }

    @Override
    public String name() {
        return NAME;
    }


    private class CarouselAdapter extends Adapter<CarouselItemViewHolder>{

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
            Glide
                    .with(getContext())
                    .load(entry.getImageResource())
                    .centerCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(holder.image);

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
            return scrapbook.getEntries().size();
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
