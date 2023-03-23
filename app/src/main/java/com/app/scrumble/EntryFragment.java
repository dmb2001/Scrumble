package com.app.scrumble;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.group.scrapbook.Entry;
import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;


public class EntryFragment extends BaseFragment{

    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final String KEY_SCRAPBOOK_ID = "KEY_SCRAPBOOK_ID";
    private static final String KEY_ENTRY_ID = "KEY_ENTRY_ID";

    private ImageView entryImage;
    private TextView imageCaption;

    private Entry entry;

    public static EntryFragment newInstance(long userID, long ScrapBookID, long entryID) {
        Bundle args = new Bundle();
        args.putLong(KEY_SCRAPBOOK_ID, ScrapBookID);
        args.putLong(KEY_ENTRY_ID, entryID);
        args.putLong(KEY_USER_ID, userID);
        EntryFragment fragment = new EntryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_entry, container, false);
        entryImage = parentLayout.findViewById(R.id.entry_image);
        imageCaption = parentLayout.findViewById(R.id.entry_caption);
//
        return parentLayout;
    }

    private void onEntryQueried(Entry entry){
        this.entry = entry;
        imageCaption.setText(entry.getCaption());
        entryImage.setImageResource(R.color.cardview_dark_background);

        String URL = "http://scrumbletest.s3.eu-west-2.amazonaws.com/" + getArguments().getLong(KEY_USER_ID) + "/" + getArguments().getLong(KEY_SCRAPBOOK_ID) + "/" + getArguments().getLong(KEY_ENTRY_ID);


        Glide
                .with(getContext())
                .load(Uri.parse(URL))
                .centerInside()
                .fallback(R.color.cardview_dark_background)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(entryImage);
    }

    @Override
    public void onResume() {
        hideNavigationBar();
        super.onResume();
        if(entry == null){
            runInBackground(
                    new Runnable() {
                        @Override
                        public void run() {
                            Scrapbook scrapbook = getScrapBookDAO().queryScrapbookByID(getArguments().getLong(KEY_SCRAPBOOK_ID));
                            if(scrapbook != null){
                                Entry entry = scrapbook.getEntryByID(getArguments().getLong(KEY_ENTRY_ID));
                                if(entry != null){
                                    runOnUIThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    onEntryQueried(entry);
                                                }
                                            }
                                    );
                                }
                            }
                        }
                    }
            );
        }
    }

    @Override
    public String name() {
        return null;
    }

}
