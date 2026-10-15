package com.gdpt.gdpt.repository;

import com.gdpt.gdpt.model.GuideDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GuideDetailsRepository extends JpaRepository<GuideDetails, Long> {
    List<GuideDetails> findByEstVerifie(boolean estVerifie);
}
