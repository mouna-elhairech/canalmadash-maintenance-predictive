package com.canal.canalmadash.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseCompatibilityMigration {

    /*
     * Petite migration de compatibilite au demarrage.
     * Elle elargit certaines colonnes enum pour eviter les erreurs si la base
     * a ete creee avec une taille trop courte dans une ancienne version.
     */
    private static final Logger log = LoggerFactory.getLogger(DatabaseCompatibilityMigration.class);

    @Bean
    ApplicationRunner migrateEnumColumns(JdbcTemplate jdbcTemplate) {
        // On ignore l'erreur si les colonnes sont deja au bon format ou si la table n'existe pas encore.
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE regle_maintenance_preventive MODIFY COLUMN type_regle VARCHAR(50)");
                jdbcTemplate.execute("ALTER TABLE regle_maintenance_preventive MODIFY COLUMN unite VARCHAR(30)");
            } catch (Exception ex) {
                log.debug("Migration compatibilite regles preventives ignoree: {}", ex.getMessage());
            }
        };
    }
}
