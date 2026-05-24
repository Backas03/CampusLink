package moe.vitamin.campuslink.config.impl;

import lombok.Getter;
import moe.vitamin.campuslink.config.yaml.YamlConfig;
import moe.vitamin.campuslink.config.yaml.YamlNode;

import java.io.File;

@Getter
public class CertificationConfig extends YamlConfig {

    private String emailSubject;
    private String plainHTMLMessage;

    public CertificationConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();

        YamlNode emailNode = getNode("email");
        this.emailSubject = emailNode.getString("subject");
        this.plainHTMLMessage = emailNode.getString("html");
    }
}
