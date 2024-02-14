package com.orange.dependencytrackprovisioning.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.DefaultSecurityFilterChain;

import java.util.Collections;

@Configuration
public class SecurityConfig {

    @Bean
    DefaultSecurityFilterChain springSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/index.html").permitAll()
                        .anyRequest().authenticated());
        // Force userInfo query for oidc
        OidcUserService oidc = new OidcUserService();
        oidc.setAccessibleScopes(Collections.emptySet());
        http.oauth2Login(oauth -> oauth.defaultSuccessUrl("/")
                .userInfoEndpoint(user -> user.oidcUserService(oidc)));

        return http.build();
    }

}
