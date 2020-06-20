package su.nepom.cash.server.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

/**
 * Пользователь приложения.
 * <p>Создается и меняется только в серверной БД
 */
@Data
@Accessors(chain = true)
@Entity
@Table(name = "appl_user")
@NoArgsConstructor
public class User implements UserDetails {
    public final static String NONEMPTY_PASSWORD = "******";
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @Column(name = "is_child")
    private boolean child; // ребенок - права ограничены
    private String password; // хэш c префиксом (см DelegatingPasswordEncoder)
    private boolean enabled;

    public User(long id) {
        this.id = id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return child ? List.of(Role.CHILD) : List.of(Role.PARENT);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
