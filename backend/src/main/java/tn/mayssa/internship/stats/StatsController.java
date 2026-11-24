package tn.mayssa.internship.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.mayssa.internship.application.ApplicationRepository;
import tn.mayssa.internship.application.ApplicationStatus;
import tn.mayssa.internship.offer.OfferRepository;
import tn.mayssa.internship.security.CurrentUser;
import tn.mayssa.internship.user.Role;
import tn.mayssa.internship.user.User;
import tn.mayssa.internship.user.UserRepository;

/** Chiffres du tableau de bord, adaptés au rôle de l'utilisateur connecté. */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    public record Stats(long openOffers, long applications, long pending, long accepted, long rejected, long students) {
    }

    private final OfferRepository offers;
    private final ApplicationRepository applications;
    private final UserRepository users;

    public StatsController(OfferRepository offers, ApplicationRepository applications, UserRepository users) {
        this.offers = offers;
        this.applications = applications;
        this.users = users;
    }

    @GetMapping
    public Stats stats() {
        User user = CurrentUser.get();
        if (user.getRole() == Role.COMPANY) {
            Long id = user.getId();
            long open = offers.findByCompanyIdOrderByCreatedAtDesc(id).stream().filter(o -> o.isOpen()).count();
            return new Stats(open,
                    applications.countByOfferCompanyId(id),
                    applications.countByOfferCompanyIdAndStatus(id, ApplicationStatus.PENDING),
                    applications.countByOfferCompanyIdAndStatus(id, ApplicationStatus.ACCEPTED),
                    applications.countByOfferCompanyIdAndStatus(id, ApplicationStatus.REJECTED),
                    users.countByRole(Role.STUDENT));
        }
        return new Stats(offers.countByOpenTrue(),
                applications.count(),
                applications.countByStatus(ApplicationStatus.PENDING),
                applications.countByStatus(ApplicationStatus.ACCEPTED),
                applications.countByStatus(ApplicationStatus.REJECTED),
                users.countByRole(Role.STUDENT));
    }
}
