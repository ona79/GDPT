package com.gdpt.gdpt.service;

import com.gdpt.gdpt.model.*;
import com.gdpt.gdpt.repository.AvisRepository;
import com.gdpt.gdpt.repository.ReservationRepository;
import com.gdpt.gdpt.repository.SignalementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AvisService {

    private final AvisRepository avisRepository;
    private final ReservationRepository reservationRepository;
    private final SignalementRepository signalementRepository;
    private final GuideService guideService;

    public AvisService(AvisRepository avisRepository,
                       ReservationRepository reservationRepository,
                       SignalementRepository signalementRepository,
                       GuideService guideService) {
        this.avisRepository = avisRepository;
        this.reservationRepository = reservationRepository;
        this.signalementRepository = signalementRepository;
        this.guideService = guideService;
    }

    public Avis submitAvis(Long reservationId, Utilisateur user, Integer note, String commentaire) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable : " + reservationId));

        if (!reservation.getUtilisateur().getId().equals(user.getId())) {
            throw new SecurityException("Vous n'êtes pas le propriétaire de cette réservation.");
        }

        if (reservation.getStatut() != Reservation.StatutReservation.CONFIRMEE) {
            throw new IllegalArgumentException("Vous ne pouvez laisser un avis que sur une réservation confirmée.");
        }

        if (avisRepository.findByReservationId(reservationId).isPresent()) {
            throw new IllegalArgumentException("Un avis a déjà été publié pour cette réservation.");
        }

        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5.");
        }

        Avis avis = Avis.builder()
                .reservation(reservation)
                .note(note)
                .commentaire(commentaire)
                .datePublication(LocalDateTime.now())
                .build();

        Avis savedAvis = avisRepository.save(avis);

        // Update Guide rating if guide was assigned
        Utilisateur guide = reservation.getEvenement().getGuide();
        if (guide != null) {
            guideService.updateGuideRating(guide.getId());
        }

        return savedAvis;
    }

    public Avis replyToAvis(Long avisId, Utilisateur guide, String reponse) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new IllegalArgumentException("Avis introuvable : " + avisId));

        Utilisateur assignedGuide = avis.getReservation().getEvenement().getGuide();
        if (assignedGuide == null || !assignedGuide.getId().equals(guide.getId())) {
            throw new SecurityException("Vous n'êtes pas le guide assigné à cette visite.");
        }

        avis.setReponseGuide(reponse);
        return avisRepository.save(avis);
    }

    public Signalement reportAvis(Long avisId, String motif) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new IllegalArgumentException("Avis introuvable : " + avisId));

        Signalement signalement = Signalement.builder()
                .avis(avis)
                .motif(motif)
                .statut(Signalement.StatutSignalement.EN_ATTENTE)
                .dateSignalement(LocalDateTime.now())
                .build();

        return signalementRepository.save(signalement);
    }

    public List<Avis> getAvisForSite(Long siteId) {
        return avisRepository.findByReservationEvenementSiteIdOrderByDatePublicationDesc(siteId);
    }

    public List<Avis> getAvisForGuide(Long guideId) {
        return avisRepository.findByReservationEvenementGuideIdOrderByDatePublicationDesc(guideId);
    }

    public List<Signalement> getPendingSignalements() {
        return signalementRepository.findByStatutOrderByDateSignalementDesc(Signalement.StatutSignalement.EN_ATTENTE);
    }

    public void deleteAvis(Long avisId) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new IllegalArgumentException("Avis introuvable"));
        
        Utilisateur guide = avis.getReservation().getEvenement().getGuide();
        avisRepository.delete(avis);
        
        // Recalculate guide rating
        if (guide != null) {
            guideService.updateGuideRating(guide.getId());
        }
    }

    public void resolveSignalement(Long signalementId, boolean deleteAvis) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new IllegalArgumentException("Signalement introuvable"));

        if (deleteAvis) {
            deleteAvis(signalement.getAvis().getId());
        } else {
            signalement.setStatut(Signalement.StatutSignalement.RESOLU);
            signalementRepository.save(signalement);
        }
    }
}
