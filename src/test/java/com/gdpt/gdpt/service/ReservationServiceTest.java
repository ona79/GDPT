package com.gdpt.gdpt.service;

import com.gdpt.gdpt.model.*;
import com.gdpt.gdpt.repository.EvenementRepository;
import com.gdpt.gdpt.repository.ReservationRepository;
import com.gdpt.gdpt.repository.SiteTouristiqueRepository;
import com.gdpt.gdpt.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private SiteTouristiqueRepository siteTouristiqueRepository;

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void testBookEventSuccess() {
        // Given
        Utilisateur user = Utilisateur.builder()
                .username("testvisiteur")
                .email("testvisiteur@mail.com")
                .password("encoded_pass")
                .role(Utilisateur.Role.ROLE_VISITEUR)
                .build();
        user = utilisateurRepository.save(user);

        SiteTouristique site = SiteTouristique.builder()
                .nom("Site de Test")
                .localisation("Paris")
                .categorie(SiteTouristique.Categorie.CULTUREL)
                .build();
        site = siteTouristiqueRepository.save(site);

        Evenement event = Evenement.builder()
                .nom("Concert Test")
                .description("Description")
                .date(LocalDateTime.now().plusDays(2))
                .placesMax(50)
                .placesDisponibles(50)
                .prixTicket(10.0)
                .site(site)
                .build();
        event = evenementRepository.save(event);

        // When
        Reservation reservation = reservationService.bookEvent(event, user, 3);

        // Then
        assertNotNull(reservation.getId());
        assertEquals(3, reservation.getNombrePlaces());
        assertEquals(Reservation.StatutReservation.CONFIRMEE, reservation.getStatut());
        
        // Reload event from database to check places decrement
        Evenement updatedEvent = evenementRepository.findById(event.getId()).orElseThrow();
        assertEquals(47, updatedEvent.getPlacesDisponibles());
    }

    @Test
    void testBookEventInsufficientPlaces() {
        // Given
        Utilisateur user = Utilisateur.builder()
                .username("testvisiteur2")
                .email("testvisiteur2@mail.com")
                .password("encoded_pass")
                .role(Utilisateur.Role.ROLE_VISITEUR)
                .build();
        user = utilisateurRepository.save(user);

        SiteTouristique site = SiteTouristique.builder()
                .nom("Site de Test 2")
                .localisation("Nice")
                .categorie(SiteTouristique.Categorie.NATUREL)
                .build();
        site = siteTouristiqueRepository.save(site);

        Evenement event = Evenement.builder()
                .nom("Kayak Test")
                .description("Description")
                .date(LocalDateTime.now().plusDays(2))
                .placesMax(5)
                .placesDisponibles(5)
                .prixTicket(20.0)
                .site(site)
                .build();
        event = evenementRepository.save(event);

        // When & Then
        Evenement finalEvent = event;
        Utilisateur finalUser = user;
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.bookEvent(finalEvent, finalUser, 10); // Trying to book 10 seats when only 5 are available
        });
    }
}
