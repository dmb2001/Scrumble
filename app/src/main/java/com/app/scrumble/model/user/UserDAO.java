package com.app.scrumble.model.user;

import java.util.List;
import java.util.Set;

public interface UserDAO {

    /**
     * Creates a record in the underlying persistence mechanism using the provided {@link User}
     * @param user A {@link User} object representing the entry to be created by the underlying persistence mechanism.
     */
    void create(User user);

    /**
     * @param userID the unique ID of the {@link User}
     * @return the {@link User} corresponding to the given ID, or null if such a user does not exist
     */
    User queryUserByID(long userID);

    /**
     * @param username the username of the {@link User}
     * @return The {@link User} corresponding to the given username, or null if such a user does not exist
     */
    User queryUserByUsername(String username);

    /**
     * @param user A user object populated with the details that should be used to update the corresponding entry in the
     *             underlying persistence mechanism. If no such user exists, then no action is taken
     */
    void updateUser(User user);

    /**
     * deletes the user with the given ID, if it exists
     * @param userID the unique ID of the {@link User} to be deleted from the underlying persistence mechanism.
     */
    void deleteUser(long userID);

    /**
     * @param user the user for whom the list should be returned.
     * @return The list of users that are followed by the provided @{@link User} or an empty list if the user is following no other users.
     */
    List<User> getFollowing(User user);


}
