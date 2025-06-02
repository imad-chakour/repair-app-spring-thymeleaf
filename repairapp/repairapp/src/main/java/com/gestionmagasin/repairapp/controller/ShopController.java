package com.gestionmagasin.repairapp.controller;

import com.gestionmagasin.repairapp.model.Shop;
import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.service.ShopService;
import com.gestionmagasin.repairapp.service.UserService;
import com.gestionmagasin.repairapp.dto.ShopCreateRequest;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Contrôleur gérant les opérations liées aux magasins
 * @RestController : Indique que c'est un contrôleur REST
 * @RequestMapping : Route de base pour les magasins
 */
@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    @PostMapping
    @PreAuthorize("hasAuthority('PROPRIETAIRE')")
    public ResponseEntity<Shop> createShop(@RequestBody Shop shop, Authentication authentication) {
        String userEmail = authentication.getName();
        User owner = userService.findByEmail(userEmail);
        
        if (owner == null) {
            logger.error("Utilisateur non trouvé pour l'email: {}", userEmail);
            return ResponseEntity.badRequest().build();
        }
        
        shop.setOwner(owner);
        Shop createdShop = shopService.createShop(shop);
        return ResponseEntity.created(URI.create("/shops/" + createdShop.getId())).body(createdShop);
    }

    @PostMapping("/multiple")
    @PreAuthorize("hasAuthority('PROPRIETAIRE')")
    public ResponseEntity<List<Shop>> createMultipleShops(@RequestBody List<Shop> shops, Authentication authentication) {
        String userEmail = authentication.getName();
        User owner = userService.findByEmail(userEmail);
        
        if (owner == null) {
            logger.error("Utilisateur non trouvé pour l'email: {}", userEmail);
            return ResponseEntity.badRequest().build();
        }

        List<Shop> createdShops = new ArrayList<>();
        for (Shop shop : shops) {
            shop.setOwner(owner);
            createdShops.add(shopService.createShop(shop));
        }
        
        return ResponseEntity.ok(createdShops);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROPRIETAIRE')")
    public ResponseEntity<List<Shop>> getAllShops(Authentication authentication) {
        String userEmail = authentication.getName();
        User owner = userService.findByEmail(userEmail);
        
        if (owner == null) {
            logger.error("Utilisateur non trouvé pour l'email: {}", userEmail);
            return ResponseEntity.badRequest().build();
        }
        
        List<Shop> shops = shopService.getShopsByOwner(owner);
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETAIRE')")
    public ResponseEntity<Shop> getShopById(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        User owner = userService.findByEmail(userEmail);
        
        if (owner == null) {
            logger.error("Utilisateur non trouvé pour l'email: {}", userEmail);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Shop> shopOpt = shopService.getShopById(id);
        if (shopOpt.isEmpty()) {
            logger.warn("Boutique {} non trouvée", id);
            return ResponseEntity.notFound().build();
        }

        Shop shop = shopOpt.get();
        if (shop.getOwner() == null || !shop.getOwner().getId().equals(owner.getId())) {
            logger.warn("Accès non autorisé à la boutique {} par l'utilisateur {}", id, userEmail);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(shop);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETAIRE')")
    public ResponseEntity<Shop> updateShop(@PathVariable Long id, @RequestBody Shop shopDetails, Authentication authentication) {
        String userEmail = authentication.getName();
        User owner = userService.findByEmail(userEmail);
        
        if (owner == null) {
            logger.error("Utilisateur non trouvé pour l'email: {}", userEmail);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Shop> shopOpt = shopService.getShopById(id);
        if (shopOpt.isEmpty()) {
            logger.warn("Boutique {} non trouvée", id);
            return ResponseEntity.notFound().build();
        }

        Shop existingShop = shopOpt.get();
        if (existingShop.getOwner() == null || !existingShop.getOwner().getId().equals(owner.getId())) {
            logger.warn("Accès non autorisé à la boutique {} par l'utilisateur {}", id, userEmail);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        shopDetails.setId(id);
        shopDetails.setOwner(owner);
        Shop updatedShop = shopService.updateShop(id, shopDetails);
        return ResponseEntity.ok(updatedShop);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETAIRE')")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        User owner = userService.findByEmail(userEmail);
        
        if (owner == null) {
            logger.error("Utilisateur non trouvé pour l'email: {}", userEmail);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Shop> shopOpt = shopService.getShopById(id);
        if (shopOpt.isEmpty()) {
            logger.warn("Boutique {} non trouvée", id);
            return ResponseEntity.notFound().build();
        }

        Shop shop = shopOpt.get();
        if (shop.getOwner() == null || !shop.getOwner().getId().equals(owner.getId())) {
            logger.warn("Accès non autorisé à la boutique {} par l'utilisateur {}", id, userEmail);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        shopService.deleteShop(id);
        return ResponseEntity.ok().build();
    }
}
