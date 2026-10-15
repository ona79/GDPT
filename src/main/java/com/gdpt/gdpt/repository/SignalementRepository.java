package com.gdpt.gdpt.repository;

import com.gdpt.gdpt.model.Signalement;
import com.gdpt.gdpt.model.Signalement.StatutSignalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {
    List<Signalement> findByStatutOrderByDateSignalementDesc(StatutSignalement statut);
}
