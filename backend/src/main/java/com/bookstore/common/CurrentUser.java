package com.bookstore.common;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

/** Small helper every controller uses to read "who is calling" out of the JWT-populated SecurityContext. */
public class CurrentUser {

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object details = auth.getDetails();
        return details instanceof Long ? (Long) details : null;
    }

    public static String email() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
