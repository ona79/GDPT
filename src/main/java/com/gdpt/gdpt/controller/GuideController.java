package com.gdpt.gdpt.controller;

import com.gdpt.gdpt.model.Avis;
import com.gdpt.gdpt.model.Evenement;
import com.gdpt.gdpt.model.Utilisateur;
import com.gdpt.gdpt.service.AvisService;
import com.gdpt.gdpt.service.EvenementService;
import com.gdpt.gdpt.service.GuideService;
import com.gdpt.gdpt.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/guide")
public class GuideController {

    private final GuideService guideService;
    private final AvisService avisService;
    private final EvenementService evenementService;
    private final UtilisateurService utilisateurService;

    public GuideController(GuideService guideService,
                           AvisService avisService,
                           EvenementService evenementService,
                           UtilisateurService utilisateurService) {
        this.guideService = guideService;
        this.avisService = avisService;
        this.evenementService = evenementService;
        this.utilisateurService = utilisateurService;
    }

    private Utilisateur getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new SecurityException("Utilisateur non connecté.");
        }
        return utilisateurService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + principal.getName()));
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        Utilisateur user = getAuthenticatedUser(principal);
        
        // Fetch Guide details
        model.addAttribute("guideDetails", user.getGuideDetails());
        model.addAttribute("user", user);

        // Fetch all events for this guide
        List<Evenement> allGuideEvents = evenementService.getAllEvents().stream()
                .filter(e -> e.getGuide() != null && e.getGuide().getId().equals(user.getId()))
                .collect(Collectors.toList());

        // Agenda of today
        LocalDate today = LocalDate.now();
        List<Evenement> todayEvents = allGuideEvents.stream()
                .filter(e -> e.getDate().toLocalDate().isEqual(today))
                .collect(Collectors.toList());

        // Reviews for this guide
        List<Avis> avisList = avisService.getAvisForGuide(user.getId());

        model.addAttribute("todayEvents", todayEvents);
        model.addAttribute("allEvents", allGuideEvents);
        model.addAttribute("avisList", avisList);

        return "guide/dashboard";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String biographie, Principal principal) {
        Utilisateur user = getAuthenticatedUser(principal);
        try {
            guideService.updateBio(user.getId(), biographie);
            return "redirect:/guide/dashboard?success=" + java.net.URLEncoder.encode("Profil mis à jour avec succès.", java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/guide/dashboard?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/avis/reply")
    public String replyToAvis(@RequestParam Long avisId, @RequestParam String reponse, Principal principal) {
        Utilisateur user = getAuthenticatedUser(principal);
        try {
            avisService.replyToAvis(avisId, user, reponse);
            return "redirect:/guide/dashboard?success=" + java.net.URLEncoder.encode("Réponse enregistrée avec succès.", java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/guide/dashboard?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/avis/report")
    public String reportAvis(@RequestParam Long avisId, @RequestParam String motif, Principal principal) {
        getAuthenticatedUser(principal); // Just check connection
        try {
            avisService.reportAvis(avisId, motif);
            return "redirect:/guide/dashboard?success=" + java.net.URLEncoder.encode("L'avis a été signalé à la modération.", java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/guide/dashboard?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
