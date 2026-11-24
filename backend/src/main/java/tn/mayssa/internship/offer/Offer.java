package tn.mayssa.internship.offer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tn.mayssa.internship.user.User;

import java.time.Instant;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private int durationMonths;

    // Compétences séparées par des virgules (ex. "Spring Boot, Angular, Docker")
    private String skills;

    @Column(name = "is_open", nullable = false)
    private boolean open = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private User company;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Offer() {
    }

    public Offer(String title, String description, String location, int durationMonths, String skills, User company) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.durationMonths = durationMonths;
        this.skills = skills;
        this.company = company;
    }

    public void update(String title, String description, String location, int durationMonths, String skills, boolean open) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.durationMonths = durationMonths;
        this.skills = skills;
        this.open = open;
    }

    public boolean isOwnedBy(User user) {
        return company.getId().equals(user.getId());
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public int getDurationMonths() { return durationMonths; }
    public String getSkills() { return skills; }
    public boolean isOpen() { return open; }
    public User getCompany() { return company; }
    public Instant getCreatedAt() { return createdAt; }
}
