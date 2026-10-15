package com.gdpt.gdpt.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signalements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "avis")
public class Signalement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avis_id", nullable = false)
    private Avis avis;

    @Column(nullable = false)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutSignalement statut = StatutSignalement.EN_ATTENTE;

    @Column(name = "date_signalement", nullable = false)
    private LocalDateTime dateSignalement;

    public enum StatutSignalement {
        EN_ATTENTE,
        RESOLU
    }
}
