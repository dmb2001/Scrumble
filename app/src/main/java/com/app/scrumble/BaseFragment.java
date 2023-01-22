package com.app.scrumble;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import com.app.scrumble.model.Comment;
import com.app.scrumble.model.Group;
import com.app.scrumble.model.Location;
import com.app.scrumble.model.Scrapbook;
import com.app.scrumble.model.User;

import java.util.List;

public abstract class BaseFragment extends Fragment {

    Location currentLocation = new Location(55.9476, -3.1467);

    protected final void showAsMainContent(Fragment fragment, boolean addTransactionToBackStack){
        ((MainActivity)getActivity()).showAsMainContent(fragment, addTransactionToBackStack);
    }

    protected final void showAsNavigationContent(Fragment fragment){
        ((MainActivity)getActivity()).showAsNavigation(fragment);
    }

    protected final List<Scrapbook> getScrapbooks(){
        return ((MainActivity)getActivity()).getScrapbooks();
    }

    protected Location getCurrentLocation(){
        return currentLocation;
    }

    protected Scrapbook getScrapbookByID(long ID){
        return ((MainActivity)getActivity()).getScrapbookByID(ID);
    }

    protected void hideKeyBoard(View view){
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected List<User> getUsers() {
        return ((MainActivity)getActivity()).getUsers();
    }

    protected User getUserByUsername(String username){
        return ((MainActivity)getActivity()).getUsersByUsername(username);
    }

    protected void popBackStack(){
        ((MainActivity)getActivity()).popBackStack();
    }

    protected void popEntireBackStack(){
        ((MainActivity)getActivity()).popEntireBackStack();
    }

    protected final List<Comment> getComments(){
        return ((MainActivity)getActivity()).getComments();
    }

    protected final Comment getCommentByResourceID(int resourceID){
        return ((MainActivity)getActivity()).getCommentByResourceID(resourceID);
    }

    protected final User getCurrentUser(){
        return ((MainActivity)getActivity()).getCurrentUser();
    }

    protected void setCurrentUser(User currentUser){
        ((MainActivity)getActivity()).setCurrentUser(currentUser);
    }

    protected void hideNavigationBar(){
        ((MainActivity)getActivity()).hideNavigationBar();
    }

    public abstract String name();

    protected final List<Group> getGroups(){
        return ((MainActivity)getActivity()).getGroups();
    }

    protected final Group getGroupByName(String name){
        return ((MainActivity)getActivity()).getGroupByName(name);
    }


}
