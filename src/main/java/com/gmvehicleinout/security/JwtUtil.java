package com.gmvehicleinout.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "ZzGZv7UJhG8o+ViYDHsFtBW0/WC6+WwVjlCJhdzOi3U=";
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 60 * 24 * 5;    // 30 minutes
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days

    // ============================
    //   Generate Access Token
    // ============================
    public static String generateAccessToken(String mobile) {
        return Jwts.builder()
                .setSubject(mobile)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    // ============================
    //   Generate Refresh Token
    // ============================
    public static String generateRefreshToken(String mobile) {
        return Jwts.builder()
                .setSubject(mobile)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }


    // ============================
    //   Validate and Extract Mobile
    // ============================
    public static String extractMobile(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
