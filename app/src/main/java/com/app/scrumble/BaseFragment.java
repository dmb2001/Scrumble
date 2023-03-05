package com.app.scrumble;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import com.app.scrumble.model.group.GroupDAO;
import com.app.scrumble.model.group.scrapbook.ScrapbookDAO;
import com.app.scrumble.model.user.User;
import com.app.scrumble.model.user.UserDAO;

import java.util.UUID;
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

    protected final GroupDAO getGroupDAO() {
        return ((MainActivity)getActivity().getGroupDAO());
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

    /**
     * Runs the provided {@link Runnable} on a background thread. Use this method whenever there may be work to be done that would block the UI thread.
     * For example, disk I/O or network activity would block the UI thread and impact the user experience, so those tasks should be run in the background.
     * Note that on the Android platform, you should never update the UI thread from a background thread or the application wil crash. Once the background work is done, call {@link #runOnUIThread(Runnable)}
     * to update the UI.
     * @param runnable A {@link Runnable} containing the code to be executed on a background thread.
     */
    protected void runInBackground(Runnable runnable){
        backgroundExecutor.execute(runnable);
    }

    /**
     * Used to run code on the UI thread.
     * @param runnable The code to be executed on the UI thread
     */
    protected void runOnUIThread(Runnable runnable){
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    protected final boolean isSafe(){
        return !isRemoving() && (getActivity() != null) && !isDetached() && isAdded() && getView() != null;
    }

    protected final long newUUID(){
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    public abstract String name();

}
