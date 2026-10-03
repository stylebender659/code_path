package com.codepath.backend.controller;

import com.codepath.backend.model.User;
import com.codepath.backend.repository.UserRepository;
import com.codepath.backend.service.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")


public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(
            AuthService authService,
            UserRepository userRepository
    ) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request,
            HttpSession session
    ) {

        User user = authService.register(
                request.name(),
                request.email(),
                request.password()
        );

        session.setAttribute("userId", user.getId());

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Registration successful"
        );
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session
    ) {

        User user = authService.login(
                request.email(),
                request.password()
        );

        session.setAttribute("userId", user.getId());

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Login successful"
        );
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    @GetMapping("/me")
    public AuthResponse me(HttpSession session) {

        Object userIdAttribute =
                session.getAttribute("userId");

        if (userIdAttribute == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Not logged in"
            );
        }

        Long userId = (Long) userIdAttribute;

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Authenticated"
        );
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public AuthResponse logout(HttpSession session) {

        session.invalidate();

        return new AuthResponse(
                null,
                null,
                null,
                "Logout successful"
        );
    }

    // =========================================================
    // REQUEST DTOs
    // =========================================================

    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    // =========================================================
    // RESPONSE DTO
    // =========================================================

    public record AuthResponse(
            Long id,
            String name,
            String email,
            String message
    ) {
    }
}