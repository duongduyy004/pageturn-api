package com.pageturn.backend.auth;

import com.pageturn.backend.auth.dto.AuthResponse;
import com.pageturn.backend.auth.dto.LoginRequest;
import com.pageturn.backend.auth.dto.LogoutRequest;
import com.pageturn.backend.auth.dto.RefreshTokenRequest;
import com.pageturn.backend.auth.dto.RegisterRequest;
import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponseFactory.success(HttpStatus.CREATED, "Registration succeeded", authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponseFactory.success(HttpStatus.OK, "Login succeeded", authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponseFactory.success(HttpStatus.OK, "Token refreshed", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(currentUserService.requireCurrentUserId(), request);
        return ApiResponseFactory.success(HttpStatus.OK, "Logout succeeded");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll() {
        authService.logoutAll(currentUserService.requireCurrentUserId());
        return ApiResponseFactory.success(HttpStatus.OK, "Logout all succeeded");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserSummary>> me() {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Current user retrieved",
                authService.getCurrentUser(currentUserService.requireCurrentUserId())
        );
    }
}
