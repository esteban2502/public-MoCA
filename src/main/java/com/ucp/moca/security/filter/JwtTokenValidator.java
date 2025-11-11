package com.ucp.moca.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ucp.moca.Util.JwtUtils;
import com.ucp.moca.entity.UserEntity;
import com.ucp.moca.repository.UserEntityRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;


public class JwtTokenValidator extends OncePerRequestFilter {

    private JwtUtils jwtUtils;
    private UserEntityRepository userEntityRepository;

    public JwtTokenValidator(JwtUtils jwtUtils, UserEntityRepository userEntityRepository){
        this.jwtUtils = jwtUtils;
        this.userEntityRepository = userEntityRepository;
    }




   @Override
    protected void doFilterInternal (@NotNull HttpServletRequest request,
                                     @NotNull HttpServletResponse response,
                                     @NotNull FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(jwtToken != null && jwtToken.startsWith("Bearer ")){
            try {
                jwtToken = jwtToken.substring(7);

                DecodedJWT decodedJWT = jwtUtils.validateToken(jwtToken);

                String username = jwtUtils.extractUsername(decodedJWT);

                // Buscar el UserEntity completo por email
                UserEntity userEntity = userEntityRepository.findUserEntityByEmail(username).orElse(null);

                if (userEntity != null) {
                    String stringAuthorities = jwtUtils.getSpecificClaim(decodedJWT,"authorities").asString();
                    Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(stringAuthorities);

                    SecurityContext context = SecurityContextHolder.getContext();

                    // Usar el UserEntity completo como principal
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null, authorities);

                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                    
                    System.out.println("✅ Usuario autenticado: " + userEntity.getFullName() + " (ID: " + userEntity.getId() + ")");
                } else {
                    System.err.println("❌ Usuario no encontrado en BD: " + username);
                }

            } catch (Exception e) {
                System.err.println("❌ Error validando token: " + e.getMessage());
            }
        }

        filterChain.doFilter(request,response);

    }

}