package com.pageturn.backend.security;

public final class SecurityConstants {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";
    public static final String AUTH_REGISTER_PATH = "/api/auth/register";
    public static final String AUTH_LOGIN_PATH = "/api/auth/login";
    public static final String AUTH_REFRESH_PATH = "/api/auth/refresh";
    public static final String STORE_ROOT_PATH = "/api/store";
    public static final String STORE_ITEM_PATH = "/api/store/*";

    private SecurityConstants() {
    }
}
