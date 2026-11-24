package tn.mayssa.internship.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.mayssa.internship.common.ApiException;
import tn.mayssa.internship.offer.Offer;
import tn.mayssa.internship.offer.OfferService;
import tn.mayssa.internship.security.CurrentUser;
import tn.mayssa.internship.user.User;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ApplicationService {

    public record ApplyRequest(@NotBlank @Size(min = 20, max = 2000, message = "Entre 20 et 2000 caractères") String motivation) {
    }

    public record StatusRequest(@NotNull ApplicationStatus status) {
    }

    public record ApplicationResponse(
            Long id,
            Long offerId,
            String offerTitle,
            String companyName,
            Long studentId,
            String studentName,
            String studentEmail,
            String studentSchool,
            String motivation,
            ApplicationStatus status,
            Instant createdAt) {

        static ApplicationResponse from(Application a) {
            Offer o = a.getOffer();
            User c = o.getCompany();
            User s = a.getStudent();
            return new ApplicationResponse(a.getId(), o.getId(), o.getTitle(),
                    c.getOrganization() != null ? c.getOrganization() : c.getFullName(),
                    s.getId(), s.getFullName(), s.getEmail(), s.getOrganization(),
                    a.getMotivation(), a.getStatus(), a.getCreatedAt());
        }
    }

    private final ApplicationRepository applications;
    private final OfferService offerService;

    public ApplicationService(ApplicationRepository applications, OfferService offerService) {
        this.applications = applications;
        this.offerService = offerService;
    }

    @Transactional
    public ApplicationResponse apply(Long offerId, ApplyRequest req) {
        User student = CurrentUser.get();
        Offer offer = offerService.find(offerId);

        if (!offer.isOpen()) {
            throw ApiException.badRequest("Cette offre est fermée");
        }
        if (applications.existsByOfferIdAndStudentId(offerId, student.getId())) {
            throw ApiException.conflict("Vous avez déjà postulé à cette offre");
        }
        return ApplicationResponse.from(applications.save(new Application(offer, student, req.motivation().trim())));
    }

    public List<ApplicationResponse> mine() {
        return applications.findByStudentIdOrderByCreatedAtDesc(CurrentUser.get().getId())
                .stream().map(ApplicationResponse::from).toList();
    }

    public List<ApplicationResponse> forOffer(Long offerId) {
        offerService.findOwned(offerId); // seule l'entreprise propriétaire voit les candidats
        return applications.findByOfferIdOrderByCreatedAtDesc(offerId)
                .stream().map(ApplicationResponse::from).toList();
    }

    @Transactional
    public ApplicationResponse changeStatus(Long applicationId, ApplicationStatus status) {
        Application application = applications.findById(applicationId)
                .orElseThrow(() -> ApiException.notFound("Candidature introuvable"));
        if (!application.getOffer().isOwnedBy(CurrentUser.get())) {
            throw ApiException.forbidden("Cette candidature ne concerne pas vos offres");
        }
        application.changeStatus(status);
        return ApplicationResponse.from(application);
    }
}
