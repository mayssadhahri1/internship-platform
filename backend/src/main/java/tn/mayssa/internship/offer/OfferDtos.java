package tn.mayssa.internship.offer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class OfferDtos {

    private OfferDtos() {
    }

    public record OfferRequest(
            @NotBlank @Size(max = 150) String title,
            @NotBlank @Size(max = 4000) String description,
            @NotBlank @Size(max = 100) String location,
            @Min(1) @Max(12) int durationMonths,
            @Size(max = 300) String skills,
            Boolean open) {
    }

    public record OfferResponse(
            Long id,
            String title,
            String description,
            String location,
            int durationMonths,
            String skills,
            boolean open,
            Long companyId,
            String companyName,
            Instant createdAt) {

        public static OfferResponse from(Offer o) {
            var company = o.getCompany();
            String name = company.getOrganization() != null ? company.getOrganization() : company.getFullName();
            return new OfferResponse(o.getId(), o.getTitle(), o.getDescription(), o.getLocation(),
                    o.getDurationMonths(), o.getSkills(), o.isOpen(), company.getId(), name, o.getCreatedAt());
        }
    }
}
