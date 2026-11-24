package tn.mayssa.internship.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.mayssa.internship.offer.Offer;
import tn.mayssa.internship.offer.OfferRepository;
import tn.mayssa.internship.user.Role;
import tn.mayssa.internship.user.User;
import tn.mayssa.internship.user.UserRepository;

/** Données de démonstration (désactivables avec SEED_DEMO_DATA=false). */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataLoader implements CommandLineRunner {

    private final UserRepository users;
    private final OfferRepository offers;
    private final PasswordEncoder encoder;

    public DemoDataLoader(UserRepository users, OfferRepository offers, PasswordEncoder encoder) {
        this.users = users;
        this.offers = offers;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        String pwd = encoder.encode("Demo1234!");
        User company = users.save(new User("Responsable RH", "rh@techsousse.tn", pwd, Role.COMPANY, "TechSousse"));
        users.save(new User("Étudiant Démo", "etudiant@demo.tn", pwd, Role.STUDENT, "École Polytechnique de Sousse"));

        offers.save(new Offer("Plateforme de gestion RH",
                "Conception et développement d'une plateforme web de gestion des congés et des évaluations.",
                "Sousse", 6, "Spring Boot, Angular, PostgreSQL", company));
        offers.save(new Offer("Assistant IA pour le support client",
                "Mise en place d'un assistant basé sur un LLM et la recherche documentaire (RAG).",
                "Tunis", 5, "Python, FastAPI, LLM, RAG", company));
        offers.save(new Offer("Application mobile de suivi de livraison",
                "Application Flutter avec suivi en temps réel et notifications push.",
                "Sousse", 4, "Flutter, Firebase, REST", company));
    }
}
