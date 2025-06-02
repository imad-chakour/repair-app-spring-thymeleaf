package com.gestionmagasin.repairapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @Entity : Marque cette classe comme une entité JPA (sera mappée à une table en base de données)
 * @Table : Spécifie le nom de la table en base de données
 * @Data : Annotation Lombok qui génère automatiquement getters, setters, equals, hashCode et toString
 * @Builder : Pattern Builder de Lombok pour créer des instances de manière fluide
 * UserDetails : Interface Spring Security pour les informations d'authentification
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    /**
     * Identifiant unique de l'utilisateur
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Prénom de l'utilisateur
     */
    @NotBlank(message = "Username is mandatory")
    @Column(unique = true)
    private String username;

    /**
     * Nom de famille de l'utilisateur
     */
    @NotBlank(message = "Full name is mandatory")
    private String fullName;

    /**
     * Email de l'utilisateur, utilisé comme identifiant unique pour l'authentification
     */
    @NotBlank(message = "Email is mandatory")
    @Email
    @Column(unique = true)
    private String email;

    /**
     * Mot de passe de l'utilisateur (stocké de manière cryptée)
     */
    @NotBlank(message = "Password is mandatory")
    private String password;

    /**
     * Rôle de l'utilisateur dans le système (ADMIN, REPAIRER, USER)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Relation OneToMany avec Shop : Un utilisateur peut posséder plusieurs magasins
     * mappedBy : Indique que la relation est gérée par le champ "owner" dans Shop
     * fetch = FetchType.EAGER : Les magasins sont chargés immédiatement avec l'utilisateur
     * @JsonIgnore : Évite les cycles infinis lors de la sérialisation JSON
     */
    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    @JsonIgnore
    @Builder.Default
    private List<Shop> ownedShops = new ArrayList<>();

    /**
     * Relation ManyToOne avec Shop : Plusieurs utilisateurs peuvent travailler dans un magasin
     * fetch = FetchType.EAGER : Le magasin est chargé immédiatement avec l'utilisateur
     * @JoinColumn : Spécifie la colonne de jointure dans la table users
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shop_id")
    @JsonIgnore
    private Shop workingShop;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Méthodes de gestion des relations bidirectionnelles Hibernate
     * Ces méthodes maintiennent la cohérence des deux côtés de la relation
     */
    public void addOwnedShop(Shop shop) {
        if (ownedShops == null) {
            ownedShops = new ArrayList<>();
        }
        ownedShops.add(shop);
        shop.setOwner(this);
    }

    public void removeOwnedShop(Shop shop) {
        ownedShops.remove(shop);
        if (shop.getOwner() == this) {
            shop.setOwner(null);
        }
    }

    public void setWorkingShop(Shop shop) {
        if (this.workingShop != null) {
            this.workingShop.getRepairers().remove(this);
        }
        this.workingShop = shop;
        if (shop != null) {
            shop.getRepairers().add(this);
        }
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String Username) { this.username = Username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", role=" + role +
               ", ownedShopsCount=" + (ownedShops != null ? ownedShops.size() : 0) +
               ", shop=" + (workingShop != null ? workingShop.getId() : "null") +
               '}';
    }
}
