package tn.mayssa.internship.offer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Recherche par mot-clé (titre, description, compétences) et par ville.
    // Une chaîne vide signifie « pas de filtre ».
    @EntityGraph(attributePaths = "company")
    @Query("""
            select o from Offer o
            where o.open = true
              and (:keyword = ''
                   or lower(o.title) like lower(concat('%', :keyword, '%'))
                   or lower(o.description) like lower(concat('%', :keyword, '%'))
                   or lower(o.skills) like lower(concat('%', :keyword, '%')))
              and (:location = '' or lower(o.location) like lower(concat('%', :location, '%')))
            """)
    Page<Offer> search(@Param("keyword") String keyword, @Param("location") String location, Pageable pageable);

    @EntityGraph(attributePaths = "company")
    List<Offer> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    long countByOpenTrue();
}
