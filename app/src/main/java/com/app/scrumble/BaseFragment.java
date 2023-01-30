package com.app.scrumble;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import com.app.scrumble.model.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class BaseFragment extends Fragment {

    private static final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    protected void setCurrentUser(User user){
        ((MainActivity)getActivity()).setCurrentUser(user);
    }

    protected User getCurrentUser(){
        return ((MainActivity)getActivity()).getCurrentUser();
    }

    protected final void showAsMainContent(Fragment fragment, boolean addTransactionToBackStack){
        ((MainActivity)getActivity()).showAsMainContent(fragment, addTransactionToBackStack);
    }

    protected final void showAsNavigationContent(Fragment fragment){
        ((MainActivity)getActivity()).showAsNavigation(fragment);
    }

    protected final UserDAO getUserDAO(){
        return ((MainActivity)getActivity()).getUserDAO();
    }

    protected final ScrapbookDAO getScrapBookDAO(){
        return ((MainActivity)getActivity()).getScrapBookDAO();
    }

    protected void hideKeyBoard(View view){
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void popBackStack(){
        ((MainActivity)getActivity()).popBackStack();
    }

    protected void popEntireBackStack(){
        ((MainActivity)getActivity()).popEntireBackStack();
    }

    protected void hideNavigationBar(){
        ((MainActivity)getActivity()).hideNavigationBar();
    }

    protected void runInBackground(Runnable runnable){
        backgroundExecutor.execute(runnable);
    }

    protected void runOnUIThread(Runnable runnable){
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    protected final boolean isSafe(){
        return !isRemoving() && (getActivity() != null) && !isDetached() && isAdded() && getView() != null;
    }

    public abstract String name();

}
