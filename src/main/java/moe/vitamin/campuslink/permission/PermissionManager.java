package moe.vitamin.campuslink.permission;

import moe.vitamin.campuslink.config.impl.PermissionConfig;
import net.dv8tion.jda.api.entities.User;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionManager {

    private final Map<String, Set<String>> userPermissions;

    public PermissionManager() {
        this.userPermissions = new ConcurrentHashMap<>();
    }

    public void reload(PermissionConfig config) {
        this.userPermissions.clear();

        Map<String, PermissionConfig.RoleData> roles = config.getRoles();
        if (roles != null) {
            for (PermissionConfig.RoleData role : roles.values()) {
                List<String> permissions = role.getPermissions();
                List<String> users = role.getUsers();
                if (permissions == null || users == null) {
                    continue;
                }
                for (String userId : users) {
                    userPermissions.computeIfAbsent(userId, k -> new HashSet<>()).addAll(permissions);
                }
            }
        }
    }

    public boolean hasPermission(User user, String permissionNode) {
        if (user == null || permissionNode == null) {
            return false;
        }

        String userId = user.getId();

        System.out.println("userId " + userId + " permissionNode " + permissionNode);

        Set<String> permissions = userPermissions.get(userId);
        System.out.println("permissions \n" + permissions);
        if (permissions == null) {
            return false;
        }

        if (permissions.contains(permissionNode) || permissions.contains("*")) {
            return true;
        }

        String[] parts = permissionNode.split("\\.");
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                current.append(".");
            }
            current.append(parts[i]);
            if (permissions.contains(current + ".*")) {
                return true;
            }
        }

        return false;
    }
}
