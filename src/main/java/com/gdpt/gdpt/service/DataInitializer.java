package com.gdpt.gdpt.service;

import com.gdpt.gdpt.model.Utilisateur.Role;
import com.gdpt.gdpt.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialise la base de données au démarrage.
 * Seul le compte administrateur est créé — aucune donnée de démo.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;

    public DataInitializer(UtilisateurService utilisateurService,
                           UtilisateurRepository utilisateurRepository) {
        this.utilisateurService = utilisateurService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Création du compte admin si la base est vide (idempotent)
        if (utilisateurRepository.count() == 0) {
            System.out.println("====== INITIALISATION : création du compte administrateur ======");
            utilisateurService.registerNewUser("admin", "admin@gdpt.com", "adminpass", Role.ROLE_ADMIN);
            System.out.println("====== Compte admin créé : admin / admin@gdpt.com ======");
        }
    }
}

