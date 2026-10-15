package com.gdpt.gdpt.service;

import com.gdpt.gdpt.model.GuideDetails;
import com.gdpt.gdpt.model.Utilisateur;
import com.gdpt.gdpt.model.Utilisateur.Role;
import com.gdpt.gdpt.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Utilisateur registerNewUser(String username, String email, String password, Role role) {
        return registerNewUser(username, email, password, role, null, null);
    }

    public Utilisateur registerNewUser(String username, String email, String password, Role role, String numeroLicence, String biographie) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("L'adresse email n'est pas au format valide.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }
        if (utilisateurRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris.");
        }
        if (utilisateurRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cette adresse email est déjà enregistrée.");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        if (role == Role.ROLE_GUIDE) {
            if (numeroLicence == null || numeroLicence.trim().isEmpty()) {
                throw new IllegalArgumentException("Le numéro de licence est obligatoire pour les guides.");
            }
            GuideDetails details = GuideDetails.builder()
                    .user(utilisateur)
                    .numeroLicence(numeroLicence.trim())
                    .biographie(biographie)
                    .estVerifie(false)
                    .noteMoyenne(0.0)
                    .build();
            utilisateur.setGuideDetails(details);
        }

        return utilisateurRepository.save(utilisateur);
    }

    public Optional<Utilisateur> findByUsername(String username) {
        return utilisateurRepository.findByUsername(username);
    }
}
