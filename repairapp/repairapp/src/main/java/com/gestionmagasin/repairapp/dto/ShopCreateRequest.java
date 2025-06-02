package com.gestionmagasin.repairapp.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data // Lombok génère getters/setters/constructeurs
public class ShopCreateRequest {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String phone;

    @NotBlank(message = "Le numéro de patente est obligatoire")
    private String patente;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;
}

