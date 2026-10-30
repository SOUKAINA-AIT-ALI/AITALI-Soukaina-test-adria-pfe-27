# Plateforme Bancaire Distribuée & Agentique — Test Technique ENSET / ADRIA

Projet réalisé dans le cadre de l'épreuve pratique de sélection (Architecture micro-services, IA et développement full-stack) — **Concours PFE 2027**.

**Candidat(e) :** AIT ALI SOUKAINA  
**Établissement :** École Normale Supérieure de l'Enseignement Technique (ENSET) Mohammedia  
**Entreprise partenaire :** ADRIA Business & Technology  

---

## 📌 Sommaire
- [Architecture Cible & Composants](#-architecture-cible--composants)
- [Organisation en Couches](#-organisation-en-couches-imposée)
- [Microservices Implémentés](#-microservices-implémentés)
- [Règles Métier Validées](#-règles-métier-validées)
- [Pré-requis & Démarrage Rapide](#-pré-requis--démarrage-rapide)
- [Catalogue des Endpoints & Tests](#-catalogue-des-endpoints--tests)
- [Suivi du Sujet (Roadmap)](#-suivi-du-sujet-roadmap)

---

## 🏛 Architecture Cible & Composants

L'écosystème repose sur une architecture microservices Spring Cloud avec découverte dynamique, configuration centralisée, tolérance aux pannes et connectivité agentique :

```
                        ┌────────────────────────────────┐
                        │      Client Web / Chatbot      │
                        └───────────────┬────────────────┘
                                        │ HTTP / GraphQL
                                        ▼
                        ┌────────────────────────────────┐
                        │  Spring Cloud Gateway (:8080)  │
                        └───────┬────────────────┬───────┘
                                │                │
        ┌───────────────────────┘                └───────────────────────┐
        ▼                                                                ▼
┌───────────────────────────────┐                              ┌───────────────────────────────┐
│     account-service (:8081)   │◄────── Feign + Resilience4J ─│  transaction-service (:8082) │
│  • domain / repository        │                              │  • domain / repository        │
│  • service métier             │                              │  • service métier             │
│  • REST / GraphQL / MCP Tools │                              │  • REST / GraphQL / MCP Tools │
└───────────────────────────────┘                              └───────────────┬───────────────┘
        ▲                                                                      │ Event Kafka
        │                                                                      ▼
┌───────┴───────────────────────┐                              ┌───────────────────────────────┐
│    discovery-service (:8761)  │                              │      Kafka Broker & Logs      │
│         Eureka Server         │                              │  bank.transactions.created   │
└───────────────────────────────┘                              └───────────────────────────────┘
        ▲
┌───────┴───────────────────────┐
│     config-service (:8888)    │ ─── bank.transaction.max-amount=1000
│   Spring Cloud Config Server  │
└───────────────────────────────┘
```

---

## 🧱 Organisation en Couches Imposée

Chaque microservice métier respecte une stricte séparation en 6 couches, où les connecteurs d'entrée ne contiennent aucune logique métier et appellent la même couche service :

```
├── domain/       # Entités JPA, records DTOs et énumérations
├── repository/   # Interfaces Spring Data JPA
├── service/      # Logique métier, intégrité bancaire et validations
├── web/
│   ├── rest/     # Contrôleurs REST et gestionnaires d'exceptions
│   └── graphql/  # Contrôleurs GraphQL et schémas .graphqls
└── mcp/          # Serveur d'outils Model Context Protocol (MCP) pour LLM
```

---

## 🚀 Microservices Implémentés

### 1. `discovery-service` (Port 8761)
* **Rôle :** Serveur d'annuaire et d'enregistrement Netflix Eureka.
* **Configuration :** Mode d'auto-préservation désactivé en local (`enable-self-preservation: false`) pour un fonctionnement optimal en développement.

### 2. `config-service` (Port 8888)
* **Rôle :** Serveur de configuration centralisé Spring Cloud Config.
* **Propriété partagée :** Diffuse le paramètre officiel `bank.transaction.max-amount=1000` via son référentiel `config-repo/`.

### 3. `gateway-service` (Port 8080)
* **Rôle :** Passerelle d'accès unique basée sur Spring Cloud Gateway.
* **Routage :** Découverte dynamique Eureka (`lb://account-service`, `lb://transaction-service`) et exposition des routes via Actuator (`/actuator/gateway/routes`).

### 4. `account-service` (Port 8081)
* **Rôle :** Gestion des comptes bancaires et des soldes.
* **Modèle :** `id` (UUID), `customerReference` (String), `status` (ACTIVE | BLOCKED), `currency` (String), `balance` (double).
* **Connecteurs :** REST (`/api/accounts`), GraphQL (`/graphql`, console `/graphiql`), MCP (`/mcp/tools`).

### 5. `transaction-service` (Port 8082)
* **Rôle :** Enregistrement et consultation des transactions et virements.
* **Modèle :** `id` (UUID), `accountId` (UUID), `amount` (BigDecimal), `currency` (String), `merchantCategory` (String), `country` (String), `createdAt` (Instant).
* **Intégration :** Client OpenFeign vers `account-service` + disjoncteur **Resilience4J** (`@CircuitBreaker`) avec méthode de repli (*fallback*).

---

## ⚖️ Règles Métier Validées

Toutes les règles du sujet sont vérifiées et couvertes par des tests automatisés :
1. **Plafond de montant :** Le montant doit être strictement positif et ≤ au plafond configuré (`1000`).
2. **Statut du compte :** Le compte doit exister et posséder le statut `ACTIVE`.
3. **Concordance de devise :** La devise de la transaction doit être identique à celle du compte.
4. **Mise à jour du solde :** Consultation préalable du solde, contrôle de provision suffisante et mise à jour immédiate du compte.
5. **Virement inter-comptes :** Débit atomique du compte émetteur, crédit du compte bénéficiaire et enregistrement des transactions respectives.

---

## 🛠 Pré-requis & Démarrage Rapide

* **Java :** Version 21 (LTS)
* **Maven :** Wrapper inclus (`./mvnw` / `.\mvnw.cmd`)

### Compilation et validation globale
```powershell
# Compilation de tous les modules
.\mvnw.cmd clean compile

# Exécution des tests de tous les microservices
.\mvnw.cmd test
```

### Ordre de démarrage recommandé
Pour exécuter la plateforme localement, lancer dans des terminaux séparés :
```powershell
1. .\mvnw.cmd spring-boot:run -pl discovery-service
2. .\mvnw.cmd spring-boot:run -pl config-service
3. .\mvnw.cmd spring-boot:run -pl gateway-service
4. .\mvnw.cmd spring-boot:run -pl account-service
5. .\mvnw.cmd spring-boot:run -pl transaction-service
```

---

## 🔍 Catalogue des Endpoints & Tests

| Microservice | Composant | Endpoint / URL | Description |
|---|---|---|---|
| **discovery-service** | Dashboard Web | `http://localhost:8761` | Console d'administration Eureka |
| **config-service** | Config Server | `http://localhost:8888/transaction-service/default` | Propriété partagée `bank.transaction.max-amount=1000` |
| **gateway-service** | Actuator Routes | `http://localhost:8080/actuator/gateway/routes` | Liste JSON des routes actives |
| **account-service** | REST API | `http://localhost:8081/api/accounts` | Liste des comptes bancaires |
| **account-service** | Console H2 | `http://localhost:8081/h2-console` | Base mémoire (`jdbc:h2:mem:accountdb`, user: `sa`) |
| **account-service** | GraphiQL | `http://localhost:8081/graphiql` | Interface interactive de requêtes GraphQL |
| **account-service** | MCP Tools | `http://localhost:8081/mcp/tools` | Catalogue des outils pour l'assistant IA |
| **transaction-service**| REST API | `http://localhost:8082/api/transactions/account/{id}` | Historique des transactions d'un compte |
| **transaction-service**| REST Virement | `http://localhost:8082/api/transactions/transfer` | Exécution d'un virement entre comptes |
| **transaction-service**| GraphiQL | `http://localhost:8082/graphiql` | Requêtes et mutations GraphQL de transactions |
| **transaction-service**| MCP Tools | `http://localhost:8082/mcp/tools` | Outils MCP de transaction et de virement |

---

## 📋 Suivi du Sujet (Roadmap)

- [x] **Partie A — Préparation et initialisation** (Repo GitHub, Conventional Commits, POM multi-modules)
- [x] **Partie B — Conception et Architecture** (Diagramme de classes & Schéma d'architecture globale)
- [x] **Partie C.1 — Account-Service** (Domain, Repository, Service, H2, REST, GraphQL, MCP)
- [x] **Partie C.2 & C.3 — Transaction-Service** (Logique métier, OpenFeign, Resilience4J, REST, GraphQL, MCP)
- [x] **Partie D.1 — Spring Cloud Gateway** (Routage dynamique, Actuator)
- [x] **Partie D.2 — Eureka Discovery Service** (Annuaire de services)
- [x] **Partie D.3 — Config Service** (Paramètre `bank.transaction.max-amount=1000`)
- [ ] **Partie E — Chatbot Agentique** (Spring AI, LLM et serveurs MCP) ⏳ *À réaliser*
- [ ] **Partie F — Communication Asynchrone Kafka** (Topic `bank.transactions.created` & Consumer logs) ⏳ *À réaliser*
- [ ] **Partie G — Client Frontend** (Interface Web Angular) ⏳ *À réaliser*
- [ ] **Partie H — Machine Learning Python** (Détection de fraude bancaire & analyse) ⏳ *À réaliser*
- [ ] **Partie I — DevOps** (Dockerfiles, Docker Compose, CI/CD GitHub Actions) ⏳ *À réaliser*

---

*Projet conforme aux exigences académiques ENSET et professionnelles ADRIA Business & Technology.*

