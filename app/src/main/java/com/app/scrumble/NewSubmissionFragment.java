package com.app.scrumble;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import java.util.ArrayList;
import java.util.List;

public class NewSubmissionFragment extends BaseFragment{

    public static final String NAME = "NEW_SUBMISSION";

    private Button submitButton;

    private EditText titleField;
    private EditText descriptionField;

    private ImageButton addTagButton;
    private TextView tagsList;

    private ImageView imageSelectedIndicator1;
    private ImageView imageSelectedIndicator2;
    private ImageView imageSelectedIndicator3;

    private int tagCount = 0;

    public static NewSubmissionFragment newInstance() {
        Bundle args = new Bundle();
        NewSubmissionFragment fragment = new NewSubmissionFragment();
        fragment.setArguments(args);
        return fragment;
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
                        }else if(!anImageIsSelected()){
                            Toast.makeText(getContext(), "You must select at least one image for this scrapbook", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getContext(), "Your scrapbook has been submitted!", Toast.LENGTH_LONG).show();
                            popBackStack();
                        }
                    }
                }
        );

        titleField = parentLayout.findViewById(R.id.input_title);
        descriptionField = parentLayout.findViewById(R.id.input_description);

        tagsList = parentLayout.findViewById(R.id.tag_list);

        addTagButton = parentLayout.findViewById(R.id.button_add_tag);
        addTagButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int newTagNumber = ++tagCount;
                        String string = "Example tag " + newTagNumber + ", ";
                        tagsList.append(string);
                    }
                }
        );

        OnClickListener imageClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DEBUGGING", "view clicked!");
                float newAlpha = view.getAlpha() == 0.0f ? 1.0f : 0.0f;
                view.setAlpha(newAlpha);
            }
        };

        ImageView image1 = parentLayout.findViewById(R.id.selectable_image_1);
        Glide
                .with(getContext())
                .load(R.drawable.edinburgh_1)
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(image1);

        ImageView image2 = parentLayout.findViewById(R.id.selectable_image_2);
        Glide
                .with(getContext())
                .load(R.drawable.edinburgh_2)
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(image2);

        ImageView image3 = parentLayout.findViewById(R.id.selectable_image_3);
        Glide
                .with(getContext())
                .load(R.drawable.edinburgh_6)
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(image3);

        imageSelectedIndicator1 = parentLayout.findViewById(R.id.selectable_image_1_selected_indicator);
        imageSelectedIndicator1.setOnClickListener(imageClickListener);
        imageSelectedIndicator2 = parentLayout.findViewById(R.id.selectable_image_2_selected_indicator);
        imageSelectedIndicator2.setOnClickListener(imageClickListener);
        imageSelectedIndicator3 = parentLayout.findViewById(R.id.selectable_image_3_selected_indicator);
        imageSelectedIndicator3.setOnClickListener(imageClickListener);
        return parentLayout;
    }

    private boolean inputHasBeenProvidedTo(EditText input){
        return input.getText() != null && input.getText().toString().trim().length() != 0;
    }

    private boolean anImageIsSelected(){
        return imageSelectedIndicator1.getAlpha() != 0.0f || imageSelectedIndicator2.getAlpha() != 0.0f || imageSelectedIndicator3.getAlpha() != 0.0f;
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
}
