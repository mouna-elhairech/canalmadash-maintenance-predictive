-- Table des utilisateurs (authentification JWT + BCrypt).
-- Optionnel : Hibernate cree deja cette table au demarrage du backend (ddl-auto=update). Script fourni pour reference.

CREATE TABLE IF NOT EXISTS utilisateur (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  mot_de_passe_hash VARCHAR(60) NOT NULL,
  role_id BIGINT NOT NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (role_id) REFERENCES role_application(id)
);

-- Creer un compte :
-- 1) Generer le hash BCrypt du mot de passe (le mot de passe ne doit jamais etre stocke en clair) :
--      python3 -c "import bcrypt; print(bcrypt.hashpw(b'MOT_DE_PASSE', bcrypt.gensalt(10)).decode())"
-- 2) Inserer le compte avec ce hash :
--      INSERT INTO utilisateur (nom, email, mot_de_passe_hash, role_id)
--      VALUES ('Nom Prenom', 'email@exemple.ma', '<HASH_BCRYPT>', 1);
--
-- role_id (table role_application, creee au demarrage) : 1 ADMIN, 2 RESPONSABLE_MAINTENANCE, 3 TECHNICIEN_MAINTENANCE, 4 OPERATEUR, 5 MAGASINIER, 6 CONSULTATION.
-- Les droits de chaque role (pages et actions) sont dans fonctionnalite_application et role_application_fonctionnalite.
