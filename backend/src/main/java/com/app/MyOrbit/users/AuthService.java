package com.app.MyOrbit.users;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, UserSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        String name = required(request.name(), "El nombre es obligatorio");
        String email = normalizedEmail(request.email());
        String password = required(request.password(), "La contrasena es obligatoria");
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 8 caracteres");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        return createSession(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizedEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Correo o contrasena incorrectos"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Correo o contrasena incorrectos");
        }
        return createSession(user);
    }

    public User requireUser(String authorization) {
        String token = extractToken(authorization);
        UserSession session = sessionRepository.findById(token)
                .orElseThrow(() -> new SecurityException("Sesion no valida"));
        return userRepository.findById(session.getUserId())
                .orElseThrow(() -> new SecurityException("Sesion no valida"));
    }

    public void logout(String authorization) {
        sessionRepository.deleteById(extractToken(authorization));
    }

    private AuthResponse createSession(User user) {
        UserSession session = new UserSession();
        session.setToken(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        sessionRepository.save(session);
        return new AuthResponse(session.getToken(), user.getId(), user.getName(), user.getEmail());
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private String normalizedEmail(String value) {
        String email = required(value, "El correo es obligatorio").toLowerCase(Locale.ROOT);
        if (!email.contains("@")) throw new IllegalArgumentException("Ingresa un correo valido");
        return email;
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new SecurityException("Debes iniciar sesion");
        }
        return authorization.substring(7);
    }
}