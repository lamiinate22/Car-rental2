package com.crud.rental.controller.auth;
import com.crud.rental.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        if (
                request.getEmail().equals("admin@test.com")
                        && request.getPassword().equals("admin")
        ) {
            String token = jwtService.generateToken(request.getEmail());
            return new AuthResponse(token, request.getEmail(), true);
        }

        throw new RuntimeException("Invalid email or password");
    }
}
