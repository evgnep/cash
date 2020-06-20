package su.nepom.cash.server.remote.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final String adminPassword;
    private final UserDetailsService userDetailsService;

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
        web.ignoring().antMatchers("/swagger-ui/**", "/swagger/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .httpBasic().and()
                .authorizeRequests(a -> a.anyRequest().authenticated());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.warn("\n\n'admin' password: {}\n", adminPassword);
        auth
                .inMemoryAuthentication().withUser("admin").password("{noop}" + adminPassword).roles("PARENT")
                .and().and()
                .userDetailsService(userDetailsService);
    }
}
