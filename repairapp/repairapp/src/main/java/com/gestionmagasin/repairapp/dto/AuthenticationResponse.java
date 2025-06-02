package com.gestionmagasin.repairapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gestionmagasin.repairapp.model.Role;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;

  // Ajout du rôle dans la réponse
  @JsonProperty("role")
  private Role role;

  // Constructeur avec uniquement accessToken
  public AuthenticationResponse(String accessToken) {
    this.accessToken = accessToken;
  }
}

