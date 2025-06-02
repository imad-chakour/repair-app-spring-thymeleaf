package com.gestionmagasin.repairapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import com.gestionmagasin.repairapp.dto.RegisterRequest;
import com.gestionmagasin.repairapp.service.ShopService;
import com.gestionmagasin.repairapp.service.UserService;
import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.model.Shop;
import com.gestionmagasin.repairapp.model.Role;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final ShopService shopService;
    private final UserService userService;

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "index";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("error", model.containsAttribute("error"));
        return "login";
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model, HttpSession session, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String userEmail = authentication.getName();
        User user = userService.findByEmail(userEmail);
        
        if (user == null) {
            return "redirect:/login";
        }

        // Redirect based on role
        if (user.getRole() == Role.ADMINISTRATOR) {
            return "redirect:/admin";
        } else if (user.getRole() == Role.PROPRIETAIRE) {
            List<Shop> ownerShops = shopService.getShopsByOwner(user);
            model.addAttribute("shops", ownerShops);
            model.addAttribute("currentUser", user);
            return "shop";
        } else {
            // For REPARATEUR role
            return "redirect:/repairs";
        }
    }

    @GetMapping("/shop")
    public String showShopPage(Model model, HttpSession session, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String userEmail = authentication.getName();
        User user = userService.findByEmail(userEmail);
        
        if (user == null) {
            return "redirect:/login";
        }

        if (user.getRole() == Role.ADMINISTRATOR) {
            return "redirect:/admin";
        }

        List<Shop> ownerShops = shopService.getShopsByOwner(user);
        model.addAttribute("shops", ownerShops);
        model.addAttribute("currentUser", user);
        
        return "shop";
    }
}