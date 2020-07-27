package su.nepom.cash.server.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    PARENT,
    CHILD,
    ;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
