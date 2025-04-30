package com.ne.template.config;

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
                contact = @Contact(
                        name = "Yves HAKIZIMANA",
                        email = "yhakizimana@rca.ac.rw",
                        url = "https://rca.ac.rw"
                ),
                description = "OpenApi Documentation For EUCL Token Meter Generation.",
                title = "OpenApi Specification - EUCL Token Meter Generation.",
                version = "1.0",
                license = @License(
                        name = "Apache-License",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                termsOfService = "Terms of Service."
        ),
        servers = {
                @Server(
                        description = "dev ENV",
                        url = "http://localhost:8080"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
