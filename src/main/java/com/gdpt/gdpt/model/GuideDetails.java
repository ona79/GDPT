package com.gdpt.gdpt.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guide_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class GuideDetails {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_user")
    private Utilisateur user;

    @Column(name = "numero_licence", nullable = false)
    private String numeroLicence;

    @Column(columnDefinition = "TEXT")
    private String biographie;

    @Builder.Default
    @Column(name = "note_moyenne")
    private Double noteMoyenne = 0.0;

    @Builder.Default
    @Column(name = "est_verifie", nullable = false)
    private boolean estVerifie = false;
}
