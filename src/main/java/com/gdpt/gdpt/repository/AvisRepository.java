package com.gdpt.gdpt.repository;

import com.gdpt.gdpt.model.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {
    
    List<Avis> findByReservationEvenementSiteIdOrderByDatePublicationDesc(Long siteId);
    
    List<Avis> findByReservationEvenementGuideIdOrderByDatePublicationDesc(Long guideId);
    
    Optional<Avis> findByReservationId(Long reservationId);
    
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.reservation.evenement.site.id = :siteId")
    Double getAverageNoteForSite(@Param("siteId") Long siteId);
    
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.reservation.evenement.guide.id = :guideId")
    Double getAverageNoteForGuide(@Param("guideId") Long guideId);
}
