package com.gdpt.gdpt.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "reservation")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(nullable = false)
    private Integer note; // 1 to 5

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "reponse_guide", columnDefinition = "TEXT")
    private String reponseGuide;

    @Column(name = "date_publication", nullable = false)
    private LocalDateTime datePublication;
}
