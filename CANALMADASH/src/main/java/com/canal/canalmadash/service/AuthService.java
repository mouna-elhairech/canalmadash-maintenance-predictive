package com.canal.canalmadash.service;

import com.canal.canalmadash.entity.FonctionnaliteApplication;
import com.canal.canalmadash.entity.Utilisateur;
import com.canal.canalmadash.repository.UtilisateurRepository;
import com.canal.canalmadash.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    public record LoginResult(String token, String nom, String email, String role,
                              List<String> fonctionnalites) {}

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public Optional<LoginResult> login(String email, String motDePasse) {
        if (email == null || motDePasse == null) {
            return Optional.empty();
        }
        return repository.findByEmail(email.trim().toLowerCase())
                .filter(Utilisateur::isActif)
                .filter(u -> passwordEncoder.matches(motDePasse, u.getMotDePasseHash()))
                .map(u -> {
                    String role = u.getRole().getCode();
                    List<String> fonctionnalites = u.getRole().getFonctionnalites().stream()
                            .filter(FonctionnaliteApplication::isActif)
                            .map(FonctionnaliteApplication::getCode)
                            .toList();
                    return new LoginResult(
                            jwtService.generer(u.getEmail(), role, fonctionnalites),
                            u.getNom(),
                            u.getEmail(),
                            role,
                            fonctionnalites);
                });
    }
}