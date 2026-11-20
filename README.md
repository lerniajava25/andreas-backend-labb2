# Helpdesk – Ärendehanteringssystem

Ett ärendehanteringssystem byggt med Java och Spring Boot som en del av utbildningen till system- och webbutvecklare.

## Funktioner

Systemet har tre användarroller:

- **USER** – Skapa och hantera egna ärenden samt kommentera.
- **AGENT** – Hantera, tilldela och ändra status på ärenden.
- **ADMIN** – Hantera ärenden och användare.

## Tekniker

- Java 21 / Spring Boot
- Spring Security och Spring Data JPA
- Thymeleaf
- MySQL 8.4 / Docker
- Maven / JUnit

## Starta projektet

**Krav:** Java 21 och Docker Desktop.

1. Klona projektet från GitHub.
2. Öppna en terminal i projektmappen.
3. Starta databasen:

```bash
export DB_PASSWORD=root
docker compose up -d
```

4. Vänta tills databasen är `healthy`:

```bash
docker compose ps
```

5. Starta applikationen:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/helpdesk
./mvnw spring-boot:run
```

Öppna sedan `http://localhost:8080`.

## Köra tester

I samma terminal (eller en ny terminal med `DB_PASSWORD` angivet):

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/helpdesk_test
./mvnw clean test
```

Projektet innehåller **58 tester**.

Docker Compose skapar databaserna `helpdesk` och `helpdesk_test` automatiskt vid första uppstarten.