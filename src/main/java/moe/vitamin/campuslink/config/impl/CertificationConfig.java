package moe.vitamin.campuslink.config.impl;

import lombok.Getter;
import moe.vitamin.campuslink.config.yaml.YamlConfig;
import moe.vitamin.campuslink.config.yaml.YamlNode;

import java.io.File;

@Getter
public class CertificationConfig extends YamlConfig {

    private String emailSubject;
    private String plainHTMLMessage;

    private long verificationExpireTimeMs;
    private long processTimeoutThreshold;

    public CertificationConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();

        YamlNode emailNode = getNode("email");
        this.emailSubject = emailNode.getString("subject");
        this.plainHTMLMessage = emailNode.getString("html");

        YamlNode certificationNode = getNode("certification");
        this.verificationExpireTimeMs = certificationNode.getLongOrDefault("expire-after", 3 * 60 * 1000L);
        this.processTimeoutThreshold = certificationNode.getLongOrDefault("process-timeout-threshold", 5 * 1000L);
    }
}
