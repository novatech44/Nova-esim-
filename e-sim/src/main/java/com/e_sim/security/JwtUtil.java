package com.e_sim.security;

// import com.traverse.authenticationservice.config.AppProperties;
// import com.traverse.authenticationservice.dao.entity.Permission;
// import com.traverse.authenticationservice.dao.entity.Role;
// import com.traverse.authenticationservice.dao.entity.User;
// import com.traverse.authenticationservice.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import com.e_sim.config.AppProperties;
import com.e_sim.dao.entity.Permission;
import com.e_sim.dao.entity.Role;
import com.e_sim.dao.entity.User;
import com.e_sim.exception.UnauthorizedException;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtEncoder encoder;
    private final JwtDecoder jwtDecoder;
    private final AppProperties appProperties;

    public String createToken(User user) {
        Instant nowInstant = Instant.now();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        List<String> permissionNames = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appProperties.getJwtIssuer())
                .issuedAt(nowInstant)
                .expiresAt(nowInstant.plusSeconds(appProperties.getJwtExpiresAt()))
                .subject(user.getUsername())
                .claim("roles", roleNames)
                .claim("permissions", permissionNames)
                .build();

        return encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String getUsernameFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getSubject();
    }

    public boolean isNotTokenExpired(String token) {
        Instant expirationTime = jwtDecoder.decode(token).getExpiresAt();
        return expirationTime != null && expirationTime.isAfter(Instant.now());
    }

    public User getAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                return user;
            }
        } catch (Exception e) {
            log.info("Failed to retrieve authenticated user: {}", e.getMessage());
        }
        throw new UnauthorizedException("User is not authenticated");
    }
}
