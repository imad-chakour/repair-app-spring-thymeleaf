package com.gestionmagasin.repairapp.controller;

import com.gestionmagasin.repairapp.dto.RegisterRequest;
import com.gestionmagasin.repairapp.model.*;
import com.gestionmagasin.repairapp.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists");
            }            User user = new User();
            user.setUsername(request.getUsername());
            user.setFullName(request.getFullname());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.PROPRIETAIRE);  // Set default role as PROPRIETAIRE

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "role", request.getRole()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    @PostMapping(value = "/login")
    @Transactional
    public ResponseEntity<?> loginUser(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            HttpSession session) {
        try {
            if (email == null || password == null) {
                return ResponseEntity.badRequest().body("Email et mot de passe requis");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("USER_ROLE", user.getRole());

            return ResponseEntity.ok()
                    .header("Location", "/shop")
                    .body(Map.of(
                        "success", true,
                        "role", user.getRole()));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("Location", "/login?error=true")
                    .body("Email ou mot de passe invalide");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'authentification : " + e.getMessage());
        }
    }
}




