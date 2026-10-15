package com.gdpt.gdpt.repository;

import com.gdpt.gdpt.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUtilisateurIdOrderByDateReservationDesc(Long utilisateurId);
    List<Reservation> findByEvenementId(Long evenementId);
    boolean existsByUtilisateurIdAndEvenementSiteIdAndStatut(Long utilisateurId, Long siteId, Reservation.StatutReservation statut);
}
