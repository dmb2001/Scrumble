package com.app.scrumble;

import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class MainNavigation extends BaseFragment{

    private ImageButton mapButton;
    private ImageButton feedButton;

    private ImageButton groupsButton;

    public static MainNavigation newInstance() {
        Bundle args = new Bundle();
        MainNavigation fragment = new MainNavigation();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_main_navigation, container, false);

        mapButton = parentLayout.findViewById(R.id.button_map);
        feedButton = parentLayout.findViewById(R.id.button_feed);
        groupsButton = parentLayout.findViewById(R.id.button_groups);

        mapButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(new MapFragment(), false);
                    }
                }
        );

        feedButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(FeedFragment.newInstance(), false);
                    }
                }
        );

        groupsButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAsMainContent(GroupFeedFragment.newInstance(), false);
                    }
                }
        );
        return parentLayout;
    }


    @Override
    public String name() {
        return null;
    }
}
