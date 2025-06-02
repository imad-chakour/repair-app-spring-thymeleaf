package com.gestionmagasin.repairapp.service;

import com.gestionmagasin.repairapp.model.Shop;
import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ShopService {
    
    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);
    
    private final ShopRepository shopRepository;
    private final UserService userService;
    
    @Transactional(readOnly = true)
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Shop> getShopById(Long id) {
        return shopRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Shop> getShopsByOwner(User owner) {
        return shopRepository.findByOwner(owner);
    }
    
    @Transactional
    public Shop createShop(Shop shop) {
        Shop savedShop = shopRepository.save(shop);
        return savedShop;
    }
    
    @Transactional
    public Shop updateShop(Long id, Shop shopDetails) {
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Boutique non trouvée"));
            
        shop.setName(shopDetails.getName());
        shop.setAddress(shopDetails.getAddress());
        shop.setPhone(shopDetails.getPhone());
        shop.setEmail(shopDetails.getEmail());
        shop.setPatente(shopDetails.getPatente());

        return shopRepository.save(shop);
    }
    
    @Transactional
    public void deleteShop(Long id) {
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Boutique non trouvée"));
            
        // Mettre à jour la liste des boutiques du propriétaire
        User owner = shop.getOwner();
        if (owner != null && owner.getOwnedShops() != null) {
            owner.getOwnedShops().remove(shop);
            userService.saveUser(owner);
        }
        
        shopRepository.deleteById(id);
    }

    @Transactional
    public Shop saveShop(Shop shop) {
        return shopRepository.save(shop);
    }

    @Transactional
    public void deleteShop(Shop shop) {
        try {
            // Get managed instance
            Shop managedShop = shopRepository.findById(shop.getId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

            // Remove all repairers from the shop
            if (managedShop.getRepairers() != null) {
                new ArrayList<>(managedShop.getRepairers()).forEach(repairer -> {
                    repairer.setWorkingShop(null);
                    userService.saveUser(repairer);
                });
            }

            // Remove owner association
            if (managedShop.getOwner() != null) {
                User owner = managedShop.getOwner();
                owner.getOwnedShops().remove(managedShop);
                userService.saveUser(owner);
            }

            // Delete the shop
            shopRepository.delete(managedShop);
            shopRepository.flush();
        } catch (Exception e) {
            logger.error("Error deleting shop: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete shop: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return shopRepository.existsByEmail(email);
    }

    @Transactional
    public void assignRepairer(Shop shop, User repairer) {
        try {
            logger.debug("Starting assignRepairer process for shop {} and user {}", shop.getId(), repairer.getId());
            
            Shop managedShop = shopRepository.findById(shop.getId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            // Remove from previous shop if any
            Shop previousShop = repairer.getWorkingShop();
            if (previousShop != null && !previousShop.equals(managedShop)) {
                logger.debug("Removing repairer from previous shop {}", previousShop.getId());
                removeRepairer(previousShop, repairer);
            }
            
            logger.debug("Adding repairer to shop {}", managedShop.getId());
            managedShop.addRepairer(repairer);
            shopRepository.saveAndFlush(managedShop);
            
            logger.debug("Successfully assigned repairer to shop");
        } catch (Exception e) {
            logger.error("Error assigning repairer to shop: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to assign repairer to shop", e);
        }
    }

    @Transactional
    public void removeRepairer(Shop shop, User repairer) {
        try {
            logger.debug("Starting removeRepairer process for shop {} and user {}", shop.getId(), repairer.getId());
            
            Shop managedShop = shopRepository.findById(shop.getId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            logger.debug("Removing repairer from shop {}", managedShop.getId());
            managedShop.removeRepairer(repairer);
            shopRepository.saveAndFlush(managedShop);
            
            logger.debug("Successfully removed repairer from shop");
        } catch (Exception e) {
            logger.error("Error removing repairer from shop: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove repairer from shop", e);
        }
    }
} 