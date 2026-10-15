package com.gdpt.gdpt.service;

import com.gdpt.gdpt.model.SiteTouristique;
import com.gdpt.gdpt.model.SiteTouristique.Categorie;
import com.gdpt.gdpt.repository.AvisRepository;
import com.gdpt.gdpt.repository.SiteTouristiqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SiteTouristiqueService {

    private final SiteTouristiqueRepository siteTouristiqueRepository;
    private final AvisRepository avisRepository;

    public SiteTouristiqueService(SiteTouristiqueRepository siteTouristiqueRepository,
                                  AvisRepository avisRepository) {
        this.siteTouristiqueRepository = siteTouristiqueRepository;
        this.avisRepository = avisRepository;
    }

    public List<SiteTouristique> getAllSites() {
        return siteTouristiqueRepository.findAll();
    }

    public Optional<SiteTouristique> getSiteById(Long id) {
        return siteTouristiqueRepository.findById(id);
    }

    public SiteTouristique saveSite(SiteTouristique site) {
        return siteTouristiqueRepository.save(site);
    }

    public void deleteSite(Long id) {
        siteTouristiqueRepository.deleteById(id);
    }

    public List<SiteTouristique> searchSites(String query, Categorie categorie) {
        if (query != null && !query.trim().isEmpty()) {
            if (categorie != null) {
                return siteTouristiqueRepository.searchByCategorieAndQuery(categorie, query.trim());
            } else {
                return siteTouristiqueRepository.searchByQuery(query.trim());
            }
        } else if (categorie != null) {
            return siteTouristiqueRepository.findByCategorie(categorie);
        } else {
            return siteTouristiqueRepository.findAll();
        }
    }

    public List<SiteTouristique> searchSitesProximity(String query, Categorie categorie, Double userLat, Double userLng) {
        List<SiteTouristique> sites = searchSites(query, categorie);
        if (userLat != null && userLng != null) {
            sites.sort((s1, s2) -> {
                Double lat1 = s1.getLatitude();
                Double lon1 = s1.getLongitude();
                Double lat2 = s2.getLatitude();
                Double lon2 = s2.getLongitude();
                if (lat1 == null || lon1 == null) return 1;
                if (lat2 == null || lon2 == null) return -1;
                double dist1 = calculateDistance(userLat, userLng, lat1, lon1);
                double dist2 = calculateDistance(userLat, userLng, lat2, lon2);
                return Double.compare(dist1, dist2);
            });
        }
        return sites;
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    public Double getAverageRating(Long siteId) {
        Double avg = avisRepository.getAverageNoteForSite(siteId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }
}
