package com.gdpt.gdpt.controller;

import com.gdpt.gdpt.model.Avis;
import com.gdpt.gdpt.model.Evenement;
import com.gdpt.gdpt.model.SiteTouristique;
import com.gdpt.gdpt.model.SiteTouristique.Categorie;
import com.gdpt.gdpt.model.Utilisateur.Role;
import com.gdpt.gdpt.service.AvisService;
import com.gdpt.gdpt.service.EvenementService;
import com.gdpt.gdpt.service.SiteTouristiqueService;
import com.gdpt.gdpt.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PublicController {

    private final SiteTouristiqueService siteTouristiqueService;
    private final EvenementService evenementService;
    private final UtilisateurService utilisateurService;
    private final AvisService avisService;

    public PublicController(SiteTouristiqueService siteTouristiqueService,
                            EvenementService evenementService,
                            UtilisateurService utilisateurService,
                            AvisService avisService) {
        this.siteTouristiqueService = siteTouristiqueService;
        this.evenementService = evenementService;
        this.utilisateurService = utilisateurService;
        this.avisService = avisService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String query,
                       @RequestParam(required = false) Categorie categorie,
                       @RequestParam(required = false) Double userLat,
                       @RequestParam(required = false) Double userLng,
                       Model model) {
        List<SiteTouristique> sites = siteTouristiqueService.searchSitesProximity(query, categorie, userLat, userLng);
        
        // Calculate average note and distances for each site
        Map<Long, Double> ratings = new HashMap<>();
        Map<Long, Double> distances = new HashMap<>();
        for (SiteTouristique site : sites) {
            ratings.put(site.getId(), siteTouristiqueService.getAverageRating(site.getId()));
            if (userLat != null && userLng != null && site.getLatitude() != null && site.getLongitude() != null) {
                double dist = siteTouristiqueService.calculateDistance(userLat, userLng, site.getLatitude(), site.getLongitude());
                distances.put(site.getId(), Math.round(dist * 10.0) / 10.0);
            }
        }

        model.addAttribute("sites", sites);
        model.addAttribute("ratings", ratings);
        model.addAttribute("distances", distances);
        model.addAttribute("categories", Categorie.values());
        model.addAttribute("selectedQuery", query);
        model.addAttribute("selectedCategorie", categorie);
        model.addAttribute("userLat", userLat);
        model.addAttribute("userLng", userLng);
        return "index";
    }

    @GetMapping("/sites/details/{id}")
    public String siteDetails(@PathVariable Long id, Model model) {
        SiteTouristique site = siteTouristiqueService.getSiteById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site touristique non trouvé: " + id));
        
        List<Evenement> upcomingEvents = evenementService.getUpcomingEventsForSite(id);
        List<Avis> evaluations = avisService.getAvisForSite(id);
        Double averageRating = siteTouristiqueService.getAverageRating(id);

        model.addAttribute("site", site);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("evaluations", evaluations);
        model.addAttribute("averageRating", averageRating);
        return "site-details";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("username", "");
        model.addAttribute("email", "");
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam String username,
                                     @RequestParam String email,
                                     @RequestParam String password,
                                     @RequestParam(required = false, defaultValue = "ROLE_VISITEUR") String role,
                                     @RequestParam(required = false) String numeroLicence,
                                     @RequestParam(required = false) String biographie,
                                     Model model) {
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            model.addAttribute("error", "Tous les champs obligatoires doivent être remplis.");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }

        try {
            Role userRole = Role.valueOf(role);
            utilisateurService.registerNewUser(username.trim(), email.trim(), password, userRole, numeroLicence, biographie);
            
            if (userRole == Role.ROLE_GUIDE) {
                return "redirect:/login?registered=guide";
            }
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("role", role);
            model.addAttribute("numeroLicence", numeroLicence);
            model.addAttribute("biographie", biographie);
            return "register";
        }
    }
}
