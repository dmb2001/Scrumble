package com.app.scrumble.model.group;

import java.util.Set;

public interface GroupDAO {

    Set<Group> queryGroupsContainingScrapbookID(long scrapbookID);

}
