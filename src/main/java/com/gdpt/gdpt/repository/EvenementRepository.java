package com.gdpt.gdpt.repository;

import com.gdpt.gdpt.model.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findBySiteId(Long siteId);
    
    @Query("SELECT e FROM Evenement e WHERE e.date >= :now ORDER BY e.date ASC")
    List<Evenement> findUpcomingEvents(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Evenement e WHERE e.site.id = :siteId AND e.date >= :now ORDER BY e.date ASC")
    List<Evenement> findUpcomingEventsBySite(@Param("siteId") Long siteId, @Param("now") LocalDateTime now);
}
