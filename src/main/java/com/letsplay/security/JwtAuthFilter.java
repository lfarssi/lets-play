package com.letsplay.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.letsplay.user.User;
import com.letsplay.user.UserRepository;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService uds,
            HandlerExceptionResolver handlerExceptionResolver,UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userDetailsService = uds;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        if (path.startsWith("/auth/") || (path.equals("/products") && request.getMethod()=="GET")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String auth = request.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new BadCredentialsException("Invalid token");
            }

            String token = auth.substring(7);

            Jws<Claims> jws = jwtService.parse(token);
            String userId = jws.getPayload().getSubject(); // 🔥 primary identity
            String email = jws.getPayload().get("email", String.class);

            if (userId == null) {
                throw new BadCredentialsException("Invalid token: missing subject");
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // 🔥 verify token integrity
            if (!user.getEmail().equals(email)) {
                throw new BadCredentialsException("Token email mismatch");
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request, response, null, e);

        }
    }
}
