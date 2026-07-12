package ge.freeuni.informatics.common.model.user;

/**
 * Roles are stored in {@code principal.role} as a comma-separated list (see {@link #hasRole}).
 */
public enum UserRole {
    STUDENT,
    TEACHER,
    ADMIN,
    WORKER;

    /**
     * True if {@code rolesCsv} contains {@code role} as a whole token (comma-separated), not as a substring.
     */
    public static boolean hasRole(String rolesCsv, UserRole role) {
        if (rolesCsv == null || role == null) {
            return false;
        }
        String expected = role.name();
        for (String token : rolesCsv.split(",")) {
            if (expected.equals(token.trim())) {
                return true;
            }
        }
        return false;
    }
}
