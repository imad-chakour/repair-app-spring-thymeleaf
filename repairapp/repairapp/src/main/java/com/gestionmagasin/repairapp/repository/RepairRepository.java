package com.gestionmagasin.repairapp.repository;

import com.gestionmagasin.repairapp.model.Repair;
import com.gestionmagasin.repairapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {
    List<Repair> findByRepairer(User repairer);
    List<Repair> findByRepairerOrderByCreatedAtDesc(User repairer);
    Optional<Repair> findByTrackingCode(String trackingCode);
}
