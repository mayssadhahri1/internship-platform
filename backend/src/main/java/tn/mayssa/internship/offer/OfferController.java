package tn.mayssa.internship.offer;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.mayssa.internship.offer.OfferDtos.OfferRequest;
import tn.mayssa.internship.offer.OfferDtos.OfferResponse;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService service;

    public OfferController(OfferService service) {
        this.service = service;
    }

    @GetMapping
    public Page<OfferResponse> search(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String location,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return service.search(keyword, location, page, size);
    }

    @GetMapping("/{id}")
    public OfferResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('COMPANY')")
    public List<OfferResponse> mine() {
        return service.mine();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COMPANY')")
    public OfferResponse create(@Valid @RequestBody OfferRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public OfferResponse update(@PathVariable Long id, @Valid @RequestBody OfferRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('COMPANY')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
