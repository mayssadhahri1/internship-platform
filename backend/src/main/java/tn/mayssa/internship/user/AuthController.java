package tn.mayssa.internship.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.mayssa.internship.common.ApiException;
import tn.mayssa.internship.security.CurrentUser;
import tn.mayssa.internship.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72, message = "8 caractères minimum") String password,
            @NotNull Role role,
            @Size(max = 150) String organization) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record UserResponse(Long id, String fullName, String email, Role role, String organization) {
        static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getOrganization());
        }
    }

    public record AuthResponse(String token, long expiresIn, UserResponse user) {
    }

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        // Le rôle ADMIN ne peut pas être choisi à l'inscription
        if (req.role() == Role.ADMIN) {
            throw ApiException.badRequest("Rôle non autorisé");
        }
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw ApiException.conflict("Un compte existe déjà avec cet email");
        }
        User user = users.save(new User(req.fullName().trim(), req.email().trim().toLowerCase(),
                passwordEncoder.encode(req.password()), req.role(), req.organization()));
        return new AuthResponse(jwtService.generate(user), jwtService.validitySeconds(), UserResponse.from(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = users.findByEmailIgnoreCase(req.email()).orElseThrow();
        return new AuthResponse(jwtService.generate(user), jwtService.validitySeconds(), UserResponse.from(user));
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(CurrentUser.get());
    }
}
