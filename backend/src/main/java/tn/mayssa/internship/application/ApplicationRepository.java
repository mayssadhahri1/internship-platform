package tn.mayssa.internship.application;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByOfferIdAndStudentId(Long offerId, Long studentId);

    @EntityGraph(attributePaths = {"offer", "offer.company", "student"})
    List<Application> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"offer", "offer.company", "student"})
    List<Application> findByOfferIdOrderByCreatedAtDesc(Long offerId);

    long countByStatus(ApplicationStatus status);

    long countByOfferCompanyId(Long companyId);

    long countByOfferCompanyIdAndStatus(Long companyId, ApplicationStatus status);
}
