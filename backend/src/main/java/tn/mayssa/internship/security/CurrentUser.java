package tn.mayssa.internship.security;

import org.springframework.security.core.context.SecurityContextHolder;
import tn.mayssa.internship.common.ApiException;
import tn.mayssa.internship.user.User;

/** Accès simple à l'utilisateur connecté depuis les services. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static User get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserDetails details) {
            return details.getUser();
        }
        throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Authentification requise");
    }
}
