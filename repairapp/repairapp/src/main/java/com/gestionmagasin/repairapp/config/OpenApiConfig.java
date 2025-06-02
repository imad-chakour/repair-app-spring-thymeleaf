package com.gestionmagasin.repairapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "GestionMagasin API Documentation",
        version = "1.0",
        description = "Documentation de l'API sécurisée avec Spring Security",
        contact = @Contact(
            name = "GestionMagasin Support",
            email = "support@gestionmagasin.com",
            url = "https://gestionmagasin.com"
        ),
        license = @License(
            name = "GestionMagasin License",
            url = "https://gestionmagasin.com/license"
        ),
        termsOfService = "https://gestionmagasin.com/terms"
    ),
    servers = {
        @Server(
            description = "Environnement local",
            url = "http://localhost:8080"
        )
    }
)
public class OpenApiConfig {
}

