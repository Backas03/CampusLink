package moe.vitamin.campuslink.config.impl;

import lombok.Getter;
import moe.vitamin.campuslink.config.yaml.YamlConfig;

import java.io.File;

@Getter
public class ClassListConfig extends YamlConfig {

    private String commandPrefix;
    private String commandPermission;
    private String description;

    public ClassListConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();

        commandPrefix = getNode("class-list").getString("command-prefix");
        commandPermission = getNode("class-list").getString("command-permission");
        description = getNode("class-list").getString("description");
    }
}
