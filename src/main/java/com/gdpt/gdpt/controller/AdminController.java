package com.gdpt.gdpt.controller;

import com.gdpt.gdpt.model.Evenement;
import com.gdpt.gdpt.model.SiteTouristique;
import com.gdpt.gdpt.model.SiteTouristique.Categorie;
import com.gdpt.gdpt.model.Utilisateur;
import com.gdpt.gdpt.repository.UtilisateurRepository;
import com.gdpt.gdpt.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final SiteTouristiqueService siteTouristiqueService;
    private final EvenementService evenementService;
    private final ReservationService reservationService;
    private final UtilisateurRepository utilisateurRepository;
    private final FileStorageService fileStorageService;
    private final GuideService guideService;
    private final AvisService avisService;

    public AdminController(SiteTouristiqueService siteTouristiqueService,
                            EvenementService evenementService,
                            ReservationService reservationService,
                            UtilisateurRepository utilisateurRepository,
                            FileStorageService fileStorageService,
                            GuideService guideService,
                            AvisService avisService) {
        this.siteTouristiqueService = siteTouristiqueService;
        this.evenementService = evenementService;
        this.reservationService = reservationService;
        this.utilisateurRepository = utilisateurRepository;
        this.fileStorageService = fileStorageService;
        this.guideService = guideService;
        this.avisService = avisService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Collect statistics
        long sitesCount = siteTouristiqueService.getAllSites().size();
        long eventsCount = evenementService.getUpcomingEvents().size();
        long bookingsCount = reservationService.getAllReservations().stream()
                .filter(r -> r.getStatut() == com.gdpt.gdpt.model.Reservation.StatutReservation.CONFIRMEE)
                .count();
        long usersCount = utilisateurRepository.count();

        model.addAttribute("sitesCount", sitesCount);
        model.addAttribute("eventsCount", eventsCount);
        model.addAttribute("bookingsCount", bookingsCount);
        model.addAttribute("usersCount", usersCount);

        // Fetch lists
        model.addAttribute("sites", siteTouristiqueService.getAllSites());
        model.addAttribute("events", evenementService.getAllEvents());
        model.addAttribute("reservations", reservationService.getAllReservations());
        model.addAttribute("guides", guideService.getAllGuides());
        model.addAttribute("signalements", avisService.getPendingSignalements());

        return "admin/dashboard";
    }

    // --- SITES MANAGEMENT (CRUD) ---

    @GetMapping("/sites/add")
    public String showAddSiteForm(Model model) {
        model.addAttribute("site", new SiteTouristique());
        model.addAttribute("categories", Categorie.values());
        return "admin/site-form";
    }

    @GetMapping("/sites/edit/{id}")
    public String showEditSiteForm(@PathVariable Long id, Model model) {
        SiteTouristique site = siteTouristiqueService.getSiteById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site non trouvé: " + id));
        model.addAttribute("site", site);
        model.addAttribute("categories", Categorie.values());
        return "admin/site-form";
    }

    @PostMapping("/sites/save")
    public String saveSite(@ModelAttribute SiteTouristique site,
                           @RequestParam(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile) {
        
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadedUri = fileStorageService.storeFile(imageFile);
            site.setImageUri(uploadedUri);
        } else if (site.getId() != null) {
            SiteTouristique oldSite = siteTouristiqueService.getSiteById(site.getId()).orElse(null);
            if (oldSite != null) {
                if (site.getImageUri() == null || site.getImageUri().isEmpty()) {
                    site.setImageUri(oldSite.getImageUri());
                }
            }
        }

        siteTouristiqueService.saveSite(site);
        return "redirect:/admin/dashboard?success=" + java.net.URLEncoder.encode("Site enregistré avec succès.", java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/sites/delete/{id}")
    public String deleteSite(@PathVariable Long id) {
        try {
            siteTouristiqueService.deleteSite(id);
            return "redirect:/admin/dashboard?success=" + java.net.URLEncoder.encode("Site supprimé avec succès.", java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error=" + java.net.URLEncoder.encode("Impossible de supprimer ce site touristique.", java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // --- EVENTS MANAGEMENT (CRUD) ---

    @GetMapping("/events/add")
    public String showAddEventForm(Model model) {
        model.addAttribute("event", new Evenement());
        model.addAttribute("sites", siteTouristiqueService.getAllSites());
        model.addAttribute("guides", guideService.getGuidesByVerification(true));
        return "admin/event-form";
    }

    @GetMapping("/events/edit/{id}")
    public String showEditEventForm(@PathVariable Long id, Model model) {
        Evenement event = evenementService.getEventById(id)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé: " + id));
        model.addAttribute("event", event);
        model.addAttribute("sites", siteTouristiqueService.getAllSites());
        model.addAttribute("guides", guideService.getGuidesByVerification(true));
        return "admin/event-form";
    }

    @PostMapping("/events/save")
    public String saveEvent(@ModelAttribute Evenement event,
                            @RequestParam Long siteId,
                            @RequestParam(required = false) Long guideId) {
        SiteTouristique site = siteTouristiqueService.getSiteById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site non trouvé: " + siteId));
        event.setSite(site);
        
        if (guideId != null) {
            Utilisateur guide = utilisateurRepository.findById(guideId).orElse(null);
            event.setGuide(guide);
        } else {
            event.setGuide(null);
        }
        
        try {
            evenementService.saveEvent(event);
            return "redirect:/admin/dashboard?success=" + java.net.URLEncoder.encode("Événement enregistré avec succès.", java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/dashboard?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        evenementService.deleteEvent(id);
        return "redirect:/admin/dashboard?success=" + java.net.URLEncoder.encode("Événement supprimé avec succès.", java.nio.charset.StandardCharsets.UTF_8);
    }

    // --- RESERVATIONS FOLLOW UP ---

    @GetMapping("/reservations")
    public String viewAllReservations(Model model) {
        model.addAttribute("reservations", reservationService.getAllReservations());
        return "admin/reservations";
    }

    // --- GUIDE VALIDATION ---

    @PostMapping("/guides/validate/{id}")
    public String validateGuide(@PathVariable Long id, @RequestParam boolean validate) {
        try {
            guideService.validateGuide(id, validate);
            String message = validate ? "Guide validé avec succès." : "Validation du guide annulée.";
            return "redirect:/admin/dashboard?success=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // --- MODERATION ---

    @PostMapping("/signalements/resolve/{id}")
    public String resolveSignalement(@PathVariable Long id, @RequestParam boolean deleteAvis) {
        try {
            avisService.resolveSignalement(id, deleteAvis);
            String message = deleteAvis ? "L'avis inapproprié a été supprimé." : "Le signalement a été classé sans suite.";
            return "redirect:/admin/dashboard?success=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
