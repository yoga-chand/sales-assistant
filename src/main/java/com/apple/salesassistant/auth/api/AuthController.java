package com.apple.salesassistant.auth.api;

import com.apple.salesassistant.auth.service.AuthService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  // DTOs (request/response)
  public record LoginRequest(
          @NotBlank @Email String email,
          @NotBlank String password
  ) {}

  public record LoginResponse(
          String userId,
          String email,
          java.util.List<String> roles,
          String accessToken,           // JWT
          String tokenType,             // "Bearer"
          long   expiresInSeconds
  ) {}

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
    var result = authService.authenticate(req.email(), req.password());
    var body = new LoginResponse(
            result.user().id(),
            result.user().email(),
            java.util.List.copyOf(result.user().roles()),
            result.jwt(),
            "Bearer",
            result.expiresInSeconds()
    );
    return ResponseEntity.ok(body);
  }
}
