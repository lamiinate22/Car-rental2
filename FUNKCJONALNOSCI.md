# Car Rental – Dokumentacja funkcjonalności

Aplikacja webowa do wypożyczania samochodów. Backend: **Spring Boot 3.2.4** (Java 17, Gradle). Frontend: **React 18** (Vite). Baza danych: **MySQL**.

---

## Wymagania projektowe – spełnienie

| # | Wymaganie | Status |
|---|-----------|--------|
| 1 | Komunikacja HTTP (GET, POST, PUT, DELETE) | Zrealizowane |
| 2 | Obsługa błędów (400, 401, 403, 404, 500…) | Zrealizowane |
| 3 | Dwie integracje z zewnętrznymi API | Zrealizowane (NBP + Stripe) |
| 4 | JWT Autentykacja i Autoryzacja | Zrealizowane |
| 5 | Testy jednostkowe | Zrealizowane |
| 6 | Dokumentacja OpenAPI (Swagger) | Zrealizowane |
| 7 | Prosty frontend | Zrealizowane |

---

## 1. REST API – Endpointy

### Autentykacja (`/api/auth`)

| Metoda | Ścieżka | Opis | Dostęp |
|--------|---------|------|--------|
| POST | `/api/auth/login` | Logowanie, zwraca JWT token | Publiczny |

### Użytkownicy (`/users`)

| Metoda | Ścieżka | Opis | Dostęp |
|--------|---------|------|--------|
| GET | `/users` | Lista wszystkich użytkowników | Zalogowany |
| POST | `/users/register` | Rejestracja nowego konta | Publiczny |
| PUT | `/users/{userId}/admin` | Nadaj / odbierz rolę admina | Zalogowany |
| DELETE | `/users/{userId}` | Usuń użytkownika | Zalogowany |

### Samochody (`/cars`)

| Metoda | Ścieżka | Opis | Dostęp |
|--------|---------|------|--------|
| GET | `/cars` | Lista wszystkich samochodów | Zalogowany |
| GET | `/cars/available` | Lista dostępnych samochodów | Zalogowany |
| GET | `/cars/{carId}` | Szczegóły samochodu | Zalogowany |
| POST | `/cars` | Dodaj nowy samochód | Admin |
| PUT | `/cars` | Zaktualizuj samochód | Admin |
| DELETE | `/cars/{carId}` | Usuń samochód | Admin |

### Rezerwacje (`/reservations`)

| Metoda | Ścieżka | Opis | Dostęp |
|--------|---------|------|--------|
| GET | `/reservations` | Lista rezerwacji | Zalogowany |
| GET | `/reservations/{reservationId}` | Szczegóły rezerwacji | Zalogowany |
| POST | `/reservations` | Utwórz rezerwację | Zalogowany |
| PUT | `/reservations/{reservationId}` | Zaktualizuj rezerwację | Zalogowany |
| DELETE | `/reservations/delete/{reservationId}` | Usuń rezerwację | Zalogowany |
| DELETE | `/reservations` | Usuń wszystkie rezerwacje | Zalogowany |

### Opcje dodatkowe (`/options`)

| Metoda | Ścieżka | Opis | Dostęp |
|--------|---------|------|--------|
| GET | `/options` | Lista opcji (GPS, fotel, dodatkowy kierowca) | Zalogowany |
| POST | `/options` | Dodaj opcję | Admin |
| DELETE | `/options/{id}` | Usuń opcję | Admin |

### Paliwa i kursy walut (`/fuels`)

| Metoda | Ścieżka | Opis | Dostęp |
|--------|---------|------|--------|
| GET | `/fuels/prices` | Ceny paliw (scraping autocentrum.pl z cache) | Zalogowany |
| GET | `/fuels/types` | Dostępne typy paliw (95, 98, ON, ON+, LPG) | Zalogowany |
| GET | `/fuels/nbp-rate/{code}` | Kurs waluty z NBP API (np. EUR, USD) | Zalogowany |

### Płatności – Stripe (`/payments`)

| Metoda | Ścieżka | Opis | Dostęp |
|--------|---------|------|--------|
| POST | `/payments/create-checkout-session` | Utwórz sesję płatności Stripe, zablokuj auto | Zalogowany |
| GET | `/payments/confirm?sessionId={id}` | Potwierdź płatność po powrocie ze Stripe | Zalogowany |
| POST | `/payments/webhook` | Webhook Stripe (checkout.session.completed) | Publiczny |

---

## 2. Obsługa błędów

Globalny handler `@RestControllerAdvice` (`GlobalExceptionHandler`) zwraca spójny JSON dla każdego błędu:

```json
{
  "status": 404,
  "error": "Car Not Found",
  "message": "Samochód nie został znaleziony",
  "timestamp": "2026-05-28T12:00:00"
}
```

| Wyjątek | Kod HTTP |
|---------|----------|
| `CarNotFoundException` | 404 Not Found |
| `UserNotFoundException` | 404 Not Found |
| `ReservationNotFoundException` | 404 Not Found |
| `OptionNotFoundException` | 404 Not Found |
| `DamageNotFoundException` | 404 Not Found |
| `IllegalStateException` | 409 Conflict |
| `IllegalArgumentException` | 400 Bad Request |
| `ResponseStatusException` | kod z wyjątku |
| `IOException` | 503 Service Unavailable |
| `RuntimeException` | 500 Internal Server Error |
| Brak JWT / nieprawidłowy token | 401 Unauthorized (Spring Security) |
| Brak uprawnień | 403 Forbidden (Spring Security) |

---

## 3. Integracje z zewnętrznymi API

### 3.1 NBP API – Kursy walut

- **URL:** `https://api.nbp.pl/api/exchangerates/rates/A/{code}/?format=json`
- **Użycie:** endpoint `/fuels/nbp-rate/{code}` (np. EUR, USD)
- **Zwraca:** nazwa waluty, kod, kurs mid (PLN), data obowiązywania
- **Wyświetlane na:** strona "Ceny paliw" w React (karty EUR/USD z przyciskiem odśwież)
- **Technicznie:** RestTemplate z Apache HttpClient5 (wyłączona weryfikacja SSL dla JDK 17 Homebrew)

### 3.2 Stripe – System płatności

- **Użycie:** tworzenie rezerwacji z płatnością online
- **Przepływ:**
  1. Frontend wysyła `POST /payments/create-checkout-session` (userId, carId, daty, opcje)
  2. Backend blokuje auto (availability = false), tworzy rezerwację PENDING na 30 minut, zwraca URL do Stripe Checkout
  3. Użytkownik płaci na stronie Stripe
  4. Stripe wysyła webhook → backend zmienia status na PAID
  5. Frontend przekierowuje na `/payment/success?session_id=...` → potwierdzenie
- **Scheduler:** co 5 minut zwalnia auta z wygasłymi (> 30 min) sesjami PENDING

---

## 4. Bezpieczeństwo – JWT

- Logowanie: `POST /api/auth/login` → zwraca token JWT (ważny 24h)
- Token przesyłany w nagłówku: `Authorization: Bearer <token>`
- Frontend automatycznie dołącza token do każdego żądania
- Przy wygaśnięciu tokenu (401): automatyczny logout i przekierowanie na `/login`
- Hasła przechowywane jako hash BCrypt

---

## 5. Testy jednostkowe

Testy w `src/test/java/com/crud/rental/`:

| Plik | Co testuje |
|------|-----------|
| `RentalApplicationTests.java` | Ładowanie kontekstu Spring |
| `domainTests/CarTestSuite.java` | Logika encji Car |
| `domainTests/UserTestSuite.java` | Logika encji User |
| `domainTests/ReservationTestSuite.java` | Logika encji Reservation |
| `domainTests/OptionTestSuite.java` | Logika encji Option |
| `domainTests/DamageTestSuite.java` | Logika encji Damage |
| `domainTests/FuelUsageTestSuite.java` | Logika encji FuelUsage |

Uruchomienie: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew test`

---

## 6. Dokumentacja OpenAPI (Swagger)

- **Zależność:** `springdoc-openapi-starter-webmvc-ui:2.5.0`
- **URL:** `http://localhost:8080/swagger-ui/index.html`
- Automatycznie dokumentuje wszystkie endpointy (kontrolery, parametry, odpowiedzi, DTOs)
- Odblokowane w Security (nie wymaga tokenu JWT)
- Umożliwia testowanie API bezpośrednio z przeglądarki (jak Postman)

---

## 7. Frontend React

### Strony

| Ścieżka | Strona | Dostęp |
|---------|--------|--------|
| `/login` | Logowanie | Publiczny |
| `/register` | Rejestracja | Publiczny |
| `/` | Lista samochodów | Zalogowany |
| `/reservations` | Lista rezerwacji | Zalogowany |
| `/reservations/new` | Nowa rezerwacja + płatność | Zalogowany |
| `/reservations/:id` | Szczegóły rezerwacji | Zalogowany |
| `/fuel-prices` | Ceny paliw + kursy NBP | Zalogowany |
| `/payment/success` | Potwierdzenie płatności | Zalogowany |
| `/options` | Zarządzanie opcjami | Admin |
| `/users` | Zarządzanie użytkownikami | Admin |

### Kluczowe funkcjonalności UI

**Samochody:**
- Przeglądanie wszystkich / tylko dostępnych samochodów
- Admin: dodawanie, edycja, usuwanie przez modal

**Rezerwacje:**
- Formularz z wyborem samochodu, dat, opcji dodatkowych
- Podgląd ceny w czasie rzeczywistym (dni × cena + opcje)
- Integracja z Stripe (przekierowanie na płatność)
- Potwierdzenie po powrocie ze Stripe
- Admin widzi wszystkie rezerwacje, user tylko swoje

**Ceny paliw i kursy walut:**
- Kursy EUR i USD pobierane live z NBP (z przyciskiem odśwież)
- Ceny paliw z podziałem na regiony i typy (filtry)

**Użytkownicy (Admin):**
- Lista użytkowników z możliwością nadania/odebrania roli admina
- Usuwanie użytkowników (zabezpieczenie: nie można modyfikować własnego konta)

---

## 8. Model danych (encje JPA)

| Encja | Kluczowe pola |
|-------|--------------|
| `User` | id, username, password (BCrypt), firstName, lastName, isAdmin |
| `Car` | id, carBrand, colour, price (zł/dzień), kilometers, availability, fuel, fuelCapacity |
| `Reservation` | id, startDate, endDate, totalPrice, paymentStatus (PENDING/PAID/EXPIRED), stripeSessionId, stripeSessionExpiresAt |
| `Option` | id, name, price (zł/dzień) — relacja M:N z Reservation |
| `Damage` | id, description, repairCost — relacja N:1 z Reservation |
| `FuelUsage` | id, startKm, endKm, fuelPrice, totalCost, fuelConsumption, fuelType |
| `FuelPriceCacheEntry` | region, fuelType, price, timestamp — cache dla scrapingu |

---

## 9. Uruchomienie

### Backend
```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew bootRun
```
> Wymagane JDK 17 (Lombok niekompatybilny z JDK 26+)

### Frontend
```bash
cd frontend-car-rental
npm install
npm run dev
```

### Wymagania
- Java 17 (`/opt/homebrew/opt/openjdk@17`)
- MySQL na `localhost:3306`, baza `car_rental`
- Node.js (dla frontendu)
