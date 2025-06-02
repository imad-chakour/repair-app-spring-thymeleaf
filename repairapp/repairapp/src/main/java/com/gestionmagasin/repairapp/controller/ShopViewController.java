package com.gestionmagasin.repairapp.controller;

import com.gestionmagasin.repairapp.model.Shop;
import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.service.ShopService;
import com.gestionmagasin.repairapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/shops-view")
public class ShopViewController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public String listShops(Model model, Authentication authentication) {
        // Récupérer l'utilisateur connecté
        String userEmail = authentication.getName();
        User user = userService.findByEmail(userEmail);

        if (user == null) {
            return "redirect:/login";
        }

        // Récupérer toutes les boutiques
        List<Shop> shops = shopService.getAllShops();
        model.addAttribute("shops", shops);
        model.addAttribute("currentUser", user);

        return "shopList";
    }
} 