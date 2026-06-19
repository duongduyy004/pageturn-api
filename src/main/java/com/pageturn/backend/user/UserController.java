package com.pageturn.backend.user;

import com.pageturn.backend.common.api.ApiResponse;
import com.pageturn.backend.common.api.ApiResponseFactory;
import com.pageturn.backend.security.CurrentUserService;
import com.pageturn.backend.user.dto.UpdateProfileRequest;
import com.pageturn.backend.user.dto.UserDto;
import com.pageturn.backend.user.dto.UserSearchDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping({"/api/users", "/api/v1/users"})
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Current user retrieved",
                userService.getCurrentUser(currentUserService.requireCurrentUserId())
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Profile updated",
                userService.updateProfile(currentUserService.requireCurrentUserId(), request)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchDto>>> searchUsers(
            @RequestParam @Size(max = 320) String email) {
        return ApiResponseFactory.success(HttpStatus.OK, "Users retrieved", userService.searchByEmail(email));
    }
}
