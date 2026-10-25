package org.example.common;

/**
 * 系统角色。
 */
public enum Role {

    MANAGER("管理者"),
    HR("一般用户"),
    EMPLOYEE("个人用户");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Role of(String value) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw BusinessException.badRequest("未知角色: " + value);
    }
}
