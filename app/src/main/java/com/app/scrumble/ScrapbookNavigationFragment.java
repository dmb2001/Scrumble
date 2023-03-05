package com.app.scrumble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.group.scrapbook.Scrapbook;

public class ScrapbookNavigationFragment extends BaseFragment{

    private static final String KEY_SCRAPBOOK_ID = "KEY_SCRAPBOOK_ID";

    private Scrapbook scrapbook;

    private ImageButton commentsButton;
    private ImageButton likeButton;
    private ImageButton reactionButton;
    private ImageButton flagButton;

    public static ScrapbookNavigationFragment newInstance(long scrapBookID) {
        Bundle args = new Bundle();
        args.putLong(KEY_SCRAPBOOK_ID, scrapBookID);
        ScrapbookNavigationFragment fragment = new ScrapbookNavigationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View parentLayout = inflater.inflate(R.layout.fragment_scrapbook_navigation, container, false);

        commentsButton = parentLayout.findViewById(R.id.button_comments);
        commentsButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(scrapbook != null){
                            showAsMainContent(CommentsFragment.newInstance(scrapbook.getID()), true);
                        }
                    }
                }
        );
//        likeButton = parentLayout.findViewById(R.id.button_like);
//        likeButton.setOnClickListener(
//                new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Toast.makeText(getContext(), ("You have liked " + scrapbook.getOwner().getUsername() + "'s scrapbook"), Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//        reactionButton = parentLayout.findViewById(R.id.button_reactions);
//        reactionButton.setOnClickListener(
//                new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Toast.makeText(getContext(), ("You have reacted " + scrapbook.getOwner().getUsername() + "'s scrapbook"), Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//        flagButton = parentLayout.findViewById(R.id.button_report);
//        flagButton.setOnClickListener(
//                new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Toast.makeText(getContext(), ("You have reported " + scrapbook.getOwner().getUsername() + "'s scrapbook"), Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );

        return parentLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe()){
                            final Scrapbook scrapbook = getScrapBookDAO().queryScrapbookByID(getArguments().getLong(KEY_SCRAPBOOK_ID));
                            runOnUIThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if(isSafe()){
                                                ScrapbookNavigationFragment.this.scrapbook = scrapbook;
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    @Override
    public String name() {
        return null;
    }
}
