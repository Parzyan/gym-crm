package com.company.gym.filter;

import com.company.gym.entity.Credentials;
import com.company.gym.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationService authenticationService;

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/trainees/register",
            "/trainees/status",
            "/trainers/register",
            "/trainers/status",
            "/trainings/types",
            "/trainings"
    );

    private static final List<String> PROTECTED_PREFIXES = List.of(
            "/trainees",
            "/trainers",
            "/trainings"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        boolean requiresAuth = PROTECTED_PREFIXES.stream().anyMatch(path::startsWith) && !EXCLUDED_PATHS.contains(path);

        if (requiresAuth) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            try {
                String base64Credentials = authHeader.substring("Basic ".length());
                String credentialsString = new String(Base64.getDecoder().decode(base64Credentials));
                String[] values = credentialsString.split(":", 2);

                if (values.length != 2) {
                    throw new IllegalArgumentException("Invalid Authorization header format");
                }

                String username = values[0];
                String password = values[1];

                authenticationService.authenticate(new Credentials(username, password));

                request.setAttribute("authenticatedUsername", username);

            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed: " + e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
