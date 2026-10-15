package com.gdpt.gdpt.service;

import com.gdpt.gdpt.model.GuideDetails;
import com.gdpt.gdpt.repository.AvisRepository;
import com.gdpt.gdpt.repository.GuideDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GuideService {

    private final GuideDetailsRepository guideDetailsRepository;
    private final AvisRepository avisRepository;

    public GuideService(GuideDetailsRepository guideDetailsRepository, AvisRepository avisRepository) {
        this.guideDetailsRepository = guideDetailsRepository;
        this.avisRepository = avisRepository;
    }

    public List<GuideDetails> getAllGuides() {
        return guideDetailsRepository.findAll();
    }

    public List<GuideDetails> getGuidesByVerification(boolean estVerifie) {
        return guideDetailsRepository.findByEstVerifie(estVerifie);
    }

    public Optional<GuideDetails> getGuideById(Long id) {
        return guideDetailsRepository.findById(id);
    }

    public GuideDetails validateGuide(Long guideId, boolean validate) {
        GuideDetails details = guideDetailsRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Détails du guide introuvables : " + guideId));
        details.setEstVerifie(validate);
        return guideDetailsRepository.save(details);
    }

    public GuideDetails updateBio(Long guideId, String biography) {
        GuideDetails details = guideDetailsRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Détails du guide introuvables : " + guideId));
        details.setBiographie(biography);
        return guideDetailsRepository.save(details);
    }

    public void updateGuideRating(Long guideId) {
        GuideDetails details = guideDetailsRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Détails du guide introuvables : " + guideId));
        Double avg = avisRepository.getAverageNoteForGuide(guideId);
        details.setNoteMoyenne(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        guideDetailsRepository.save(details);
    }
}
