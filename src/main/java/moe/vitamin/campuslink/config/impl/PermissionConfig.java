package moe.vitamin.campuslink.config.impl;

import lombok.Getter;
import moe.vitamin.campuslink.config.yaml.YamlConfig;
import moe.vitamin.campuslink.config.yaml.YamlNode;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PermissionConfig extends YamlConfig {

    private Map<String, RoleData> roles;

    public PermissionConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        super.load();

        this.roles = new HashMap<>();
        YamlNode rolesNode = getRoot().getNode("roles");
        Object rolesObj = rolesNode.get();
        if (rolesObj instanceof Map) {
            Map<?, ?> rolesMap = (Map<?, ?>) rolesObj;
            for (Object roleNameObj : rolesMap.keySet()) {
                String roleName = roleNameObj.toString();
                YamlNode roleNode = rolesNode.getNode(roleName);
                List<String> permissions = roleNode.getStringList("permissions");
                List<String> users = roleNode.getStringList("users");

                if (permissions == null) permissions = Collections.emptyList();
                if (users == null) users = Collections.emptyList();

                this.roles.put(roleName, new RoleData(permissions, users));
            }
        }
    }

    @Getter
    public static class RoleData {
        private final List<String> permissions;
        private final List<String> users;

        public RoleData(List<String> permissions, List<String> users) {
            this.permissions = permissions;
            this.users = users;
        }
    }
}
