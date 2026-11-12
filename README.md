# Helpdesk – Ärendehanteringssystem

Ett ärendehanteringssystem utvecklat som laboration i Spring Boot.

Applikationen låter användare skapa och följa supportärenden. Supportpersonal kan hantera inkomna ärenden och administratörer kan hantera användarkonton.

## Tekniker

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- MySQL
- Maven
- JUnit 5 och Mockito

## Funktioner

Systemet har tre olika användarroller.

**USER**
- Logga in och skapa supportärenden
- Se och följa sina egna ärenden
- Visa ärendedetaljer
- Skriva kommentarer på egna ärenden

**AGENT**
- Se alla supportärenden
- Tilldela ärenden till sig själv
- Ändra ärendestatus
- Skriva kommentarer på ärenden

**ADMIN**
- Alla funktioner som AGENT har
- Skapa nya användare
- Ändra användarroller
- Aktivera och inaktivera användarkonton

Ärenden kan ha följande status:

`NEW`, `IN_PROGRESS`, `WAITING_FOR_USER`, `RESOLVED`, `CLOSED`

## Databas

Applikationen använder MySQL tillsammans med Spring Data JPA.

De tre huvudsakliga entiteterna är:

- **User** – användaruppgifter, roller och kontostatus
- **Ticket** – supportärenden och deras status
- **Comment** – kommentarer kopplade till ärenden

## Starta projektet

Projektet kräver Java 21 och en MySQL-databas.

1. Starta MySQL och skapa en databas som heter `helpdesk`.
2. Ange miljövariabeln `DB_PASSWORD` med lösenordet till MySQL.
3. Kontrollera databasinställningarna i `application.properties`.
4. Starta applikationen via IntelliJ eller Maven.

Med Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Applikationen nås sedan på:

http://localhost:8080

För utvecklingsmiljön används även miljövariablerna `DEV_USER_PASSWORD`, `DEV_AGENT_PASSWORD` och `DEV_ADMIN_PASSWORD` för testkontona.

## Tester

Projektet innehåller enhetstester och integrationstester för bland annat användarhantering, ärenden, kommentarer och behörighetskontroller.

Tester körs med:

```bash
./mvnw test
```

Integrationstesterna använder en separat MySQL-databas, `helpdesk_test`.

## Projektstruktur

- `controller` – hanterar HTTP-förfrågningar och webbsidor
- `service` – innehåller applikationens logik
- `repository` – kommunicerar med databasen
- `entity` – innehåller databasentiteterna
- `security` – hanterar användarinloggning
- `config` – konfiguration för applikationen

## Om laborationen

Projektet är utvecklat för att öva på Spring Boot, databashantering med JPA, autentisering och auktorisering med Spring Security samt serverrenderade webbsidor med Thymeleaf.