package com.gestionmagasin.repairapp.service;

import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.model.Role;
import com.gestionmagasin.repairapp.model.Shop;
import com.gestionmagasin.repairapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("Utilisateur non trouvé pour l'email: " + email);
        }

        User user = userOpt.get();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // on utilise email comme username pour Spring Security
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRole()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    private List<SimpleGrantedAuthority> getAuthorities(Role role) {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getRepairersByShop(Shop shop) {
        return userRepository.findByWorkingShopAndRole(shop, Role.REPARATEUR);
    }

    @Transactional
    public void deleteUser(User user) {
        try {
            // Get managed instance
            User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Remove from working shop if user is a repairer
            if (managedUser.getWorkingShop() != null) {
                managedUser.getWorkingShop().removeRepairer(managedUser);
            }

            // Remove owned shops or reassign them
            if (managedUser.getOwnedShops() != null && !managedUser.getOwnedShops().isEmpty()) {
                managedUser.getOwnedShops().forEach(shop -> shop.setOwner(null));
            }

            // Delete the user
            userRepository.delete(managedUser);
            userRepository.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllOwners() {
        return userRepository.findByRole(Role.PROPRIETAIRE);
    }
}

