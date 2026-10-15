# Gestion de Patrimoine Touristique (GDPT)

GDPT est une application web monolithique moderne conçue avec **Spring Boot 3**, **Thymeleaf**, **Spring Security** et **Spring Data JPA**. Elle permet de répertorier des sites touristiques incontournables, de planifier et de réserver des événements (concerts, randonnées, visites nocturnes) et de recueillir les retours d'expérience des visiteurs.

---

## 🎨 Fonctionnalités Clés

### 🌍 Portail Visiteur (Public / Authentifié)
* **Recherche & Filtrage** : Exploration interactive des sites touristiques par mot-clé et par catégorie (Historique, Naturel, Culturel, Religieux, Gastronomique).
* **Fiche Détails Immersive** : Présentation détaillée des sites avec tarification, horaires d'ouverture, note moyenne et avis.
* **Réservations de Tickets** : Achat direct de places pour les événements à venir liés aux sites (avec débit dynamique de la capacité en places).
* **Avis & Notation** : Système d'évaluation des sites avec un score de 1 à 5 étoiles et commentaires textuels.
* **Espace "Mes Réservations"** : Historique personnel des tickets commandés avec option d'annulation sécurisée en un clic.

### 📊 Console d'Administration (`/admin/dashboard`)
* **KPIs analytiques** : Indicateurs visuels (nombre total de sites, d'événements, de comptes et de réservations validées).
* **Gestion des Sites (CRUD)** : Ajout, modification et suppression définitive des fiches de sites touristiques.
* **Gestion des Événements (CRUD)** : Programmation d'activités avec prix de tickets, dates et capacités maximales.
* **Suivi des Réservations** : Tableau de bord listant toutes les réservations des utilisateurs avec possibilité d'annulation administrative.

### 🔒 Sécurité & Performance
* **Spring Security 6** : Contrôle d'accès strict selon deux rôles distincts (`ROLE_ADMIN` et `ROLE_VISITEUR`).
* **Hachage BCrypt** : Sécurisation robuste des mots de passe utilisateurs.
* **Design Premium Sombre** : Feuille de style CSS 3 native et personnalisée offrant un design type *glassmorphism* soigné (sans framework lourd type Tailwind ou Bootstrap), fluide et entièrement responsive.

---

## 🛠️ Stack Technique

* **Framework principal** : Spring Boot 3.2.5
* **Moteur de templates** : Thymeleaf (avec intégration Spring Security 6)
* **Accès aux données** : Spring Data JPA / Hibernate
* **Base de données** : H2 Database (persistance via fichier local `./data/gdpt` pour le développement)
* **Chiffrement** : BCrypt Password Encoder
* **Outil de build** : Maven (avec Maven Wrapper)
* **Java** : Version 17 requis

---

## 🚀 Démarrage Rapide

### Prérequis
* Java Development Kit (JDK) 17 ou supérieur installé sur votre machine.

### Lancement du Projet
1. Ouvrez votre terminal à la racine du projet.
2. Exécutez le serveur de développement à l'aide du Maven Wrapper :
   ```bash
   ./mvnw spring-boot:run
   ```
3. Une fois que la console affiche `Started GdptApplication in ... seconds`, ouvrez votre navigateur web et accédez à :
   ```text
   http://localhost:8080
   ```

---

## 🔐 Identifiants de Démonstration

La base de données H2 est automatiquement initialisée au premier démarrage avec des données réalistes et les comptes suivants :

| Rôle | Nom d'utilisateur | Mot de passe | Actions possibles |
| :--- | :--- | :--- | :--- |
| **Administrateur** | `admin` | `adminpass` | Accès complet au Tableau de Bord Admin, CRUD complet. |
| **Visiteur 1** | `visiteur1` | `password` | Réserver des places, laisser des avis, annuler ses tickets. |
| **Visiteur 2** | `visiteur2` | `password` | Réserver des places, laisser des avis, annuler ses tickets. |

*Note : Vous pouvez également vous inscrire en tant que nouveau visiteur directement depuis la page d'inscription `/register` !*

---

## 🗃️ Base de données & Console H2

Pour visualiser les tables de la base de données durant le développement :
1. Accédez à la console H2 à l'adresse : `http://localhost:8080/h2-console`
2. Configurez les paramètres de connexion comme suit :
   * **JDBC URL** : `jdbc:h2:file:./data/gdpt`
   * **User Name** : `sa`
   * **Password** : *(laisser vide)*
3. Cliquez sur **Connect**.

---

## 🧪 Lancement des Tests

Pour exécuter la suite de tests d'intégration (qui utilise une base de données H2 isolée en mémoire) :
```bash
./mvnw test
```

La suite de tests valide notamment :
- ✅ **Chargement du contexte Spring** (intégration complète des beans)
- ✅ **Réservation de tickets** : décompte correct des places disponibles après achat
- ✅ **Refus de surréservation** : levée d'exception correcte si la capacité est dépassée

---

## 📂 Structure du Projet

```
GDPT/
├── src/
│   ├── main/
│   │   ├── java/com/gdpt/gdpt/
│   │   │   ├── GdptApplication.java          # Point d'entrée Spring Boot
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java       # Configuration Spring Security 6
│   │   │   ├── controller/
│   │   │   │   ├── PublicController.java      # Routes publiques (accueil, détails, auth)
│   │   │   │   ├── VisitorController.java     # Routes visiteur (réservations, avis)
│   │   │   │   └── AdminController.java       # Routes admin (CRUD, dashboard)
│   │   │   ├── model/
│   │   │   │   ├── Utilisateur.java           # Entité utilisateur + rôles
│   │   │   │   ├── SiteTouristique.java       # Entité site touristique
│   │   │   │   ├── Evenement.java             # Entité événement/activité
│   │   │   │   ├── Reservation.java           # Entité réservation de ticket
│   │   │   │   └── Evaluation.java            # Entité avis/commentaire
│   │   │   ├── repository/
│   │   │   │   ├── UtilisateurRepository.java
│   │   │   │   ├── SiteTouristiqueRepository.java
│   │   │   │   ├── EvenementRepository.java
│   │   │   │   ├── ReservationRepository.java
│   │   │   │   └── EvaluationRepository.java
│   │   │   └── service/
│   │   │       ├── CustomUserDetailsService.java  # Chargement sécurité Spring
│   │   │       ├── UtilisateurService.java         # Inscription, recherche
│   │   │       ├── SiteTouristiqueService.java     # CRUD sites, notes moyennes
│   │   │       ├── EvenementService.java           # CRUD événements, capacités
│   │   │       ├── ReservationService.java         # Achat / annulation tickets
│   │   │       └── DataInitializer.java            # Initialisation des données de démo
│   │   └── resources/
│   │       ├── application.properties        # Configuration principale (H2 fichier)
│   │       ├── static/css/
│   │       │   └── style.css                 # Feuille de style CSS personnalisée
│   │       └── templates/
│   │           ├── fragments/
│   │           │   └── layout.html           # Fragments réutilisables (navbar, footer)
│   │           ├── index.html                # Page d'accueil publique
│   │           ├── site-details.html         # Fiche détaillée d'un site
│   │           ├── login.html                # Page de connexion
│   │           ├── register.html             # Page d'inscription
│   │           ├── visiteur/
│   │           │   └── mes-reservations.html # Espace personnel du visiteur
│   │           └── admin/
│   │               ├── dashboard.html        # Tableau de bord administrateur
│   │               ├── site-form.html        # Formulaire ajout/édition de site
│   │               ├── event-form.html       # Formulaire ajout/édition d'événement
│   │               └── reservations.html     # Suivi global des réservations
│   └── test/
│       ├── java/com/gdpt/gdpt/
│       │   ├── GdptApplicationTests.java
│       │   └── service/
│       │       └── ReservationServiceTest.java
│       └── resources/
│           └── application.properties        # Config tests (H2 en mémoire)
├── data/                                     # Répertoire de la base H2 (généré au 1er démarrage)
├── pom.xml                                   # Dépendances Maven
├── mvnw / mvnw.cmd                           # Maven Wrapper
└── README.md
```

---

## 🗺️ Table des Routes URL

### Routes Publiques (accessibles sans connexion)
| Méthode | URL | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Page d'accueil avec recherche et filtres |
| `GET` | `/sites/details/{id}` | Fiche détaillée d'un site |
| `GET` | `/login` | Page de connexion |
| `GET/POST` | `/register` | Page d'inscription |
| `POST` | `/logout` | Déconnexion |

### Routes Visiteur (nécessite `ROLE_VISITEUR`)
| Méthode | URL | Description |
| :--- | :--- | :--- |
| `GET` | `/visiteur/reservations` | Historique des réservations personnelles |
| `POST` | `/visiteur/reservation/book` | Réserver des places pour un événement |
| `POST` | `/visiteur/reservation/cancel/{id}` | Annuler une réservation |
| `POST` | `/visiteur/evaluation/add` | Publier un avis sur un site |

### Routes Administrateur (nécessite `ROLE_ADMIN`)
| Méthode | URL | Description |
| :--- | :--- | :--- |
| `GET` | `/admin/dashboard` | Tableau de bord avec statistiques |
| `GET` | `/admin/reservations` | Suivi global de toutes les réservations |
| `GET` | `/admin/sites/add` | Formulaire d'ajout d'un site |
| `GET` | `/admin/sites/edit/{id}` | Formulaire de modification d'un site |
| `POST` | `/admin/sites/save` | Enregistrer un site (création ou modification) |
| `POST` | `/admin/sites/delete/{id}` | Supprimer un site |
| `GET` | `/admin/events/add` | Formulaire de création d'un événement |
| `GET` | `/admin/events/edit/{id}` | Formulaire de modification d'un événement |
| `POST` | `/admin/events/save` | Enregistrer un événement |
| `POST` | `/admin/events/delete/{id}` | Supprimer un événement |

---

## 🗄️ Modèle de Données

```
Utilisateur (1) ─────────── (N) Reservation
    |                               |
    |                               N
    |                          Evenement (N) ─── (1) SiteTouristique
    |                                                      |
    └─────── (N) Evaluation (N) ──────────────────────────┘
```

| Entité | Attributs principaux |
| :--- | :--- |
| **Utilisateur** | `id`, `username` (unique), `email` (unique), `password` (BCrypt), `role` |
| **SiteTouristique** | `id`, `nom`, `description`, `localisation`, `categorie`, `imageUri`, `tarifEntree`, `horairesOuverture` |
| **Evenement** | `id`, `nom`, `description`, `date`, `prixTicket`, `placesMax`, `placesDisponibles`, `site` |
| **Reservation** | `id`, `utilisateur`, `evenement`, `nombrePlaces`, `dateReservation`, `statut` |
| **Evaluation** | `id`, `utilisateur`, `site`, `note` (1-5), `commentaire`, `datePublication` |

### Catégories de sites
`HISTORIQUE` · `NATUREL` · `CULTUREL` · `RELIGIEUX` · `GASTRONOMIQUE`

### Statuts de réservation
`CONFIRMEE` · `ANNULEE`

---

## 📝 Données de Démonstration Préchargées

Au premier démarrage, l'application insère automatiquement les données suivantes :

### Sites Touristiques
| # | Nom | Catégorie | Localisation | Tarif |
| :- | :--- | :--- | :--- | :--- |
| 1 | Tour Eiffel | HISTORIQUE | Paris, France | 26,80 € |
| 2 | Gorges du Verdon | NATUREL | Provence-Alpes-Côte d'Azur | Gratuit |
| 3 | Musée du Louvre | CULTUREL | Paris, France | 17,00 € |
| 4 | Mont Saint-Michel | HISTORIQUE | Normandie, France | Gratuit |
| 5 | Château de Versailles | HISTORIQUE | Versailles, France | 21,50 € |

### Événements Programmés
| # | Événement | Site | Date | Prix |
| :- | :--- | :--- | :--- | :--- |
| 1 | Spectacle Nocturne Grandes Eaux | Château de Versailles | J+10 à 21h | 30,00 € |
| 2 | Randonnée Kayak dans le Canyon | Gorges du Verdon | J+5 à 10h | 45,00 € |
| 3 | Visite Nocturne : Chefs-d'œuvre du Louvre | Musée du Louvre | J+3 à 19h30 | 35,00 € |

---

## 👤 Auteur

Projet réalisé dans le cadre de l'Exercice d'Application du **Semestre 6** — Architecture Logicielle.

> Application monolithique avec **Spring Boot 3**, **Thymeleaf**, **Spring Security 6** et **Spring Data JPA**.
# GDPT
