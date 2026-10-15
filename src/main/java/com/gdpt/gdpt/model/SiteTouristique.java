package com.gdpt.gdpt.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sites_touristiques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"evenements", "medias"})
public class SiteTouristique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String localisation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categorie categorie;

    private String imageUri;

    private Double tarifEntree;

    private String horairesOuverture;

    @Column(name = "capacite_accueil")
    private Integer capaciteAccueil;

    private Double latitude;

    private Double longitude;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Evenement> evenements = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Media> medias = new ArrayList<>();

    public enum Categorie {
        HISTORIQUE,
        NATUREL,
        CULTUREL,
        RELIGIEUX,
        GASTRONOMIQUE
    }
}
