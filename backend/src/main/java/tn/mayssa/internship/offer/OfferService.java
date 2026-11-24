package tn.mayssa.internship.offer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.mayssa.internship.common.ApiException;
import tn.mayssa.internship.offer.OfferDtos.OfferRequest;
import tn.mayssa.internship.offer.OfferDtos.OfferResponse;
import tn.mayssa.internship.security.CurrentUser;
import tn.mayssa.internship.user.User;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OfferService {

    private final OfferRepository offers;

    public OfferService(OfferRepository offers) {
        this.offers = offers;
    }

    public Page<OfferResponse> search(String keyword, String location, int page, int size) {
        var pageable = PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        return offers.search(clean(keyword), clean(location), pageable).map(OfferResponse::from);
    }

    public OfferResponse get(Long id) {
        return OfferResponse.from(find(id));
    }

    public List<OfferResponse> mine() {
        return offers.findByCompanyIdOrderByCreatedAtDesc(CurrentUser.get().getId())
                .stream().map(OfferResponse::from).toList();
    }

    @Transactional
    public OfferResponse create(OfferRequest req) {
        User company = CurrentUser.get();
        Offer offer = new Offer(req.title().trim(), req.description().trim(), req.location().trim(),
                req.durationMonths(), req.skills(), company);
        return OfferResponse.from(offers.save(offer));
    }

    @Transactional
    public OfferResponse update(Long id, OfferRequest req) {
        Offer offer = findOwned(id);
        boolean open = req.open() == null ? offer.isOpen() : req.open();
        offer.update(req.title().trim(), req.description().trim(), req.location().trim(),
                req.durationMonths(), req.skills(), open);
        return OfferResponse.from(offer);
    }

    @Transactional
    public void delete(Long id) {
        offers.delete(findOwned(id));
    }

    public Offer find(Long id) {
        return offers.findById(id).orElseThrow(() -> ApiException.notFound("Offre introuvable"));
    }

    /** Vérifie que l'offre appartient à l'entreprise connectée. */
    public Offer findOwned(Long id) {
        Offer offer = find(id);
        if (!offer.isOwnedBy(CurrentUser.get())) {
            throw ApiException.forbidden("Cette offre ne vous appartient pas");
        }
        return offer;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
