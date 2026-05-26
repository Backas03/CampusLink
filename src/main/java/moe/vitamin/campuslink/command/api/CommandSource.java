package moe.vitamin.campuslink.command.api;

import org.jetbrains.annotations.Nullable;

public interface CommandSource {

    @Nullable String getDescription();

    @Nullable String getUsage();

    @Nullable default String getPermissionNode() {
        return null;
    }

}
