package com.canal.canalmadash.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer "))
                ? header.substring(7)
                : request.getParameter("token");

        if (token != null) {
            jwtService.verifier(token).ifPresent(claims -> authentifier(claims));
        }
        chain.doFilter(request, response);
    }

    private void authentifier(Claims claims) {
        String role = claims.get("role", String.class);

        List<GrantedAuthority> autorites = new ArrayList<>();
        autorites.add(new SimpleGrantedAuthority("ROLE_" + role));

        Object codes = claims.get("fonctionnalites");
        if (codes instanceof List<?> liste) {
            for (Object code : liste) {
                autorites.add(new SimpleGrantedAuthority("FONC_" + code));
            }
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null, autorites);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}