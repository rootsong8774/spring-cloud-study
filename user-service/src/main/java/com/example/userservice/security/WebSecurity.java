package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {
    
    private final Environment environment;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        
        http.csrf().disable();
//        http.authorizeRequests().antMatchers("/user/**").permitAll();
        http.authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.authorizeRequests().antMatchers("/**")
            .hasIpAddress("121.130.182.119")
            .and()
            .addFilter(getAuthenticationFilter());
        http.headers().frameOptions().disable();
    }
    
    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(userService, environment);
        filter.setAuthenticationManager(authenticationManager());
        
        return filter;
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }
}
