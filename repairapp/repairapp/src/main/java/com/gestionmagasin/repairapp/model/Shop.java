package com.gestionmagasin.repairapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String phone;
    private String email;
    private String patente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "workingShop", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    @Builder.Default
    private List<User> repairers = new ArrayList<>();

    public void addRepairer(User repairer) {
        if (repairers == null) {
            repairers = new ArrayList<>();
        }
        if (!repairers.contains(repairer)) {
            repairers.add(repairer);
            repairer.setWorkingShop(this);
        }
    }

    public void removeRepairer(User repairer) {
        if (repairers != null && repairers.contains(repairer)) {
            repairers.remove(repairer);
            if (repairer.getWorkingShop() == this) {
                repairer.setWorkingShop(null);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shop shop = (Shop) o;
        return Objects.equals(id, shop.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", patente='" + patente + '\'' +
                ", email='" + email + '\'' +
                ", owner=" + (owner != null ? owner.getId() : "null") +
                ", repairersCount=" + (repairers != null ? repairers.size() : 0) +
                '}';
    }
}
