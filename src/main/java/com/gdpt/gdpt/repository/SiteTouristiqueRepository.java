package com.gdpt.gdpt.repository;

import com.gdpt.gdpt.model.SiteTouristique;
import com.gdpt.gdpt.model.SiteTouristique.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteTouristiqueRepository extends JpaRepository<SiteTouristique, Long> {
    List<SiteTouristique> findByCategorie(Categorie categorie);
    
    @Query("SELECT s FROM SiteTouristique s WHERE LOWER(s.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.localisation) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<SiteTouristique> searchByQuery(@Param("query") String query);

    @Query("SELECT s FROM SiteTouristique s WHERE s.categorie = :categorie AND (LOWER(s.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.localisation) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<SiteTouristique> searchByCategorieAndQuery(@Param("categorie") Categorie categorie, @Param("query") String query);
}
