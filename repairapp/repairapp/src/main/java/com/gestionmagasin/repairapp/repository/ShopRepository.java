package com.gestionmagasin.repairapp.repository;

import com.gestionmagasin.repairapp.model.Shop;
import com.gestionmagasin.repairapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByOwner(User owner);
    boolean existsByEmail(String email);
}
