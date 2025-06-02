package com.gestionmagasin.repairapp.controller;

import com.gestionmagasin.repairapp.dto.RepairerCreateRequest;
import com.gestionmagasin.repairapp.model.Shop;
import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.model.Role;
import com.gestionmagasin.repairapp.service.ShopService;
import com.gestionmagasin.repairapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/repairers")
@RequiredArgsConstructor
public class RepairerController {

    private static final Logger logger = LoggerFactory.getLogger(RepairerController.class);

    private final UserService userService;
    private final ShopService shopService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/shop/{shopId}")
    @Transactional(readOnly = true)
    public String showRepairers(@PathVariable Long shopId, Model model) {
        Optional<Shop> shopOpt = shopService.getShopById(shopId);
        if (shopOpt.isEmpty()) {
            logger.warn("Shop not found with id: {}", shopId);
            return "redirect:/shop?error=true";
        }
        
        Shop shop = shopOpt.get();
        List<User> repairers = userService.getRepairersByShop(shop);
        
        model.addAttribute("shop", shop);
        model.addAttribute("repairers", repairers);
        return "repairers";
    }

    @PostMapping("/shop/{shopId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> addRepairer(@PathVariable Long shopId, @RequestBody RepairerCreateRequest request) {
        try {
            logger.info("Adding new repairer for shop: {}", shopId);
            
            Optional<Shop> shopOpt = shopService.getShopById(shopId);
            if (shopOpt.isEmpty()) {
                logger.warn("Shop not found with id: {}", shopId);
                return ResponseEntity.badRequest().body("Boutique non trouvée");
            }

            if (userService.existsByEmail(request.getEmail())) {
                logger.warn("Email already exists: {}", request.getEmail());
                return ResponseEntity.badRequest().body("Email déjà utilisé");
            }

            Shop shop = shopOpt.get();
            User repairer = new User();
            repairer.setUsername(request.getUsername());
            repairer.setFullName(request.getFullName());
            repairer.setEmail(request.getEmail());
            repairer.setPassword(passwordEncoder.encode(request.getPassword()));
            repairer.setRole(Role.REPARATEUR);
            
            // Save the repairer first to get an ID
            repairer = userService.saveUser(repairer);
            
            // Use the helper method to set up the bidirectional relationship
            repairer.setWorkingShop(shop);
            
            // Save again to update the relationship
            repairer = userService.saveUser(repairer);
            
            logger.info("Successfully created repairer with email: {}", request.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error creating repairer", e);
            return ResponseEntity.internalServerError().body("Erreur lors de la création du réparateur: " + e.getMessage());
        }
    }

    @GetMapping("/{repairerId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRepairer(@PathVariable Long repairerId) {
        try {
            Optional<User> repairerOpt = userService.getUserById(repairerId);
            if (repairerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Réparateur non trouvé");
            }

            User repairer = repairerOpt.get();
            if (repairer.getRole() != Role.REPARATEUR) {
                return ResponseEntity.badRequest().body("L'utilisateur n'est pas un réparateur");
            }

            return ResponseEntity.ok(repairer);
        } catch (Exception e) {
            logger.error("Error getting repairer details", e);
            return ResponseEntity.internalServerError().body("Erreur lors de la récupération des données: " + e.getMessage());
        }
    }

    @PutMapping("/{repairerId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> updateRepairer(@PathVariable Long repairerId, @RequestBody RepairerCreateRequest request) {
        try {
            Optional<User> repairerOpt = userService.getUserById(repairerId);
            if (repairerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Réparateur non trouvé");
            }

            User repairer = repairerOpt.get();
            if (repairer.getRole() != Role.REPARATEUR) {
                return ResponseEntity.badRequest().body("L'utilisateur n'est pas un réparateur");
            }

            // Check if email is being changed and if it's already in use
            if (!repairer.getEmail().equals(request.getEmail()) && userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email déjà utilisé");
            }

            repairer.setUsername(request.getUsername());
            repairer.setFullName(request.getFullName());
            repairer.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                repairer.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userService.saveUser(repairer);
            logger.info("Successfully updated repairer: {}", repairerId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating repairer", e);
            return ResponseEntity.internalServerError().body("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @DeleteMapping("/{repairerId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteRepairer(@PathVariable Long repairerId) {
        try {
            Optional<User> repairerOpt = userService.getUserById(repairerId);
            if (repairerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Réparateur non trouvé");
            }

            User repairer = repairerOpt.get();
            if (repairer.getRole() != Role.REPARATEUR) {
                return ResponseEntity.badRequest().body("L'utilisateur n'est pas un réparateur");
            }

            userService.deleteUser(repairer);
            logger.info("Successfully deleted repairer: {}", repairerId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting repairer", e);
            return ResponseEntity.internalServerError().body("Erreur lors de la suppression: " + e.getMessage());
        }
    }
} 