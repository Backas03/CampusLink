package moe.vitamin.campuslink.config.impl;

import lombok.Getter;
import moe.vitamin.campuslink.config.yaml.YamlConfig;

import java.io.File;

@Getter
public class SoraConfig extends YamlConfig {

    private String token;
    private String chatCommandPrefix;

    public SoraConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();

        this.token = getNode("discord").getString("bot-token");
        this.chatCommandPrefix = getNode("command")
                .getNode("chat")
                .getStringOrDefault("prefix", "!");
    }
}
