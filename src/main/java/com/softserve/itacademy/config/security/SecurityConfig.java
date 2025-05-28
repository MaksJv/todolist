package com.softserve.itacademy.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;


    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(c -> {
                    c.loginPage("/login");
                    c.failureUrl("/login?error=true");
                })
                .httpBasic(withDefaults())
                .authenticationProvider(authenticationProvider);

        http.logout(c -> {
            c.logoutUrl("/logout");
            c.logoutSuccessUrl("/login");
            c.invalidateHttpSession(true);
            c.deleteCookies("JSESSIONID");
        });

        http.exceptionHandling(customizer -> customizer
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))


                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                })
        );

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("error").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/users/create").permitAll()
                .requestMatchers("/").permitAll()
                .anyRequest().authenticated());

        return http.build();
    }
}
