package moe.vitamin.campuslink.config.impl;

import lombok.Getter;
import moe.vitamin.campuslink.config.yaml.YamlConfig;
import moe.vitamin.campuslink.config.yaml.YamlNode;

import java.io.File;

@Getter
public class EmailConfig extends YamlConfig {

    private String host;
    private int port;
    private boolean auth;

    private String credentialEmail;
    private String appPassword;

    private Boolean startTLSEnabled;

    public EmailConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();

        YamlNode smtpNode = getNode("SMTP");

        this.host = smtpNode.getString("host");
        this.port = smtpNode.getInt("port");
        this.auth = smtpNode.getBooleanOrDefault("auth", true);

        YamlNode credentialNode = smtpNode.getNode("credential");
        this.credentialEmail = credentialNode.getString("email");
        this.appPassword = credentialNode.getString("app-password");

        if (smtpNode.contains("start-tls")) {
            startTLSEnabled = smtpNode.getNode("start-tls").getBooleanOrDefault("enabled", true);
        }
    }
}
