package com.crud.rental.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        new AntPathRequestMatcher("/reservations/**"),
                        new AntPathRequestMatcher("/cars/**"),
                        new AntPathRequestMatcher("/users/**"),
                        new AntPathRequestMatcher("/options/**"),
                        new AntPathRequestMatcher("/damages/**"),
                        new AntPathRequestMatcher("/fuels/**")
                ).permitAll()
        );
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/reservations/**", "/cars/**", "/users/**", "/options/**", "/damages/**", "/fuels/**")
        );
        super.configure(http);
        setLoginView(http, "/login");
    }
}
