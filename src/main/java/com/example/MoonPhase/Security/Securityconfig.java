package com.example.MoonPhase.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.MoonPhase.Model.AppUsuarioService;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class Securityconfig {
  
  @Autowired
  private final AppUsuarioService appUserService;

  @Autowired
  private final CustomSuccessHandler customSuccessHandler;

  @Bean
  public UserDetailsService userDetailsService(){
    return appUserService;
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider(){
      DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
      provider.setUserDetailsService(appUserService);
      provider.setPasswordEncoder(passwordEncoder());
      return provider;
  }


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity
            .csrf(AbstractHttpConfigurer:: disable)
            .headers(headers->headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .authorizeHttpRequests(registry -> {
                registry.requestMatchers("/login", "/css/**", "/js/**").permitAll(); // ← Agregar /login
                registry.anyRequest().authenticated();
            })
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                //.defaultSuccessUrl("/index", true)
                .successHandler(customSuccessHandler)
            )

            .logout(logout -> logout
              .permitAll()
              // Redirige a la página de login con un parámetro para mostrar un mensaje si es necesario
              .logoutSuccessUrl("/login?logout") 
            )
            .build();
  }
  
}


