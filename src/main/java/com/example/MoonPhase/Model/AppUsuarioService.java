package com.example.MoonPhase.Model;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AppUsuarioService implements UserDetailsService {
  
  @Autowired
  private AppUsuarioRepository repository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    
    Optional<AppUsuario> user =repository.findByNombreUsuario(username);
    if(user.isPresent()){
        var userObj = user.get();
        return User.builder()
              .username(userObj.getNombreUsuario())
              .password(userObj.getClave())
              .build();
    }else {
        throw new UsernameNotFoundException(username);
    }
       
  }
  

}
