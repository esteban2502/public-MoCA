package com.ucp.moca.service.implement;


import com.ucp.moca.Util.JwtUtils;
import com.ucp.moca.dto.AuthCreateUserRequest;
import com.ucp.moca.dto.AuthLoginRequest;
import com.ucp.moca.dto.AuthResponse;
import com.ucp.moca.entity.RoleEntity;
import com.ucp.moca.entity.UserEntity;
import com.ucp.moca.repository.RoleRepository;
import com.ucp.moca.repository.UserEntityRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailServiceImpl implements UserDetailsService {


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;


    @Autowired
    private UserEntityRepository repository;

    @Autowired
    private RoleRepository roleRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = repository.findUserEntityByEmail(email)
                .orElseThrow(()->  new UsernameNotFoundException("El usuario con el correo "+ email + " no existe" ));

        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();

        userEntity.getRoles()
                .forEach(role-> authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))));
        userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissionList().stream())
                .forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission.getName())));

        return new User(userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.isEnabled(),
                userEntity.isAccountNoExpired(),
                userEntity.isAccountNoLocked(),
                userEntity.isCredentialNoExpired(),
                authorityList);
    }

    public AuthResponse loginUser(@Valid AuthLoginRequest authLoginRequest){
        String username =authLoginRequest.email();
        String password = authLoginRequest.password();

        Authentication authentication = this.authenticate(username,password);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.createToken(authentication);

        Optional<UserEntity> userAux = repository.findUserEntityByEmail(username);

        Integer idUser = 0;
        if(userAux.isPresent()) {
            idUser = userAux.get().getId();
        }


        AuthResponse authResponse = new AuthResponse(idUser,username,"Usuario Registrado Correctamente",accessToken,true);

        return  authResponse;
    }

    public Authentication authenticate(String username, String password){
        UserDetails userDetails = this.loadUserByUsername(username);

        if(userDetails == null){
            throw  new BadCredentialsException("Email o Contraseña Incorrecta.");
        }

        if(!passwordEncoder.matches(password,userDetails.getPassword())){
            throw  new BadCredentialsException("Contraseña Incorrecta.");

        }

        return new UsernamePasswordAuthenticationToken(username,userDetails.getPassword(),userDetails.getAuthorities());
    }

    public AuthResponse createUser(AuthCreateUserRequest authCreateUserRequest){
        String name = authCreateUserRequest.name();
        String email = authCreateUserRequest.email();
        String password = authCreateUserRequest.password();
        String idNumber = authCreateUserRequest.idNumber();
        List<String> roleRequest = authCreateUserRequest.roleRequest().roleListName();

        Set<RoleEntity> roleEntitySet = roleRepository.findRoleEntitiesByRoleEnumIn(roleRequest)
                .stream().collect(Collectors.toSet());

        if(roleEntitySet.isEmpty()){
            throw new IllegalArgumentException("El rol especificado no exieste.");
        }

        UserEntity userEntity = UserEntity.builder()
                .fullName(name)
                .email(email)
                .idNumber(idNumber)
                .password(passwordEncoder.encode(password))
                .roles(roleEntitySet)
                .isEnabled(true)
                .accountNoLocked(true)
                .accountNoExpired(true)
                .credentialNoExpired(true)
                .build();

        UserEntity userCreated = repository.save(userEntity);

        ArrayList<SimpleGrantedAuthority> authorityList = new ArrayList<>();

        userCreated.getRoles().forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))));

        userCreated.getRoles()
                .stream()
                .flatMap(roleEntity -> roleEntity.getPermissionList().stream())
                .forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission.getName())));



        Authentication authentication = new UsernamePasswordAuthenticationToken(userCreated.getEmail(),userCreated.getPassword(),authorityList);

        String accessToken = jwtUtils.createToken(authentication);

        Optional<UserEntity> userAux = repository.findUserEntityByEmail(email);

        Integer idUser = 0;
        if(userAux.isPresent()) {
            idUser = userAux.get().getId();
        }



        AuthResponse authResponse =  new AuthResponse(idUser,userCreated.getEmail(),"Usuario creado correctamente",accessToken,true);

        return authResponse;

    }
}