package com.erp.auth;

import com.erp.auth.dto.LoginRequest;
import com.erp.auth.service.AuthService;
import com.erp.common.Result;
import com.erp.security.LoginUser;
import com.erp.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        LoginUser user = SecurityUtils.current();
        return Result.ok(authService.me(user));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }
}
