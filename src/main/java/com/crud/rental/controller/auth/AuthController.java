package com.crud.rental.controller.auth;
import com.crud.rental.domain.User;
import com.crud.rental.repository.UserRepository;
import com.crud.rental.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.isAdmin());
    }
}
