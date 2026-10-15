package com.gdpt.gdpt.service;

import com.gdpt.gdpt.model.Evenement;
import com.gdpt.gdpt.model.Reservation;
import com.gdpt.gdpt.model.Reservation.StatutReservation;
import com.gdpt.gdpt.model.Utilisateur;
import com.gdpt.gdpt.repository.EvenementRepository;
import com.gdpt.gdpt.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EvenementRepository evenementRepository;

    public ReservationService(ReservationRepository reservationRepository, EvenementRepository evenementRepository) {
        this.reservationRepository = reservationRepository;
        this.evenementRepository = evenementRepository;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsForUser(Long utilisateurId) {
        return reservationRepository.findByUtilisateurIdOrderByDateReservationDesc(utilisateurId);
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation bookEvent(Evenement event, Utilisateur user, int seats) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit être supérieur à zéro.");
        }

        if (event.getDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cet événement a déjà eu lieu.");
        }

        if (event.getPlacesDisponibles() < seats) {
            throw new IllegalArgumentException("Nombre de places insuffisant. Places disponibles : " + event.getPlacesDisponibles());
        }

        // Decrement available seats
        event.setPlacesDisponibles(event.getPlacesDisponibles() - seats);
        evenementRepository.save(event);

        Reservation reservation = Reservation.builder()
                .evenement(event)
                .utilisateur(user)
                .nombrePlaces(seats)
                .dateReservation(LocalDateTime.now())
                .statut(StatutReservation.CONFIRMEE)
                .qrCodeHash(java.util.UUID.randomUUID().toString())
                .build();

        return reservationRepository.save(reservation);
    }

    public void cancelReservation(Long id, Utilisateur user) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée: " + id));

        // If the caller is a visitor, check ownership
        if (user.getRole() == Utilisateur.Role.ROLE_VISITEUR && !reservation.getUtilisateur().getId().equals(user.getId())) {
            throw new SecurityException("Vous n'êtes pas autorisé à annuler cette réservation.");
        }

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new IllegalStateException("Cette réservation est déjà annulée.");
        }

        Evenement event = reservation.getEvenement();
        if (event.getDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible d'annuler une réservation pour un événement déjà passé.");
        }

        // Règle d'annulation : refuser si l'événement est dans moins de 24 heures
        if (event.getDate().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalStateException("Annulation impossible : l'événement a lieu dans moins de 24 heures.");
        }

        // Increment available seats
        event.setPlacesDisponibles(event.getPlacesDisponibles() + reservation.getNombrePlaces());
        evenementRepository.save(event);

        // Changer le statut plutôt que supprimer la ligne — préserve l'historique
        reservation.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(reservation);
    }
}
