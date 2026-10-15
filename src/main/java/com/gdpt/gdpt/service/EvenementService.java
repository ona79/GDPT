package com.gdpt.gdpt.service;

import com.gdpt.gdpt.model.Evenement;
import com.gdpt.gdpt.repository.EvenementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EvenementService {

    private final EvenementRepository evenementRepository;

    public EvenementService(EvenementRepository evenementRepository) {
        this.evenementRepository = evenementRepository;
    }

    public List<Evenement> getAllEvents() {
        return evenementRepository.findAll();
    }

    public List<Evenement> getUpcomingEvents() {
        return evenementRepository.findUpcomingEvents(LocalDateTime.now());
    }

    public List<Evenement> getUpcomingEventsForSite(Long siteId) {
        return evenementRepository.findUpcomingEventsBySite(siteId, LocalDateTime.now());
    }

    public Optional<Evenement> getEventById(Long id) {
        return evenementRepository.findById(id);
    }

    public Evenement saveEvent(Evenement event) {
        // If it's a new event, initialize available seats
        if (event.getId() == null) {
            event.setPlacesDisponibles(event.getPlacesMax());
        } else {
            // Update logic for editing places: keep track of difference
            Evenement existing = evenementRepository.findById(event.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));
            int bookedSeats = existing.getPlacesMax() - existing.getPlacesDisponibles();
            if (event.getPlacesMax() < bookedSeats) {
                throw new IllegalArgumentException("La capacité maximale ne peut pas être inférieure au nombre de places déjà réservées (" + bookedSeats + ").");
            }
            event.setPlacesDisponibles(event.getPlacesMax() - bookedSeats);
        }
        return evenementRepository.save(event);
    }

    public void deleteEvent(Long id) {
        evenementRepository.deleteById(id);
    }
}
