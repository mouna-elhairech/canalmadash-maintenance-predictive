package com.canal.canalmadash.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ImageStorageService {

    /*
     * Service responsable du stockage physique des images.
     * La base de donnees garde seulement le chemin relatif retourne ici,
     * pas le contenu binaire du fichier.
     */
    private final Path rootLocation;

    public ImageStorageService(@Value("${app.storage.root:uploads}") String rootDir) {
        // Le dossier racine est configurable; par defaut les fichiers vont dans uploads/.
        this.rootLocation = Paths.get(rootDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage d'images", e);
        }
    }

    // -------- MACHINE --------
    public String storeMachineImage(int machineId, MultipartFile file) {
        // Image principale d'une machine, rangee dans uploads/machines.
        return store(file, "machines", "machine_" + machineId);
    }

    // -------- MAINTENANCE --------
    public String storeMaintenanceImage(Long maintenanceId, MultipartFile file) {
        // Image principale d'une intervention de maintenance.
        return store(file, "maintenances", "maintenance_" + maintenanceId);
    }

    public String storeMaintenanceAvantImage(Long maintenanceId, MultipartFile file) {
        // Image prise avant l'intervention pour documenter l'etat initial.
        return store(file, "maintenances", "maintenance_" + maintenanceId + "_avant");
    }

    public String storeMaintenanceApresImage(Long maintenanceId, MultipartFile file) {
        // Image prise apres l'intervention pour prouver le resultat.
        return store(file, "maintenances", "maintenance_" + maintenanceId + "_apres");
    }

    // -------- DEMANDE MAINTENANCE --------
    public String storeDemandeImage(int demandeId, MultipartFile file) {
        // Image ajoutee par l'operateur pour illustrer une demande.
        return store(file, "demandes", "demande_" + demandeId);
    }

    // -------- GÉNÉRIQUE --------
    private String store(MultipartFile file, String folder, String prefix) {
        /*
         * Methode generique de sauvegarde.
         * Le nom final contient un prefixe metier, un timestamp et un suffixe
         * aleatoire pour eviter les collisions.
         */
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Fichier image vide");
        }

        try {
            Path dir = this.rootLocation.resolve(folder);
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String random = UUID.randomUUID().toString().substring(0, 8);

            String filename = prefix + "_" + timestamp + "_" + random + ext;
            Path destination = dir.resolve(filename).normalize();

            // REPLACE_EXISTING protege contre un cas tres rare de collision de nom.
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return folder + "/" + filename; // chemin relatif stocké en DB

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier image", e);
        }
    }

    public Resource loadAsResource(String relativePath) {
        // Transforme le chemin relatif stocke en base en ressource telechargeable par Spring.
        try {
            if (relativePath == null || relativePath.isBlank()) {
                return null;
            }
            Path file = rootLocation.resolve(relativePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void delete(String relativePath) {
        // Suppression silencieuse: l'absence du fichier ne doit pas bloquer l'action metier.
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path file = rootLocation.resolve(relativePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // On ignore volontairement pour ne pas casser la suppression de l'objet metier.
        }
    }
}
