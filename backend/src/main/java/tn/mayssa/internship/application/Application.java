package tn.mayssa.internship.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tn.mayssa.internship.offer.Offer;
import tn.mayssa.internship.user.User;

import java.time.Instant;

// Un étudiant ne peut postuler qu'une seule fois à une même offre
@Entity
@Table(name = "applications", uniqueConstraints = @UniqueConstraint(columnNames = {"offer_id", "student_id"}))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id")
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(nullable = false, length = 2000)
    private String motivation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Application() {
    }

    public Application(Offer offer, User student, String motivation) {
        this.offer = offer;
        this.student = student;
        this.motivation = motivation;
    }

    public void changeStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public Offer getOffer() { return offer; }
    public User getStudent() { return student; }
    public String getMotivation() { return motivation; }
    public ApplicationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
