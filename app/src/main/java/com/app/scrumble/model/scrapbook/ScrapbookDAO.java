package com.app.scrumble.model.scrapbook;

import com.app.scrumble.model.user.User;

import java.util.List;
import java.util.Set;

public interface ScrapbookDAO {

    /**
     * @param id the ID of the required scrapbook
     * @return the scrapbook corresponding to the provided ID, or null if no such scrapbook exists
     */
    Scrapbook queryScrapbookByID(long id);

    /**
     * Query all scrapbooks within a given radius from a given latitude/longitude.
     * @param start The starting location
     * @param maxDistance the maximum radius
     * @return the set of {@link Scrapbook} objects that fall within the given radius, or null if there are no such scrapbooks
     */
    Set<Scrapbook> queryScrapbooksByLocation(Location start, long maxDistance);

    /**
     * Query all scrapbooks belonging to the given {@link User}
     * @param user the {@link User} whose {@link Scrapbook Scrapbooks} should be returned
     * @return All {@link Scrapbook Scrapbooks} belonging to the given {@link User}
     */
    Set<Scrapbook> queryScrapbooksByUser(User user);

    /**
     * Create a new {@link Scrapbook}
     * @param scrapbook A {@link Scrapbook} containing the values to use when creating the new record in the persistence mechanism
     */
    void createScrapbook(Scrapbook scrapbook);

    /**
     * deletes the scrapbook with the given id, if it exists
     * @param id the unique ID of the scrapbook to be deleted
     */
    void deleteScrapbook(long id);

    /**
     * Crete a scrapbook entry
     * @param entry An {@link Entry} object containing the values to use when creating the record
     * @param scrapbookID the ID of the {@link Scrapbook} with which this entry is associated
     */
    void createEntry(Entry entry, long scrapbookID);

    /**
     * Create a comment
     * @param comment A {@link Comment} object containing the values to use when creating the record
     * @param scrapbookID the ID of the {@link Scrapbook} with which this comment is associated
     */
    void createComment(Comment comment, long scrapbookID, Long parentCommentID);

}
