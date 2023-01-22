package com.app.scrumble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

public class MapFragment extends BaseFragment {

    public static final String NAME = "MAP";

    public static MapFragment newInstance() {
        Bundle args = new Bundle();
        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_map, container, false);

        ImageView mapView = parentLayout.findViewById(R.id.map);
        Glide
                .with(this)
                .load(R.drawable.map_static)
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(mapView);

        parentLayout.findViewById(R.id.scrapbook_1_pin).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(ScrapBookFragment.newInstance(MainActivity.SCRAPBOOK_1_ID), true);
                    }
                }
        );
        parentLayout.findViewById(R.id.scrapbook_2_pin).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(ScrapBookFragment.newInstance(MainActivity.SCRAPBOOK_2_ID), true);
                    }
                }
        );
        parentLayout.findViewById(R.id.scrapbook_3_pin).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(ScrapBookFragment.newInstance(MainActivity.SCRAPBOOK_3_ID), true);
                    }
                }
        );
        parentLayout.findViewById(R.id.button_new_post).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(NewSubmissionFragment.newInstance(), true);
                    }
                }
        );
        parentLayout.findViewById(R.id.button_nav_profile).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(ProfileFragment.newInstance(getCurrentUser().getUsername()), true);
                    }
                }
        );
        return parentLayout;
    }

    @Override
    public void onResume() {
        showAsNavigationContent(MainNavigation.newInstance());
        super.onResume();
    }

    @Override
    public String name() {
        return NAME;
    }
}
