# Car Rental — Dokumentacja funkcjonalności

## Opis projektu

**Car Rental** to webowa aplikacja do zarządzania wypożyczalnią samochodów. System umożliwia klientom przeglądanie dostępnych pojazdów, składanie rezerwacji z opcjami dodatkowymi oraz opłacanie ich przez Stripe. Administratorzy zarządzają flotą, opcjami, rezerwacjami i użytkownikami z poziomu tego samego interfejsu.

**Stos technologiczny:**
- **Backend:** Java 17, Spring Boot 3, Spring Security (JWT, stateless), JPA/Hibernate, MySQL, Stripe SDK, Jsoup (scraping)
- **Frontend:** React 18, Vite, React Router v6
- **Płatności:** Stripe Checkout (redirect flow) + webhook
- **Dane zewnętrzne:** scraping cen paliw z autocentrum.pl z fallbackiem do cache w bazie

**Role użytkowników:**
- **Użytkownik** — przegląda auta, tworzy rezerwacje, widzi własne rezerwacje, sprawdza ceny paliw
- **Administrator** — ma pełny dostęp: zarządza flotą, opcjami, wszystkimi rezerwacjami i kontami użytkowników

---

## BACKEND (Spring Boot / Java)

---

### Autentykacja i autoryzacja

- `POST /api/auth/login` — logowanie z JWT; zwraca token + dane usera (id, username, firstName, lastName, admin)
- `POST /users/register` — rejestracja (publiczny endpoint)
- `POST /payments/webhook` — publiczny (Stripe webhook)
- Wszystkie pozostałe endpointy wymagają ważnego JWT w nagłówku `Authorization: Bearer`
- Dwa poziomy ról: `admin` (flaga `isAdmin`) i zwykły użytkownik
- Brak hashowania haseł — przechowywane w plaintext

---

### Zarządzanie użytkownikami (`/users`)

| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | `/users` | Pobierz wszystkich użytkowników |
| POST | `/users/register` | Zarejestruj użytkownika (walidacja username: 3–30 znaków, `[a-zA-Z0-9_-]`) |
| POST | `/users/login` | Stary login bez JWT (porównanie plain text) |
| DELETE | `/users/{userId}` | Usuń użytkownika |

Pola encji: `id`, `username`, `password`, `firstName`, `lastName`, `isAdmin`

---

### Zarządzanie samochodami (`/cars`)

| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | `/cars` | Pobierz wszystkie samochody |
| GET | `/cars/{carId}` | Pobierz samochód po ID |
| GET | `/cars/available` | Pobierz dostępne samochody (`availability = true`) |
| POST | `/cars` | Dodaj samochód |
| PUT | `/cars` | Zaktualizuj samochód |
| DELETE | `/cars/{carId}` | Usuń samochód |

Pola encji: `id`, `colour`, `carBrand`, `price` (BigDecimal, cena/dzień), `kilometers`, `availability`, `fuel`, `fuelCapacity`

---

### Zarządzanie rezerwacjami (`/reservations`)

| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | `/reservations` | Pobierz wszystkie rezerwacje |
| GET | `/reservations/{id}` | Pobierz rezerwację po ID |
| POST | `/reservations` | Dodaj rezerwację (ustawia `availability = false` dla samochodu) |
| PUT | `/reservations/{id}` | Zaktualizuj rezerwację |
| DELETE | `/reservations/delete/{id}` | Usuń rezerwację (przywraca `availability = true`) |
| DELETE | `/reservations` | Usuń WSZYSTKIE rezerwacje (przywraca dostępność każdego auta) |
| POST | `/reservations/add/{reservationId}/{carId}` | Dodaj auto do rezerwacji (logika częściowo zaimplementowana) |

Pola encji: `id`, `startDate`, `endDate`, `totalPrice`, `status`, `mileage`, `ended`, `paymentStatus`, `stripeSessionId`, `stripeSessionExpiresAt`, `user`, `car`, `damages`, `options`

**Logika cenowa (ReservationService):**
- `calculateBasePrice` → liczba dni × cena auta/dzień
- `calculateOptionCosts` → suma cen wybranych opcji
- `calculateDamageCosts` → suma kosztów napraw szkód
- `calculateTotalPrice` → base + opcje + szkody (zapisuje do bazy)
- `endReservation` → ustawia przebieg (mileage) i flagę `ended = true`

---

### Zarządzanie opcjami dodatkowymi (`/options`)

| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | `/options` | Pobierz wszystkie opcje |
| POST | `/options` | Dodaj opcję |
| DELETE | `/options/{id}` | Usuń opcję |

Pola encji: `id`, `name`, `price`

---

### Ceny paliw (`/fuels`)

| Metoda | Endpoint | Opis |
|--------|----------|------|
| GET | `/fuels/prices` | Pobierz ceny paliw (scraping autocentrum.pl + cache DB) |
| GET | `/fuels/types` | Zwraca listę typów paliw: ["95", "98", "ON", "ON+", "LPG"] |

Mechanizm: scraping przez Jsoup → wyniki zapisywane do cache w DB → fallback na cache gdy scraping zawiedzie.

---

### Płatności Stripe (`/payments`)

| Metoda | Endpoint | Opis |
|--------|----------|------|
| POST | `/payments/create-checkout-session` | Tworzy sesję Stripe Checkout (PLN, `mode=PAYMENT`) |
| GET | `/payments/confirm?sessionId=...` | Potwierdza płatność i tworzy rezerwację (idempotentne) |
| POST | `/payments/webhook` | Obsługuje webhook `checkout.session.completed` (tworzy rezerwację) |

Logika kalkulacji: `(cena auta × dni) + (suma opcji × dni)`  
Metadane Stripe: `userId`, `carId`, `startDate`, `endDate`, `optionIds`, `totalPrice`  
Po potwierdzeniu: auto oznaczane jako niedostępne, rezerwacja z `paymentStatus = "PAID"`

---

### Szkody (Damage — serwis bez dedykowanego controllera REST)

- Zapis szkody (`description`, `repairCost`) powiązanej z rezerwacją
- Pobierz wszystkie szkody
- Usuń szkodę po ID
- Dodaj szkodę do rezerwacji

---

## FRONTEND (React + Vite)

---

### Routing i ochrona tras

| Ścieżka | Dostęp | Strona |
|---------|--------|--------|
| `/login` | Publiczny | Logowanie |
| `/register` | Publiczny | Rejestracja |
| `/` | Chroniony | Lista samochodów |
| `/reservations` | Chroniony | Lista rezerwacji |
| `/reservations/new` | Chroniony | Nowa rezerwacja |
| `/reservations/:id` | Chroniony | Szczegóły rezerwacji |
| `/fuel-prices` | Chroniony | Ceny paliw |
| `/payment/success` | Chroniony | Potwierdzenie płatności |
| `/options` | Admin only | Zarządzanie opcjami |
| `/users` | Admin only | Zarządzanie użytkownikami |
| `*` | — | Redirect do `/` |

---

### Autentykacja (AuthContext)

- Logowanie → JWT + dane usera zapisywane do `localStorage`
- Rejestracja → przekierowanie do loginu (brak auto-loginu)
- Wylogowanie → czyszczenie `localStorage`
- Sesja persystowana po odświeżeniu strony
- Auto-redirect do `/login` przy odpowiedzi 401 (poza endpointem logowania)

---

### Strona Samochodów (`/`)

- Lista samochodów w formie kart (grid)
- Filtr: „Tylko dostępne" (toggle checkbox)
- **[Admin]** Dodaj samochód (modal z formularzem)
- **[Admin]** Edytuj samochód (modal z formularzem)
- **[Admin]** Usuń samochód (dialog potwierdzenia)
- Wyświetlane dane: marka, kolor, cena/dzień, dostępność, typ paliwa, kilometry, pojemność baku

---

### Nowa Rezerwacja (`/reservations/new`)

- Wybór auta z listy dostępnych (dropdown: marka, kolor, cena/dzień)
- Wybór daty od (min: dzisiaj)
- Wybór daty do (min: data od)
- Wybór opcji dodatkowych (checkboxy)
- **Live sidebar z podsumowaniem:** liczba dni, cena auta/dzień, opcje/dzień, łączna kwota
- Walidacja: wszystkie pola wymagane, data końcowa > startowa
- Zapis → inicjuje Stripe Checkout (przekierowanie do Stripe)

---

### Lista Rezerwacji (`/reservations`)

- **Admin:** widzi WSZYSTKIE rezerwacje
- **Użytkownik:** widzi tylko własne (filtrowanie po `userId` po stronie frontendu)
- Usuwanie rezerwacji (dialog potwierdzenia)
- Link do tworzenia nowej rezerwacji

---

### Szczegóły Rezerwacji (`/reservations/:id`)

- Dane: marka+kolor auta, typ paliwa, ID użytkownika, daty od/do, kwota
- Czas trwania w dniach
- Badge statusu (aktywna/nieaktywna)
- Lista wybranych opcji dodatkowych

---

### Potwierdzenie Płatności (`/payment/success`)

- Odczytuje `session_id` z URL
- Wywołuje `/payments/confirm` → aktualizuje rezerwację PENDING → PAID
- Sukces: numer rezerwacji, link do szczegółów rezerwacji, link na stronę główną
- Błąd: informacja że rezerwacja zostanie aktywowana automatycznie przez webhook

---

### Ceny Paliw (`/fuel-prices`)

- Pobiera dane z backendu (scraping autocentrum.pl)
- Tabela: region, typ paliwa, cena (zł/l)
- Filtry przyciskowe według typu paliwa (dynamiczne z danych)

---

### Opcje Dodatkowe (`/options`) — Admin only

- Lista opcji z nazwą i ceną/dzień
- Formularz dodawania opcji (nazwa + cena)
- Usuwanie opcji (dialog potwierdzenia)

---

### Użytkownicy (`/users`) — Admin only

- Tabela: ID, imię+nazwisko, login, rola
- Usuwanie użytkownika (dialog potwierdzenia)
- Ochrony: nie można usunąć siebie, nie można usunąć administratora

---

### Współdzielone komponenty

- `LoadingSpinner` — podczas operacji async
- `StatusBadge` — aktywna/nieaktywna rezerwacja
- `ConfirmDialog` — modal do potwierdzania niszczących operacji
- `CarCard` — karta samochodu z akcjami admin
- `CarModal` + `CarForm` — formularz dodawania/edycji auta
- `OptionCheckbox` — checkbox z opcją i ceną
- `ReservationTable` + `ReservationRow` — tabela rezerwacji z linkami
- `Navbar` — nawigacja: imię usera, linki admin, wylogowanie
- `Layout` — wrapper z Navbar dla chronionych stron

---

## Istotne ograniczenia / zachowania wpływające na edge cases

1. Hasła w plaintext (brak bcrypt)
2. `calculateOptionCosts` w serwisie sumuje ceny opcji (nie mnoży przez dni) — inaczej niż frontend, który mnoży przez dni
3. `updateReservation` wewnętrznie wywołuje `addReservation` — ustawia auto znowu jako niedostępne
4. Tworzenie rezerwacji przez webhook i przez `/confirm` jest idempotentne (sprawdzają `stripeSessionId`)
5. Filtrowanie rezerwacji user vs admin odbywa się **na frontendzie**, nie na backendzie — backend zawsze zwraca wszystkie
6. Walidacja username tylko przy rejestracji przez `/users/register`, nie przez `/api/auth/login`
7. Dostępność samochodów to prosta flaga boolean — brak obsługi konfliktów dat
8. Dwa endpointy logowania: `/api/auth/login` (JWT) i `/users/login` (legacy, bez JWT)
