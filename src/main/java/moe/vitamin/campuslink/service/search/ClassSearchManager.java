package moe.vitamin.campuslink.service.search;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.impl.ClassSearchSlashCommand;
import moe.vitamin.campuslink.service.search.database.ClassSearchDao;

@Slf4j
public class ClassSearchManager {

    private final ClassSearchDao classSearchDao;

    public static ClassSearchManager init() {
        ClassSearchDao.init();
        return new ClassSearchManager();
    }

    private ClassSearchManager() {
        this.classSearchDao = new ClassSearchDao();
        CampusLink.getInstance()
                .getSora()
                .getCommandManager()
                .registerSlashCommand(new ClassSearchSlashCommand(this));
    }
}