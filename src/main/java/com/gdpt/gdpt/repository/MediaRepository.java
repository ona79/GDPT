package com.gdpt.gdpt.repository;

import com.gdpt.gdpt.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findBySiteId(Long siteId);
}
