package su.nepom.cash.server.remote.crud;

import org.springframework.security.core.context.SecurityContextHolder;
import su.nepom.cash.server.domain.Role;

import java.util.function.Function;

public class SecurityUtils {
    private static final String CHILD = Role.CHILD.getAuthority();

    public static String childName() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        for (var auth : authentication.getAuthorities())
            if (auth.getAuthority().equals(CHILD))
                return authentication.getName();
        return null;
    }

    public static boolean isChild() {
        return childName() != null;
    }

    public static void ifChild(Function<String, Boolean> canProcessByChild) {
        var name = childName();
        if (name != null && !canProcessByChild.apply(name))
            throw new ForbiddenException();
    }
}
