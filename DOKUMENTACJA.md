# Dokumentacja systemu wypożyczalni samochodów

## Spis treści

1. [Ogólny opis systemu](#ogólny-opis-systemu)
2. [Jak uruchomić aplikację](#jak-uruchomić-aplikację)
3. [Backend — Spring Boot](#backend--spring-boot)
   - [Struktura folderów](#struktura-folderów-backend)
   - [Baza danych](#baza-danych)
   - [Encje (tabele)](#encje-tabele)
   - [Endpointy REST API](#endpointy-rest-api)
   - [Logika biznesowa (serwisy)](#logika-biznesowa-serwisy)
   - [Bezpieczeństwo](#bezpieczeństwo)
4. [Frontend — React](#frontend--react)
   - [Struktura folderów](#struktura-folderów-frontend)
   - [Nawigacja i strony](#nawigacja-i-strony)
   - [Zarządzanie stanem](#zarządzanie-stanem)
   - [Komunikacja z API](#komunikacja-z-api)
5. [Przepływ danych — przykłady](#przepływ-danych--przykłady)
6. [Znane ograniczenia](#znane-ograniczenia)

---

## Ogólny opis systemu

Aplikacja webowa do zarządzania wypożyczalnią samochodów. Składa się z dwóch niezależnych części:

| Część         | Technologia               | Port   |
| ------------- | ------------------------- | ------ |
| Backend (API) | Java 17 + Spring Boot 3.2 | `8080` |
| Frontend (UI) | React 19 + Vite           | `5173` |

**Co umożliwia system:**

- Przeglądanie i zarządzanie flotą samochodów
- Tworzenie i śledzenie rezerwacji
- Wybór dodatkowych opcji (GPS, fotelik, dodatkowy kierowca)
- Podgląd aktualnych cen paliw (pobieranych z internetu)
- Zarządzanie użytkownikami (panel admina)

---

## Jak uruchomić aplikację

### Backend

```bash
# Z głównego folderu projektu
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew bootRun
```

Serwer uruchomi się na `http://localhost:8080`.

### Frontend

```bash
cd frontend-car-rental
npm install       # tylko przy pierwszym uruchomieniu
npm run dev
```

Aplikacja będzie dostępna pod `http://localhost:5173`.

> **Wymagania:** MySQL uruchomiony lokalnie, baza danych `car_rental` (Hibernate tworzy tabele automatycznie).
> Dane do bazy: `application.properties` → `spring.datasource.*`

---

## Backend — Spring Boot

### Struktura folderów (backend)

```
src/main/java/com/crud/rental/
├── config/         ← konfiguracja bezpieczeństwa i CORS
├── controller/     ← endpointy HTTP (REST API)
├── service/        ← logika biznesowa
├── repository/     ← dostęp do bazy danych (JPA)
├── domain/         ← encje (tabele) i DTO (obiekty transferu danych)
├── mapper/         ← konwersja encja ↔ DTO
├── exception/      ← własne wyjątki (np. CarNotFoundException)
└── seeder/         ← dane startowe (opcje, konto admina)
```

### Baza danych

Typ: **MySQL**. Hibernate automatycznie tworzy/aktualizuje tabele (`ddl-auto=update`).

Konfiguracja w: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/car_rental
```

Przy starcie aplikacji `DatabaseSeeder` dodaje:

- 3 domyślne opcje: GPS (5 zł/dzień), Fotelik dziecięcy (7.50 zł/dzień), Dodatkowy kierowca (10 zł/dzień)
- Konto admina: `login: admin`, `hasło: admin`

### Encje (tabele)

#### `USERS` — Użytkownicy

| Pole                | Typ     | Opis               |
| ------------------- | ------- | ------------------ |
| id                  | Long    | Klucz główny       |
| username            | String  | Login (unikalny)   |
| password            | String  | Hasło (plain text) |
| firstName, lastName | String  | Imię i nazwisko    |
| isAdmin             | boolean | Czy administrator  |

#### `CARS` — Samochody

| Pole         | Typ        | Opis                              |
| ------------ | ---------- | --------------------------------- |
| id           | Long       | Klucz główny                      |
| carBrand     | String     | Marka                             |
| colour       | String     | Kolor                             |
| price        | BigDecimal | Cena za dobę (PLN)                |
| kilometers   | int        | Przebieg                          |
| availability | boolean    | Czy dostępny do wynajmu           |
| fuel         | String     | Typ paliwa (95, 98, ON, ON+, LPG) |
| fuelCapacity | double     | Pojemność baku (litry)            |

#### `RESERVATIONS` — Rezerwacje

| Pole               | Typ        | Opis                     |
| ------------------ | ---------- | ------------------------ |
| id                 | Long       | Klucz główny             |
| startDate, endDate | LocalDate  | Daty wynajmu             |
| totalPrice         | BigDecimal | Łączna cena              |
| status             | boolean    | Czy aktywna              |
| mileage            | int        | Przebieg podczas wynajmu |
| ended              | boolean    | Czy zakończona           |
| user_id            | FK         | Powiązany użytkownik     |
| car_id             | FK         | Powiązany samochód       |

#### `OPTIONS` — Opcje dodatkowe

| Pole  | Typ        | Opis         |
| ----- | ---------- | ------------ |
| id    | Long       | Klucz główny |
| name  | String     | Nazwa opcji  |
| price | BigDecimal | Cena za dobę |

#### `DAMAGE` — Szkody

| Pole           | Typ        | Opis                 |
| -------------- | ---------- | -------------------- |
| id             | Long       | Klucz główny         |
| description    | String     | Opis szkody          |
| repairCost     | BigDecimal | Koszt naprawy        |
| reservation_id | FK         | Powiązana rezerwacja |

#### `FUEL_PRICE_CACHE` — Cache cen paliw

Przechowuje pobrane ze strony zewnętrznej ceny paliw (z datą odczytu). Używany gdy scraping się nie powiedzie.

#### Relacje między tabelami

```
USER ──< RESERVATION >── CAR
              │
              ├──< DAMAGE
              │
              └──>< OPTIONS (tabela pośrednia RESERVATION_OPTION)
```

---

### Endpointy REST API

Wszystkie endpointy dostępne pod `http://localhost:8080`.

#### Samochody `/cars`

| Metoda | Ścieżka           | Opis                        |
| ------ | ----------------- | --------------------------- |
| GET    | `/cars`           | Lista wszystkich samochodów |
| GET    | `/cars/{id}`      | Jeden samochód po ID        |
| GET    | `/cars/available` | Tylko dostępne samochody    |
| POST   | `/cars`           | Dodaj nowy samochód         |
| PUT    | `/cars`           | Zaktualizuj samochód        |
| DELETE | `/cars/{id}`      | Usuń samochód               |

#### Rezerwacje `/reservations`

| Metoda | Ścieżka                             | Opis                            |
| ------ | ----------------------------------- | ------------------------------- |
| GET    | `/reservations`                     | Lista wszystkich rezerwacji     |
| GET    | `/reservations/{id}`                | Jedna rezerwacja po ID          |
| POST   | `/reservations`                     | Utwórz rezerwację               |
| PUT    | `/reservations/{id}`                | Zaktualizuj rezerwację          |
| DELETE | `/reservations/delete/{id}`         | Usuń rezerwację                 |
| POST   | `/reservations/add/{rezId}/{carId}` | Przypisz samochód do rezerwacji |

#### Użytkownicy `/users`

| Metoda | Ścieżka           | Opis                           |
| ------ | ----------------- | ------------------------------ |
| GET    | `/users`          | Lista wszystkich użytkowników  |
| POST   | `/users/register` | Rejestracja nowego użytkownika |
| POST   | `/users/login`    | Logowanie                      |
| DELETE | `/users/{id}`     | Usuń użytkownika               |

#### Opcje `/options`

| Metoda | Ścieżka         | Opis                    |
| ------ | --------------- | ----------------------- |
| GET    | `/options`      | Lista opcji dodatkowych |
| POST   | `/options`      | Dodaj opcję             |
| DELETE | `/options/{id}` | Usuń opcję              |

#### Ceny paliw `/fuels`

| Metoda | Ścieżka         | Opis                           |
| ------ | --------------- | ------------------------------ |
| GET    | `/fuels/prices` | Aktualne ceny paliw (scraping) |
| GET    | `/fuels/types`  | Dostępne typy paliw            |

---

### Logika biznesowa (serwisy)

#### `ReservationService` — najważniejszy serwis

Przy tworzeniu rezerwacji:

1. Pobiera użytkownika i samochód po ID
2. Dopasowuje nazwy opcji do encji w bazie
3. Oznacza samochód jako **niedostępny** (`availability = false`)
4. Oblicza cenę łączną: `(liczba dni × cena auta) + (liczba dni × suma opcji) + koszty szkód`
5. Zapisuje rezerwację

Przy usuwaniu rezerwacji:

- Samochód wraca do statusu **dostępny**

#### `FuelUsageService` — ceny paliw

1. Próbuje pobrać ceny paliw przez scraping strony `autocentrum.pl` (Jsoup)
2. Parsuje tabelę HTML z regionami i cenami
3. Zapisuje wyniki w bazie (`FUEL_PRICE_CACHE`)
4. Jeśli scraping się nie uda → zwraca ostatnio zapisane dane z cache

#### `UserService` — autoryzacja

- Login sprawdza `username` i `password` w bazie
- Zalogowany użytkownik trzymany jest **w pamięci serwera** (ginie po restarcie)
- Brak JWT ani sesji HTTP

---

### Bezpieczeństwo

> **Uwaga:** Aplikacja jest w fazie rozwoju — brak produkcyjnego zabezpieczenia.

- Wszystkie endpointy są **publiczne** (brak wymaganego tokenu)
- Hasła przechowywane jako **plain text** (niezalecane)
- CORS ustawiony na `*` (wszystkie domeny)
- Rola admina sprawdzana **tylko po stronie frontendu** — backend nie weryfikuje uprawnień

---

## Frontend — React

### Struktura folderów (frontend)

```
frontend-car-rental/src/
├── api/
│   └── client.js          ← wszystkie wywołania HTTP do backendu
├── context/
│   └── AuthContext.jsx    ← globalny stan zalogowanego użytkownika
├── components/
│   ├── auth/              ← ochrona tras (ProtectedRoute)
│   ├── layout/            ← Navbar, Layout
│   ├── cars/              ← karty i formularze samochodów
│   ├── reservations/      ← tabela rezerwacji, wiersze, checkboxy opcji
│   └── shared/            ← spinner, dialog potwierdzenia, badge statusu
└── pages/                 ← pełne strony aplikacji
```

### Nawigacja i strony

Routing obsługiwany przez **React Router**. Strony chronione wymagają zalogowania, niektóre wymagają roli admina.

| Ścieżka             | Strona                    | Dostęp     |
| ------------------- | ------------------------- | ---------- |
| `/login`            | Logowanie                 | Publiczny  |
| `/register`         | Rejestracja               | Publiczny  |
| `/`                 | Lista samochodów          | Zalogowany |
| `/reservations`     | Moje rezerwacje           | Zalogowany |
| `/reservations/new` | Nowa rezerwacja           | Zalogowany |
| `/reservations/:id` | Szczegóły rezerwacji      | Zalogowany |
| `/fuel-prices`      | Ceny paliw                | Zalogowany |
| `/options`          | Zarządzanie opcjami       | **Admin**  |
| `/users`            | Zarządzanie użytkownikami | **Admin**  |

#### Opis poszczególnych stron

**Strona główna — Lista samochodów (`/`)**

- Wyświetla samochody jako karty (marka, kolor, cena/dzień, dostępność)
- Filtr "tylko dostępne"
- Admin: może dodawać, edytować i usuwać samochody

**Nowa rezerwacja (`/reservations/new`)**

- Wybór samochodu, dat, opcji dodatkowych
- Kalkulacja ceny na żywo po stronie frontendu:
  - liczba dni × cena samochodu + liczba dni × suma opcji
- Podsumowanie kosztów widoczne na bieżąco

**Lista rezerwacji (`/reservations`)**

- Zwykły użytkownik widzi tylko swoje rezerwacje
- Admin widzi wszystkie rezerwacje
- Możliwość usunięcia rezerwacji (z oknem potwierdzenia)

**Szczegóły rezerwacji (`/reservations/:id`)**

- Dane samochodu, daty, cena, wybrane opcje

**Ceny paliw (`/fuel-prices`)**

- Pobiera ceny z backendu (który scrapuje internet)
- Filtr według typu paliwa
- Tabela: Region, Typ paliwa, Cena (zł/L)

**Opcje (`/options`) — tylko admin**

- Tabela opcji z cenami
- Formularz dodania nowej opcji
- Usuwanie opcji

**Użytkownicy (`/users`) — tylko admin**

- Lista użytkowników z rolami
- Usuwanie użytkowników (nie można usunąć siebie ani innych adminów)

---

### Zarządzanie stanem

**`AuthContext.jsx`** — globalny kontekst React trzymający dane zalogowanego użytkownika.

```
localStorage (car_rental_user)
       ↓ odczyt przy starcie
  AuthContext
  { user, login(), logout(), register() }
       ↓ dostęp przez useContext()
  Każdy komponent
```

- Po zalogowaniu dane użytkownika zapisywane są w `localStorage` → sesja przeżywa odświeżenie strony
- `logout()` czyści localStorage i przekierowuje na `/login`

**`ProtectedRoute.jsx`** — komponent owijający chronione strony:

- Jeśli brak zalogowanego użytkownika → przekierowanie na `/login`
- Jeśli `adminOnly=true` i użytkownik nie jest adminem → przekierowanie na `/`

---

### Komunikacja z API

Cała komunikacja z backendem przez **`api/client.js`**:

```js
// Przykład wywołania
const cars = await apiClient.getCars();
const reservation = await apiClient.createReservation(data);
```

- Base URL: `http://localhost:8080`
- Format danych: JSON
- Błędy HTTP (4xx, 5xx) rzucają wyjątek → przechwytywane przez try/catch w komponentach

---

## Przepływ danych — przykłady

### Tworzenie rezerwacji krok po kroku

```
1. Użytkownik wchodzi na /reservations/new
        ↓
2. Frontend pobiera: GET /cars/available  +  GET /options
        ↓
3. Użytkownik wybiera: auto, daty, opcje
   Frontend liczy cenę na żywo (bez zapytania do API)
        ↓
4. Klik "Zarezerwuj" → POST /reservations
   Body: { startDate, endDate, totalPrice, userId, carId, optionNames[] }
        ↓
5. Backend (ReservationService):
   - Pobiera User z bazy po userId
   - Pobiera Car z bazy po carId
   - Zamienia nazwy opcji na encje Option
   - Ustawia car.availability = false
   - Zapisuje rezerwację w bazie
        ↓
6. Frontend przekierowuje na /reservations
   Użytkownik widzi nową rezerwację na liście
```

### Logowanie użytkownika

```
1. Formularz login → POST /users/login
   Body: { username, password }
        ↓
2. Backend sprawdza username + password w bazie
   Zwraca obiekt UserDto (id, firstName, isAdmin, ...)
        ↓
3. Frontend zapisuje użytkownika w localStorage
   AuthContext aktualizuje globalny stan
        ↓
4. React Router przekierowuje na /
```

### Ceny paliw

```
1. Użytkownik wchodzi na /fuel-prices
        ↓
2. Frontend: GET /fuels/prices
        ↓
3. Backend (FuelUsageService):
   → Próba scrapingu autocentrum.pl (Jsoup)
   → Sukces: parsuje HTML, zapisuje w FUEL_PRICE_CACHE, zwraca dane
   → Błąd: zwraca ostatnie dane z FUEL_PRICE_CACHE
        ↓
4. Frontend wyświetla tabelę, filtr po typie paliwa
```

---

## Znane ograniczenia

| Problem               | Opis                                                                                           |
| --------------------- | ---------------------------------------------------------------------------------------------- |
| Hasła plain text      | Hasła nie są hashowane (brak bcrypt)                                                           |
| Brak JWT              | Endpointy API są publiczne — każdy może je wywołać                                             |
| Role tylko na froncie | Admin-check wyłącznie w React, backend nie sprawdza                                            |
| Logowanie in-memory   | Zalogowany użytkownik na backendzie ginie po restarcie serwera                                 |
| Dane DB w kodzie      | Login/hasło do bazy w `application.properties` (powinny być w zmiennych env)                   |
| Scraping fragile      | Ceny paliw pobierane przez scraping HTML — zmiana struktury strony zewnętrznej zepsuje funkcję |
