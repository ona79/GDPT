package com.gdpt.gdpt.controller;

import com.gdpt.gdpt.model.*;
import com.gdpt.gdpt.service.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/visiteur")
public class VisitorController {

    private final ReservationService reservationService;
    private final EvenementService evenementService;
    private final SiteTouristiqueService siteTouristiqueService;
    private final UtilisateurService utilisateurService;
    private final QrCodeService qrCodeService;
    private final AvisService avisService;

    public VisitorController(ReservationService reservationService,
                             EvenementService evenementService,
                             SiteTouristiqueService siteTouristiqueService,
                             UtilisateurService utilisateurService,
                             QrCodeService qrCodeService,
                             AvisService avisService) {
        this.reservationService = reservationService;
        this.evenementService = evenementService;
        this.siteTouristiqueService = siteTouristiqueService;
        this.utilisateurService = utilisateurService;
        this.qrCodeService = qrCodeService;
        this.avisService = avisService;
    }

    private Utilisateur getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new SecurityException("Utilisateur non connecté.");
        }
        return utilisateurService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + principal.getName()));
    }

    @GetMapping("/reservations")
    public String viewReservations(Principal principal, Model model) {
        Utilisateur user = getAuthenticatedUser(principal);
        List<Reservation> reservations = reservationService.getReservationsForUser(user.getId());
        
        Map<Long, String> qrCodes = new HashMap<>();
        Map<Long, Boolean> hasAvis = new HashMap<>();
        
        for (Reservation res : reservations) {
            if (res.getQrCodeHash() != null) {
                try {
                    String qrContent = String.format("TICKET GDPT\n------------------\nMonument : %s\nActivite : %s\nDate : %s\nPlaces : %d\nTitulaire : %s\nRef : %d\nCode : %s",
                            res.getEvenement().getSite().getNom(),
                            res.getEvenement().getNom(),
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(res.getEvenement().getDate()),
                            res.getNombrePlaces(),
                            user.getUsername(),
                            res.getId(),
                            res.getQrCodeHash());
                    String qr = qrCodeService.generateQrCodeBase64(qrContent, 250, 250);
                    qrCodes.put(res.getId(), qr);
                } catch (Exception e) {
                    // fall back
                }
            }
            boolean exists = avisService.getAvisForSite(res.getEvenement().getSite().getId()).stream()
                    .anyMatch(a -> a.getReservation().getId().equals(res.getId()));
            hasAvis.put(res.getId(), exists);
        }

        model.addAttribute("reservations", reservations);
        model.addAttribute("qrCodes", qrCodes);
        model.addAttribute("hasAvis", hasAvis);
        return "visiteur/mes-reservations";
    }

    @PostMapping("/reservation/book")
    public String bookTicket(@RequestParam Long evenementId,
                             @RequestParam int nombrePlaces,
                             Principal principal) {
        Evenement event = null;
        try {
            Utilisateur user = getAuthenticatedUser(principal);
            event = evenementService.getEventById(evenementId)
                    .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé: " + evenementId));
            
            reservationService.bookEvent(event, user, nombrePlaces);
            return "redirect:/sites/details/" + event.getSite().getId() + "?success=" + java.net.URLEncoder.encode("Votre réservation a été confirmée !", java.nio.charset.StandardCharsets.UTF_8);
        } catch (ObjectOptimisticLockingFailureException e) {
            if (event != null && event.getSite() != null) {
                return "redirect:/sites/details/" + event.getSite().getId() + "?error=" + java.net.URLEncoder.encode("Cette réservation n'a pas pu être confirmée, les places viennent d'être prises. Veuillez réessayer.", java.nio.charset.StandardCharsets.UTF_8);
            }
            return "redirect:/?error=" + java.net.URLEncoder.encode("Cette réservation n'a pas pu être confirmée, les places viennent d'être prises. Veuillez réessayer.", java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            if (event != null && event.getSite() != null) {
                return "redirect:/sites/details/" + event.getSite().getId() + "?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
            return "redirect:/?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/reservation/cancel/{id}")
    public String cancelTicket(@PathVariable Long id, Principal principal) {
        try {
            Utilisateur user = getAuthenticatedUser(principal);
            reservationService.cancelReservation(id, user);
            return "redirect:/visiteur/reservations?success=" + java.net.URLEncoder.encode("Réservation annulée avec succès.", java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/visiteur/reservations?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/evaluation/add")
    public String addReview(@RequestParam Long reservationId,
                            @RequestParam int note,
                            @RequestParam String commentaire,
                            Principal principal) {
        try {
            Utilisateur user = getAuthenticatedUser(principal);
            avisService.submitAvis(reservationId, user, note, commentaire);
            return "redirect:/visiteur/reservations?success=" + java.net.URLEncoder.encode("Merci pour votre avis !", java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/visiteur/reservations?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
