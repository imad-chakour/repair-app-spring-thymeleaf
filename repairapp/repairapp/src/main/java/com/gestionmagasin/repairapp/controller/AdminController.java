package com.gestionmagasin.repairapp.controller;

import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.model.Shop;
import com.gestionmagasin.repairapp.model.Role;
import com.gestionmagasin.repairapp.service.UserService;
import com.gestionmagasin.repairapp.service.ShopService;
import com.gestionmagasin.repairapp.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur gérant les fonctionnalités d'administration
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMINISTRATOR')")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    /**
     * Injection des services nécessaires via constructeur
     * Meilleure pratique Spring pour l'injection de dépendances
     */
    private final UserService userService;
    private final ShopService shopService;
    private final RepairService repairService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Point d'entrée pour le tableau de bord d'administration
     * @return Vue du tableau de bord d'administration
     */
    @GetMapping
    public String adminDashboard(Model model) {
        try {
            // Log authentication details
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("Current user: {}", auth.getName());
            logger.info("User authorities: {}", auth.getAuthorities());

            // Log the start of dashboard loading
            logger.info("Starting to load admin dashboard");

            // Get users with logging
            logger.debug("Fetching all users");
            List<User> allUsers = userService.getAllUsers();
            logger.debug("Found {} users", allUsers.size());

            // Get shops with logging
            logger.debug("Fetching all shops");
            List<Shop> allShops = shopService.getAllShops();
            logger.debug("Found {} shops", allShops.size());

            // Get total repairs count
            logger.debug("Fetching total repairs count");
            long totalRepairs = repairService.getTotalRepairsCount();
            logger.debug("Found {} total repairs", totalRepairs);

            // Add attributes to model
            logger.debug("Adding attributes to model");
            model.addAttribute("users", allUsers);
            model.addAttribute("shops", allShops);
            model.addAttribute("totalRepairs", totalRepairs);
            long nbReparateurs = allUsers.stream().filter(u -> u.getRole() == Role.REPARATEUR).count();
            long nbAdmins = allUsers.stream().filter(u -> u.getRole() == Role.ADMINISTRATOR).count();
            model.addAttribute("nbReparateurs", nbReparateurs);
            model.addAttribute("nbAdmins", nbAdmins);

            // Log successful completion
            logger.info("Admin dashboard loaded successfully");
            return "admin/dashboard";
        } catch (Exception e) {
            logger.error("Error loading admin dashboard: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load admin dashboard: " + e.getMessage(), e);
        }
    }

    /**
     * Point d'entrée pour la gestion des utilisateurs
     * @return Vue de la page de gestion des utilisateurs
     */
    @GetMapping("/users-management")
    public String usersManagement(Model model) {
        try {
            logger.info("Loading users management page");
            List<User> allUsers = userService.getAllUsers();
            List<Shop> allShops = shopService.getAllShops();
            
            model.addAttribute("users", allUsers);
            model.addAttribute("shops", allShops);
            
            logger.info("Users management page loaded successfully");
            return "admin/users";
        } catch (Exception e) {
            logger.error("Error loading users management page: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load users management page: " + e.getMessage(), e);
        }
    }

    /**
     * @return Liste de tous les utilisateurs
     */
    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * @param id ID de l'utilisateur à récupérer
     * @return Utilisateur correspondant à l'ID
     */
    @GetMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    /**
     * @return Liste de tous les magasins
     */
    @GetMapping("/shops-list")
    @ResponseBody
    public ResponseEntity<List<Shop>> getAllShops() {
        return ResponseEntity.ok(shopService.getAllShops());
    }

    /**
     * @param id ID du magasin à récupérer
     * @return Magasin correspondant à l'ID
     */
    @GetMapping("/shops/{id}")
    @ResponseBody
    public ResponseEntity<?> getShop(@PathVariable Long id) {
        Shop shop = shopService.getShopById(id)
            .orElseThrow(() -> new RuntimeException("Shop not found"));
        return ResponseEntity.ok(shop);
    }

    /**
     * @param payload DTO contenant les informations du nouvel utilisateur
     * @return Réponse HTTP avec le nouvel utilisateur créé
     */
    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> payload) {
        try {
            User user = new User();
            user.setFullName((String) payload.get("fullName"));
            user.setEmail((String) payload.get("email"));
            user.setRole(Role.valueOf((String) payload.get("role")));
            
            // Set username to email if not provided
            user.setUsername(user.getEmail());

            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            // Handle password
            String password = (String) payload.get("password");
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }
            user.setPassword(passwordEncoder.encode(password));

            // Save the user first
            User savedUser = userService.saveUser(user);

            // Handle shop association for repairers
            if (user.getRole() == Role.REPARATEUR) {
                String shopId = (String) payload.get("shopId");
                if (shopId == null || shopId.isEmpty()) {
                    return ResponseEntity.badRequest().body("Shop selection is required for repairers");
                }
                
                Shop shop = shopService.getShopById(Long.parseLong(shopId))
                    .orElseThrow(() -> new RuntimeException("Shop not found"));
                
                shopService.assignRepairer(shop, savedUser);
            }

            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating user: " + e.getMessage());
        }
    }

    /**
     * @param id ID de l'utilisateur à mettre à jour
     * @param payload DTO contenant les nouvelles informations de l'utilisateur
     * @return Réponse HTTP avec l'utilisateur mis à jour
     */
    @PutMapping("/users/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            User existingUser = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

            String newEmail = (String) payload.get("email");
            // Check if email is being changed and if it's already taken
            if (!existingUser.getEmail().equals(newEmail) && 
                userService.existsByEmail(newEmail)) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            // Update user fields
            existingUser.setFullName((String) payload.get("fullName"));
            existingUser.setEmail(newEmail);
            existingUser.setUsername(newEmail);
            Role newRole = Role.valueOf((String) payload.get("role"));
            
            // Handle password if provided
            String password = (String) payload.get("password");
            if (password != null && !password.isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(password));
            }

            // Save the user first to ensure it's managed
            existingUser = userService.saveUser(existingUser);

            // Handle shop association for repairers
            Shop currentShop = existingUser.getWorkingShop();
            if (newRole == Role.REPARATEUR) {
                String shopId = (String) payload.get("shopId");
                if (shopId == null || shopId.isEmpty()) {
                    return ResponseEntity.badRequest().body("Shop selection is required for repairers");
                }
                
                Shop newShop = shopService.getShopById(Long.parseLong(shopId))
                    .orElseThrow(() -> new RuntimeException("Shop not found"));

                // Only change shop if it's different
                if (currentShop == null || !currentShop.getId().equals(newShop.getId())) {
                    logger.debug("Changing repairer's shop from {} to {}", 
                        currentShop != null ? currentShop.getId() : "null", 
                        newShop.getId());
                    shopService.assignRepairer(newShop, existingUser);
                }
            } else if (currentShop != null) {
                // Remove from current shop if role is changed from REPARATEUR
                logger.debug("Removing repairer from shop {} due to role change", currentShop.getId());
                shopService.removeRepairer(currentShop, existingUser);
            }

            // Set the role after handling shop associations
            existingUser.setRole(newRole);
            existingUser = userService.saveUser(existingUser);

            return ResponseEntity.ok(existingUser);
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating user: " + e.getMessage());
        }
    }

    /**
     * @param id ID de l'utilisateur à supprimer
     * @return Réponse HTTP avec la suppression effectuée
     */
    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

            userService.deleteUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * @param shop DTO contenant les informations du nouveau magasin
     * @return Réponse HTTP avec le nouveau magasin créé
     */
    @PostMapping("/shops")
    @ResponseBody
    public ResponseEntity<?> createShop(@RequestBody Shop shop) {
        if (shopService.existsByEmail(shop.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        Shop savedShop = shopService.saveShop(shop);
        return ResponseEntity.ok(savedShop);
    }

    /**
     * @param id ID du magasin à mettre à jour
     * @param shop DTO contenant les nouvelles informations du magasin
     * @return Réponse HTTP avec le magasin mis à jour
     */
    @PutMapping("/shops/{id}")
    @ResponseBody
    public ResponseEntity<?> updateShop(@PathVariable Long id, @RequestBody Shop shop) {
        Shop existingShop = shopService.getShopById(id)
            .orElseThrow(() -> new RuntimeException("Shop not found"));

        existingShop.setName(shop.getName());
        existingShop.setAddress(shop.getAddress());
        existingShop.setPhone(shop.getPhone());
        existingShop.setEmail(shop.getEmail());
        existingShop.setPatente(shop.getPatente());

        Shop updatedShop = shopService.saveShop(existingShop);
        return ResponseEntity.ok(updatedShop);
    }

    /**
     * @param id ID du magasin à supprimer
     * @return Réponse HTTP avec la suppression effectuée
     */
    @DeleteMapping("/shops/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteShop(@PathVariable Long id) {
        try {
            Shop shop = shopService.getShopById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

            shopService.deleteShop(shop);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting shop: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        logger.error("Error in admin controller: {}", e.getMessage(), e);
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @GetMapping("/shops")
    public String showShops(Model model) {
        List<Shop> shops = shopService.getAllShops();
        List<User> owners = userService.getAllOwners();
        model.addAttribute("shops", shops);
        model.addAttribute("owners", owners);
        return "admin/shops";
    }
} 