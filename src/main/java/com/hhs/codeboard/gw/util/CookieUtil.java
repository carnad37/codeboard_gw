package com.hhs.codeboard.gw.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@UtilityClass
public class CookieUtil {

    public static ResponseCookie getCookie(String name, String value, Duration duration) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .maxAge(duration)
//                    .secure(true)
                .sameSite("Strict")
                .path("/")
                .build();
    }

}
