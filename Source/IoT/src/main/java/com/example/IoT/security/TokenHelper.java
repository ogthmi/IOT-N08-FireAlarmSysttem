package com.example.IoT.security;

import com.example.IoT.entity.UserEntity;
import com.example.IoT.exception.AppException;
import com.example.IoT.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class TokenHelper {
    private static final String SECRET_KEY = "yourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKeyyourSecretKey";
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days

    public static String generateToken(UserEntity userEntity) {
        Date now = new Date();
        Date expirrationDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .claim("user_id",userEntity.getId())
                .claim("username",userEntity.getUsername())
                .claim("role", userEntity.getRole())
                .setSubject(userEntity.getUsername())
                .setIssuedAt(now)
                .setExpiration(expirrationDate)
                .signWith(SignatureAlgorithm.HS512,SECRET_KEY)
                .compact();
    }

    public static Long getUserIdFromToken(String accessToken) {
        accessToken = accessToken.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(accessToken)
                    .getBody();
            return claims.get("user_id", Long.class);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    public static String getUsernameFromToken(String accessToken) {
        accessToken = accessToken.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(accessToken)
                    .getBody();
            return claims.get("username", String.class);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    public static String getRoleFromToken(String accessToken) {
        accessToken = accessToken.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(accessToken)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}
