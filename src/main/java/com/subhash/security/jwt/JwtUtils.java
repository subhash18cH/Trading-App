package com.subhash.security.jwt;

import com.subhash.security.service.MyUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private String SECRET_KEY;

    public JwtUtils(){
        try {
            KeyGenerator keyGen=KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk=keyGen.generateKey();
            SECRET_KEY= Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${spring.jwt.expiration}")
    private Long jwtExpiration;

    public String generateToken(MyUserDetails userDetails) {
        String userName=userDetails.getUsername();
        String email=userDetails.getEmail();
        String roles=userDetails.getAuthorities().stream()
                .map(authority->authority.getAuthority()).collect(Collectors.joining(","));
        return Jwts.builder()
                .subject(email)
                .claim("userName",userName)
                .claim("roles",roles)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime()+jwtExpiration))
                .signWith(getKey())
                .compact();
    }

    private Key getKey() {
        byte [] keyBytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken=request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }else{
            return null;
        }
    }

    public boolean validateToken(String jwt) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getKey())
                    .build()
                    .parseSignedClaims(jwt);
            return true;
        }catch (Exception e){
            System.out.println(e);
        }
        return false;
    }

    public String getEmailFromJwtToken(String jwt) {
        return Jwts.parser()
                .verifyWith((SecretKey)getKey())
                .build()
                .parseSignedClaims(jwt).getPayload().getSubject();
    }
}
