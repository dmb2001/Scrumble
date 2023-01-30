package com.app.scrumble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.app.scrumble.model.scrapbook.Comment;
import com.app.scrumble.model.scrapbook.Entry;
import com.app.scrumble.model.group.Group;
import com.app.scrumble.model.scrapbook.Location;
import com.app.scrumble.model.scrapbook.Scrapbook;
import com.app.scrumble.model.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final long SCRAPBOOK_1_ID = 1;
    public static final long SCRAPBOOK_2_ID = 2;
    public static final long SCRAPBOOK_3_ID = 3;
    public static final String TAG_EDINBURGH = "Edinburgh";
    public static final String TAG_TRAVEL = "Travel";
    public static final String TAG_MEMORIES = "Memories";

    public static final long SCRAPBOOK_FOODIE_1_ID = 4;
    public static final long SCRAPBOOK_FOODIE_2_ID = 5;
    public static final long SCRAPBOOK_FOODIE_3_ID = 6;
    public static final String TAG_FOODIE = "Foodie";

    public static final long SCRAPBOOK_ADVENTURE_1_ID = 7;
    public static final long SCRAPBOOK_ADVENTURE_2_ID = 8;
    public static final String TAG_ADVENTURE = "Adventure";

    public static final long SCRAPBOOK_LANDMARKS_1_ID = 9;
    public static final long SCRAPBOOK_LANDMARKS_2_ID = 10;
    public static final long SCRAPBOOK_LANDMARKS_3_ID = 11;
    public static final String TAG_LANDMARKS = "Landmarks";

    private static final String CAPTION = "Lorem ipsum dolor sit amet. Ut voluptatem maxime aut assumenda incidunt qui fuga pariatur sed consectetur dolore est molestiae odio et corrupti quidem. Et accusamus sapiente eum repellendus voluptatem rem aperiam repellendus non fuga quia. Est nobis eaque ut placeat quas nam voluptatem cupiditate aut expedita eligendi.";

    private List<Scrapbook> scrapbooks = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    private List<Comment> exampleComments = new ArrayList<>();

    private List<Group> groups = new ArrayList<>();

    private ConstraintLayout parentLayout;
    private ConstraintSet bottomBarHiddenConstraints;
    private ConstraintSet bottomBarShowingConstraints;

    private static final long BOTTOM_BAR_TRANSITION_DURATION = 100;
    private final Transition bottomBarTransition = new AutoTransition().setDuration(BOTTOM_BAR_TRANSITION_DURATION);

    private boolean firstAction = true;

    private ImageButton mapButton;
    private ImageButton feedButton;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parentLayout = findViewById(R.id.parent_layout);

        bottomBarHiddenConstraints = new ConstraintSet();
        bottomBarHiddenConstraints.clone(parentLayout);
        bottomBarHiddenConstraints.clear(R.id.navigation_container, ConstraintSet.TOP);
        bottomBarHiddenConstraints.clear(R.id.navigation_container, ConstraintSet.BOTTOM);
        bottomBarHiddenConstraints.connect(
                R.id.navigation_container,
                ConstraintSet.TOP,
                R.id.parent_layout,
                ConstraintSet.BOTTOM);

        bottomBarShowingConstraints = new ConstraintSet();
        bottomBarShowingConstraints.clone(parentLayout);
        bottomBarShowingConstraints.clear(R.id.navigation_container, ConstraintSet.TOP);
        bottomBarShowingConstraints.clear(R.id.navigation_container, ConstraintSet.BOTTOM);
        bottomBarShowingConstraints.connect(
                R.id.navigation_container,
                ConstraintSet.BOTTOM,
                R.id.parent_layout,
                ConstraintSet.BOTTOM);

//        mapButton = findViewById(R.id.button_map);
//        feedButton = findViewById(R.id.button_feed);

        if(hasNeededPermissions()){
            if(currentUser != null){
                showAsMainContent(MapFragment.newInstance(), false);
            }else{
                showAsMainContent(LoginFragment.newInstance(), false);
            }
        }else{
//            show screen where user can grant permissions
            showAsMainContent(new PermissionRequestFragment(), false);
        }
    }

    private boolean hasNeededPermissions(){

        int fineLocationGranted = ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION);
        int courseLocationGranted = ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION);

        return fineLocationGranted == PackageManager.PERMISSION_GRANTED && courseLocationGranted == PackageManager.PERMISSION_GRANTED;

    }

    public void setCurrentUser(User user){
        if(!Objects.equals(currentUser, user)){
            this.currentUser = user;
            if(currentUser == null){
                popEntireBackStack();
                showAsMainContent(LoginFragment.newInstance(), false);
            }else{
                showAsMainContent(MapFragment.newInstance(), false);
            }
        }
    }

    public final User getCurrentUser(){
        return currentUser;
    }

    public final UserDAO getUserDAO(){
        return ((Scrumble)getApplication()).getUserDAO();
    }

    public final ScrapbookDAO getScrapBookDAO(){
        return ((Scrumble)getApplication()).getScrapBookDAO();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!hasNeededPermissions()){
            popEntireBackStack();
            showAsMainContent(PermissionRequestFragment.newInstance(), false);
        }
    }

    //    private void generateDummyData() {
//        User user1 = new User("Susan", "example@example.com", "password", "travelFan100", R.drawable.image_user_pp_1);
//        User user2 = new User("John", "example@example.com", "password", "scrapbooker2000", R.drawable.image_user_pp_2);
//        User user3 = new User("Helen", "example@example.com", "password", "superScrap", R.drawable.image_user_pp_3);
//        User user4 = new User("Sam", "example@example.com", "password", "investhymn", R.drawable.image_user_pp_4);
//        User user5 = new User("Dale", "example@example.com", "password", "STARsue", R.drawable.image_user_pp_5);
//        User user6 = new User("Jane", "example@example.com", "password", "doubleDOT", R.drawable.image_user_pp_6);
//        User user7 = new User("Jonathan", "example@example.com", "password", "travel_fan", R.drawable.image_user_pp_7);
//        User user8 = new User("Richard", "example@example.com", "password", "angelrich", R.drawable.image_user_pp_8);
//        User user9 = new User("Julia", "example@example.com", "password", "RaInBoW", R.drawable.image_user_pp_9);
//        User user10 = new User("John", "example@example.com", "password", "john_smith", R.drawable.image_user_pp_10);
//
//        users.add(user1);
//        users.add(user2);
//        users.add(user3);
//        users.add(user4);
//        users.add(user5);
//        users.add(user6);
//        users.add(user7);
//        users.add(user8);
//        users.add(user9);
//        users.add(user10);
//
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5), user4, R.string.comment_1));
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(4), user5, R.string.comment_2));
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3), user4, R.string.comment_3));
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(4), user6, R.string.comment_4));
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5), user7, R.string.comment_5));
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5), user4, R.string.comment_6));
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3), user8, R.string.comment_7));
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5), user9, R.string.comment_8));
//        exampleComments.add(new Comment(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5), user10, R.string.comment_9));
//
//
//        Scrapbook scrapbook1 = new Scrapbook(SCRAPBOOK_1_ID, user1, new Location(55.9520, -3.2002));
//        scrapbook1.setTitle("Holiday 2022");
//        scrapbook1.setTag(TAG_EDINBURGH);
//        scrapbook1.setTag(TAG_TRAVEL);
//        scrapbook1.setTag(TAG_MEMORIES);
//        scrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)), R.drawable.edinburgh_1, CAPTION));
//        scrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5)), R.drawable.edinburgh_2, CAPTION));
//        scrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)),R.drawable.edinburgh_3, CAPTION));
//        scrapbooks.add(scrapbook1);
//
//        Scrapbook scrapbook2 = new Scrapbook(SCRAPBOOK_2_ID, user2, new Location(55.9495, -3.1956));
//        scrapbook2.setTitle("November trip");
//        scrapbook2.setTag(TAG_EDINBURGH);
//        scrapbook2.setTag(TAG_TRAVEL);
//        scrapbook2.setTag(TAG_MEMORIES);
//        scrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)), R.drawable.edinburgh_4, CAPTION));
//        scrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(9)), R.drawable.edinburgh_5, CAPTION));
//        scrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.DAYS.toMillis(20)),R.drawable.edinburgh_6, CAPTION));
//        scrapbooks.add(scrapbook2);
//
//        Scrapbook scrapbook3 = new Scrapbook(SCRAPBOOK_3_ID, user3, new Location(55.9476, -3.1924));
//        scrapbook3.setTitle("Sightseeing");
//        scrapbook3.setTag(TAG_EDINBURGH);
//        scrapbook3.setTag(TAG_TRAVEL);
//        scrapbook3.setTag(TAG_MEMORIES);
//        scrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.DAYS.toMillis(6)), R.drawable.edinburgh_7, CAPTION));
//        scrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)), R.drawable.edinburgh_8, CAPTION));
//        scrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.DAYS.toMillis(12)),R.drawable.edinburgh_9, CAPTION));
//        scrapbooks.add(scrapbook3);
//
//        Group foodieGroup = new Group("Foodie", R.drawable.foodie_group_pp);
//        foodieGroup.addMember(user1);
//        foodieGroup.addMember(user2);
//        foodieGroup.addMember(user3);
//        foodieGroup.addMember(user4);
//
//        Scrapbook foodieScrapbook1 = new Scrapbook(SCRAPBOOK_FOODIE_1_ID, user5, new Location(55.9476, -3.1924));
//        foodieScrapbook1.setTitle("Yumm");
//        foodieScrapbook1.setTag(TAG_FOODIE);
//        foodieScrapbook1.setTag(TAG_TRAVEL);
//        scrapbooks.add(foodieScrapbook1);
//        foodieScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)), R.drawable.foodie_1, CAPTION));
//        foodieScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(6)), R.drawable.foodie_2, CAPTION));
//        foodieScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(10)), R.drawable.foodie_3, CAPTION));
//        foodieGroup.addRecentPost(foodieScrapbook1);
//
//        Scrapbook foodieScrapbook2 = new Scrapbook(SCRAPBOOK_FOODIE_2_ID, user10, new Location(55.9476, -3.1924));
//        foodieScrapbook2.setTitle("Best Foods");
//        foodieScrapbook2.setTag(TAG_FOODIE);
//        foodieScrapbook2.setTag(TAG_TRAVEL);
//        foodieScrapbook2.setTag(TAG_MEMORIES);
//        scrapbooks.add(foodieScrapbook2);
//        foodieScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)), R.drawable.foodie_4, CAPTION));
//        foodieScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)), R.drawable.foodie_5, CAPTION));
//        foodieScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)), R.drawable.foodie_6, CAPTION));
//        foodieGroup.addRecentPost(foodieScrapbook2);
//
//        Scrapbook foodieScrapbook3 = new Scrapbook(SCRAPBOOK_FOODIE_3_ID, user2, new Location(55.9476, -3.1924));
//        foodieScrapbook3.setTitle("Food Hot-Spots");
//        foodieScrapbook3.setTag(TAG_FOODIE);
//        foodieScrapbook3.setTag(TAG_MEMORIES);
//        scrapbooks.add(foodieScrapbook3);
//        foodieScrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)), R.drawable.foodie_7, CAPTION));
//        foodieScrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(7)), R.drawable.foodie_8, CAPTION));
//        foodieScrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(8)), R.drawable.foodie_9, CAPTION));
//        foodieGroup.addRecentPost(foodieScrapbook3);
//        groups.add(foodieGroup);
//
//        Group adventureGroup = new Group("Adventure", R.drawable.adventure_group_pp);
//        adventureGroup.addMember(user1);
//        adventureGroup.addMember(user2);
//        adventureGroup.addMember(user3);
//        adventureGroup.addMember(user4);
//        adventureGroup.addMember(user5);
//        adventureGroup.addMember(user6);
//        adventureGroup.addMember(user7);
//
//        Scrapbook adventureScrapbook1 = new Scrapbook(SCRAPBOOK_ADVENTURE_1_ID, user3, new Location(55.9476, -3.1924));
//        adventureScrapbook1.setTitle("My Adventures");
//        adventureScrapbook1.setTag(TAG_ADVENTURE);
//        adventureScrapbook1.setTag(TAG_TRAVEL);
//        adventureScrapbook1.setTag(TAG_MEMORIES);
//        scrapbooks.add(adventureScrapbook1);
//        adventureScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(4)), R.drawable.adventure_1, CAPTION));
//        adventureScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)), R.drawable.adventure_2, CAPTION));
//        adventureScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)), R.drawable.adventure_3, CAPTION));
//        adventureGroup.addRecentPost(adventureScrapbook1);
//
//        Scrapbook adventureScrapbook2 = new Scrapbook(SCRAPBOOK_ADVENTURE_2_ID, user4, new Location(55.9476, -3.1924));
//        adventureScrapbook2.setTitle("Hiking 2022");
//        adventureScrapbook2.setTag(TAG_ADVENTURE);
//        adventureScrapbook2.setTag(TAG_TRAVEL);
//        adventureScrapbook2.setTag(TAG_MEMORIES);
//        scrapbooks.add(adventureScrapbook2);
//        adventureScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)), R.drawable.adventure_4, CAPTION));
//        adventureScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)), R.drawable.adventure_5, CAPTION));
//        adventureScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12)), R.drawable.adventure_6, CAPTION));
//        adventureScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)), R.drawable.adventure_7, CAPTION));
//        adventureScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(36)), R.drawable.adventure_8, CAPTION));
//        adventureScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5)), R.drawable.adventure_9, CAPTION));
//        adventureGroup.addRecentPost(adventureScrapbook2);
//        groups.add(adventureGroup);
//
//        Group landmarksGroup = new Group("Landmarks", R.drawable.landmark_group_pp);
//        landmarksGroup.addMember(user10);
//        landmarksGroup.addMember(user9);
//        landmarksGroup.addMember(user8);
//        landmarksGroup.addMember(user7);
//        landmarksGroup.addMember(user6);
//
//        Scrapbook landmarksScrapbook1 = new Scrapbook(SCRAPBOOK_LANDMARKS_1_ID, user8, new Location(55.9476, -3.1924));
//        adventureScrapbook2.setTitle("Hiking 2022");
//        landmarksScrapbook1.setTag(TAG_LANDMARKS);
//        landmarksScrapbook1.setTag(TAG_TRAVEL);
//        landmarksScrapbook1.setTag(TAG_MEMORIES);
//        scrapbooks.add(landmarksScrapbook1);
//        landmarksScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)), R.drawable.landmark_1, CAPTION));
//        landmarksScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(4)), R.drawable.landmark_2, CAPTION));
//        landmarksScrapbook1.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(20)), R.drawable.landmark_3, CAPTION));
//        landmarksGroup.addRecentPost(landmarksScrapbook1);
//
//        Scrapbook landmarksScrapbook2 = new Scrapbook(SCRAPBOOK_LANDMARKS_2_ID, user9, new Location(55.9476, -3.1924));
//        landmarksScrapbook2.setTitle("Beautiful Memories");
//        landmarksScrapbook2.setTag(TAG_LANDMARKS);
//        landmarksScrapbook2.setTag(TAG_TRAVEL);
//        landmarksScrapbook2.setTag(TAG_MEMORIES);
//        scrapbooks.add(landmarksScrapbook2);
//        landmarksScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(20)), R.drawable.landmark_4, CAPTION));
//        landmarksScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(20)), R.drawable.landmark_5, CAPTION));
//        landmarksScrapbook2.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(22)), R.drawable.landmark_6, CAPTION));
//        landmarksGroup.addRecentPost(landmarksScrapbook2);
//
//        Scrapbook landmarksScrapbook3 = new Scrapbook(SCRAPBOOK_LANDMARKS_3_ID, user9, new Location(55.9476, -3.1924));
//        landmarksScrapbook3.setTitle("Favourite Shots");
//        landmarksScrapbook3.setTag(TAG_LANDMARKS);
//        landmarksScrapbook3.setTag(TAG_TRAVEL);
//        landmarksScrapbook3.setTag(TAG_MEMORIES);
//        scrapbooks.add(landmarksScrapbook3);
//        landmarksScrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)), R.drawable.landmark_7, CAPTION));
//        landmarksScrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)), R.drawable.landmark_8, CAPTION));
//        landmarksScrapbook3.addEntry(new Entry((System.currentTimeMillis() - TimeUnit.HOURS.toMillis(6)), R.drawable.landmark_9, CAPTION));
//        landmarksGroup.addRecentPost(landmarksScrapbook3);
//        groups.add(landmarksGroup);
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    void showAsMainContent(Fragment fragment, boolean addTransitionToBackStack){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.main_content_container, fragment);
        if(firstAction){
           firstAction = false;
        }else if(addTransitionToBackStack){
            fragmentTransaction.addToBackStack(null);
        }
        HandlerCompat.createAsync(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        fragmentTransaction.commit();
                        getSupportFragmentManager().executePendingTransactions();
                    }
                }
        );
    }

    void showAsNavigation(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.navigation_container, fragment);
        HandlerCompat.createAsync(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        fragmentTransaction.commit();
                        getSupportFragmentManager().executePendingTransactions();
                    }
                }
        );
        if(!navigationBarShowing()){
            showNavigationBar();
        }
    }

    protected void popBackStack(){
        getSupportFragmentManager().popBackStack();
    }

    protected void popEntireBackStack(){
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private boolean navigationBarShowing(){
        ConstraintLayout.LayoutParams layoutParameters =
                (LayoutParams) findViewById(R.id.navigation_container).getLayoutParams();
        return layoutParameters.bottomToBottom == R.id.parent_layout;
    }

    protected final void hideNavigationBar(){
        if(navigationBarShowing()){
            TransitionManager.beginDelayedTransition(parentLayout, bottomBarTransition);
            bottomBarHiddenConstraints.applyTo(parentLayout);
        }
    }

    protected final void showNavigationBar(){
        if(!navigationBarShowing()){
            TransitionManager.beginDelayedTransition(parentLayout, bottomBarTransition);
            bottomBarShowingConstraints.applyTo(parentLayout);
        }
    }

}