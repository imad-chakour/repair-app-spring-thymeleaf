package com.gestionmagasin.repairapp.repository;

import com.gestionmagasin.repairapp.model.User;
import com.gestionmagasin.repairapp.model.Role;
import com.gestionmagasin.repairapp.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByWorkingShopAndRole(Shop shop, Role role);
    List<User> findByRole(Role role);
}
