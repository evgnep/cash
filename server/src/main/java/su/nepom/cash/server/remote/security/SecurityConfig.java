package su.nepom.cash.server.remote.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import su.nepom.cash.server.domain.Role;

import java.util.UUID;

@Configuration
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final String adminPassword;
    private final UserDetailsService userDetailsService;
    private final String PARENT = Role.PARENT.name(), CHILD = Role.CHILD.name();

    public SecurityConfig(@Value("${cash.adminPassword:}") String adminPassword,
                          @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        if (adminPassword.isEmpty())
            adminPassword = UUID.randomUUID().toString();
        this.adminPassword = adminPassword;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/swagger-ui/**", "/swagger/**", "/v3/api-docs/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .httpBasic().and()
                .authorizeRequests(a -> a
                        //- пользователи
                        .antMatchers(HttpMethod.GET, "/api/user/**").hasAnyRole(CHILD, PARENT)
                        .antMatchers(HttpMethod.PUT, "/api/user/*").hasAnyRole(CHILD, PARENT) // доп проверки на уровне метода
                        .antMatchers("/api/user/**").hasRole(PARENT)
                        //- счета
                        .antMatchers(HttpMethod.GET, "/api/currency/**").hasAnyRole(CHILD, PARENT)
                        .antMatchers("/api/currency/**").hasRole(PARENT)
                        //- группы счетов
                        .antMatchers(HttpMethod.GET, "/api/account-group/**").hasAnyRole(CHILD, PARENT) // доп проверки на уровне метода
                        .antMatchers("/api/account-group/**").hasRole(PARENT)
                        //- счета
                        .antMatchers(HttpMethod.GET, "/api/account/**").hasAnyRole(CHILD, PARENT) // доп проверки на уровне метода
                        .antMatchers("/api/account/**").hasRole(PARENT)
                        //- проводки
                        .antMatchers("/api/record/**").hasAnyRole(CHILD, PARENT) // доп проверки на уровне методов
                        //- работа с чеками
                        .antMatchers("/api/cheque/**").permitAll()
                        //--
                        .anyRequest().denyAll()
                );
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.warn("\n\n'admin' password: {}\n", adminPassword);
        auth
                .inMemoryAuthentication().withUser("admin").password("{noop}" + adminPassword).roles(PARENT)
                .and().and()
                .userDetailsService(userDetailsService);
    }
}
