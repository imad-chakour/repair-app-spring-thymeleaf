package com.gestionmagasin.repairapp.controller;

import com.gestionmagasin.repairapp.dto.RepairRequest;
import com.gestionmagasin.repairapp.model.Repair;
import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.service.RepairService;
import com.gestionmagasin.repairapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/repairs")
@RequiredArgsConstructor
public class RepairController {

    private static final Logger logger = LoggerFactory.getLogger(RepairController.class);
    private final RepairService repairService;
    private final UserService userService;

    @GetMapping("/track")
    public String showTrackingPage() {
        return "track-repair";
    }

    @GetMapping
    public String showRepairs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        List<Repair> repairs = repairService.getRepairsByRepairer(user);
        model.addAttribute("repairs", repairs);
        return "repairs";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getRepair(@PathVariable Long id) {
        return repairService.getRepairById(id)
                .map(repair -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", repair.getId());
                    response.put("trackingCode", repair.getTrackingCode());
                    response.put("clientName", repair.getClientName());
                    response.put("deviceType", repair.getDeviceType());
                    response.put("brand", repair.getBrand());
                    response.put("model", repair.getModel());
                    response.put("issueDescription", repair.getIssueDescription());
                    response.put("cost", repair.getCost());
                    response.put("advance", repair.getAdvance());
                    response.put("remainingPayment", repair.getRemainingPayment());
                    response.put("status", repair.getStatus());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/track/{trackingCode}")
    @ResponseBody
    public ResponseEntity<?> getRepairByTrackingCode(@PathVariable String trackingCode) {
        return repairService.getRepairByTrackingCode(trackingCode)
                .map(repair -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("clientName", repair.getClientName());
                    response.put("deviceType", repair.getDeviceType());
                    response.put("brand", repair.getBrand());
                    response.put("model", repair.getModel());
                    response.put("status", repair.getStatus());
                    response.put("remainingPayment", repair.getRemainingPayment());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createRepair(@AuthenticationPrincipal UserDetails userDetails, @RequestBody RepairRequest request) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            Repair repair = repairService.createRepair(request, user);
            logger.info("Created repair with tracking code: {} for client: {}", 
                       repair.getTrackingCode(), request.getClientName());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Réparation créée avec succès");
            response.put("trackingCode", repair.getTrackingCode());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating repair", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création de la réparation: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateRepair(@PathVariable Long id, @RequestBody RepairRequest request) {
        try {
            return repairService.updateRepair(id, request)
                    .map(repair -> {
                        logger.info("Updated repair: {} with tracking code: {}", 
                                  id, repair.getTrackingCode());
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "Réparation mise à jour avec succès");
                        response.put("trackingCode", repair.getTrackingCode());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error updating repair", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteRepair(@PathVariable Long id) {
        try {
            repairService.deleteRepair(id);
            logger.info("Deleted repair: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting repair", e);
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }
}