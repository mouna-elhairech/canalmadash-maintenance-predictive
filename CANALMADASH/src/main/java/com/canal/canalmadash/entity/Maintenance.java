package com.canal.canalmadash.entity;

import com.canal.canalmadash.Enum.TypeIntervention;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Maintenance {

    /*
     * Intervention realisee sur une machine.
     * Elle contient le diagnostic, les pieces utilisees, les controles effectues,
     * le cout final, les images avant/apres et les informations fournisseur
     * quand l'intervention est externe.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Machine concernee. Ignoree en JSON pour eviter les cycles dans les reponses API.
    @JsonIgnore
    @ManyToOne(optional = false)
    private Machine machine;

    @ManyToOne(optional = false)
    private TypeMaintenance type;

    private String description;

    private String note;

    private String imagePath;
    private String imageAvantPath;
    private String imageApresPath;

    // Ancien champ texte libre; garde pour compatibilite avec les saisies simples.
    @Column(length = 1000)
    private String piecesUtilisees;

    // Nouvelle structure detaillee: chaque piece a sa quantite, son cout et ses retours.
    @OneToMany(mappedBy = "maintenance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenancePieceUtilisee> pieces = new ArrayList<>();

    private Double cout;

    private String causePanne;

    @Column(length = 1000)
    private String checklist;

    private Boolean controleEffectue = false;
    private Boolean nettoyageEffectue = false;
    private Boolean testFinalEffectue = false;
    private String validationSignature;

    private String user;

    @Enumerated(EnumType.STRING)
    private TypeIntervention typeIntervention = TypeIntervention.INTERNE;

    // Champs utilises uniquement si typeIntervention vaut EXTERNE.
    private String fournisseurNom;
    private String fournisseurContact;
    private String fournisseurTelephone;
    private String fournisseurEmail;
    private String fournisseurAdresse;
    private String referenceDevis;

    @Column(length = 1500)
    private String messageDemandeExterne;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    // Demande d'origine si l'intervention a ete creee depuis une demande.
    @JsonIgnore
    @OneToOne
    private DemandeMaintenance demandeMaintenance;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public TypeMaintenance getType() {
        return type;
    }

    public void setType(TypeMaintenance type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageAvantPath() {
        return imageAvantPath;
    }

    public void setImageAvantPath(String imageAvantPath) {
        this.imageAvantPath = imageAvantPath;
    }

    public String getImageApresPath() {
        return imageApresPath;
    }

    public void setImageApresPath(String imageApresPath) {
        this.imageApresPath = imageApresPath;
    }

    public String getPiecesUtilisees() {
        return piecesUtilisees;
    }

    public void setPiecesUtilisees(String piecesUtilisees) {
        this.piecesUtilisees = piecesUtilisees;
    }

    public List<MaintenancePieceUtilisee> getPieces() {
        return pieces;
    }

    public void setPieces(List<MaintenancePieceUtilisee> pieces) {
        this.pieces = pieces;
    }

    public Double getCout() {
        return cout;
    }

    public void setCout(Double cout) {
        this.cout = cout;
    }

    public double getCoutPieces() {
        // Somme le cout net des pieces, apres deduction des retours eventuels.
        return pieces.stream()
                .mapToDouble(MaintenancePieceUtilisee::getCoutNet)
                .sum();
    }

    public String getCausePanne() {
        return causePanne;
    }

    public void setCausePanne(String causePanne) {
        this.causePanne = causePanne;
    }

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public Boolean getControleEffectue() {
        return controleEffectue;
    }

    public void setControleEffectue(Boolean controleEffectue) {
        this.controleEffectue = controleEffectue;
    }

    public Boolean getNettoyageEffectue() {
        return nettoyageEffectue;
    }

    public void setNettoyageEffectue(Boolean nettoyageEffectue) {
        this.nettoyageEffectue = nettoyageEffectue;
    }

    public Boolean getTestFinalEffectue() {
        return testFinalEffectue;
    }

    public void setTestFinalEffectue(Boolean testFinalEffectue) {
        this.testFinalEffectue = testFinalEffectue;
    }

    public String getValidationSignature() {
        return validationSignature;
    }

    public void setValidationSignature(String validationSignature) {
        this.validationSignature = validationSignature;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public TypeIntervention getTypeIntervention() {
        return typeIntervention;
    }

    public void setTypeIntervention(TypeIntervention typeIntervention) {
        this.typeIntervention = typeIntervention;
    }

    public String getFournisseurNom() {
        return fournisseurNom;
    }

    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }

    public String getFournisseurContact() {
        return fournisseurContact;
    }

    public void setFournisseurContact(String fournisseurContact) {
        this.fournisseurContact = fournisseurContact;
    }

    public String getFournisseurTelephone() {
        return fournisseurTelephone;
    }

    public void setFournisseurTelephone(String fournisseurTelephone) {
        this.fournisseurTelephone = fournisseurTelephone;
    }

    public String getFournisseurEmail() {
        return fournisseurEmail;
    }

    public void setFournisseurEmail(String fournisseurEmail) {
        this.fournisseurEmail = fournisseurEmail;
    }

    public String getFournisseurAdresse() {
        return fournisseurAdresse;
    }

    public void setFournisseurAdresse(String fournisseurAdresse) {
        this.fournisseurAdresse = fournisseurAdresse;
    }

    public String getReferenceDevis() {
        return referenceDevis;
    }

    public void setReferenceDevis(String referenceDevis) {
        this.referenceDevis = referenceDevis;
    }

    public String getMessageDemandeExterne() {
        return messageDemandeExterne;
    }

    public void setMessageDemandeExterne(String messageDemandeExterne) {
        this.messageDemandeExterne = messageDemandeExterne;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public DemandeMaintenance getDemandeMaintenance() {
        return demandeMaintenance;
    }

    public void setDemandeMaintenance(DemandeMaintenance demandeMaintenance) {
        this.demandeMaintenance = demandeMaintenance;
    }
}
