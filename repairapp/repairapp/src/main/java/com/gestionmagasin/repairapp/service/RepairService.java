package com.gestionmagasin.repairapp.service;

import com.gestionmagasin.repairapp.dto.RepairRequest;
import com.gestionmagasin.repairapp.model.Repair;
import com.gestionmagasin.repairapp.model.RepairStatus;
import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.repository.RepairRepository;
import com.gestionmagasin.repairapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * @Service : Marque cette classe comme un bean de service Spring
 * - Composant de la couche métier
 * - Automatiquement détecté par le scan des composants Spring
 * 
 * @RequiredArgsConstructor : Génère un constructeur avec injection de dépendances automatique
 * pour les champs finals (injection par constructeur recommandée par Spring)
 */
@Service
@RequiredArgsConstructor
public class RepairService {

    /**
     * Injection de dépendances des repositories
     * Spring injecte automatiquement les implémentations au démarrage
     */
    private final RepairRepository repairRepository;
    private final UserRepository userRepository;

    /**
     * @Transactional(readOnly = true) : 
     * - Gère automatiquement la transaction de base de données
     * - readOnly = true optimise les performances pour les opérations de lecture
     * - Spring rollback automatiquement en cas d'exception
     */
    @Transactional(readOnly = true)
    public List<Repair> getAllRepairs() {
        return repairRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Repair> getRepairsByRepairer(User repairer) {
        return repairRepository.findByRepairerOrderByCreatedAtDesc(repairer);
    }

    @Transactional(readOnly = true)
    public Optional<Repair> getRepairById(Long id) {
        return repairRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Repair> getRepairByTrackingCode(String trackingCode) {
        return repairRepository.findByTrackingCode(trackingCode);
    }

    /**
     * @Transactional : 
     * - Garantit l'atomicité des opérations de base de données
     * - Gère automatiquement les sessions Hibernate
     * - Commit automatique si succès, rollback si exception
     */
    @Transactional
    public Repair createRepair(RepairRequest request, User repairer) {
        Repair repair = new Repair();
        updateRepairFromRequest(repair, request);
        repair.setRepairer(repairer);
        repair.setStatus(RepairStatus.EN_COURS);
        return repairRepository.save(repair);
    }

    /**
     * Met à jour le statut d'une réparation
     * @param id Identifiant de la réparation
     * @param status Nouveau statut de la réparation
     * @return La réparation mise à jour
     */
    @Transactional
    public Optional<Repair> updateRepair(Long id, RepairRequest request) {
        return repairRepository.findById(id)
                .map(repair -> {
                    updateRepairFromRequest(repair, request);
                    return repairRepository.save(repair);
                });
    }

    @Transactional
    public void deleteRepair(Long id) {
        repairRepository.deleteById(id);
    }

    private void updateRepairFromRequest(Repair repair, RepairRequest request) {
        repair.setClientName(request.getClientName());
        repair.setDeviceType(request.getDeviceType());
        repair.setBrand(request.getBrand());
        repair.setModel(request.getModel());
        repair.setIssueDescription(request.getIssueDescription());
        repair.setCost(request.getCost());
        repair.setAdvance(request.getAdvance());
        if (request.getStatus() != null) {
            repair.setStatus(request.getStatus());
        }
    }

    /**
     * Compte le nombre total de réparations dans le système
     * @return le nombre total de réparations
     */
    public long getTotalRepairsCount() {
        return repairRepository.count();
    }
}