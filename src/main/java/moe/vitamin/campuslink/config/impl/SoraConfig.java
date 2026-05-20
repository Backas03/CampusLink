package moe.vitamin.campuslink.config.impl;

import lombok.Getter;
import moe.vitamin.campuslink.config.YamlConfig;

import java.io.File;

@Getter
public class SoraConfig extends YamlConfig {

    private String token;

    public SoraConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();

        this.token = getNode("discord").getString("bot-token");
    }
}
