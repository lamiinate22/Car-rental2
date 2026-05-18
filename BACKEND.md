# Backend — Car Rental API

Spring Boot 3.2.4 · Java 17 · MySQL · Gradle

---

## Uruchomienie

**Wymagania:** Java 17, MySQL 8 uruchomiony lokalnie.

```bash
# Utwórz bazę danych
mysql -u root -p -e "CREATE DATABASE car_rental;"

# Uruchom aplikację
./gradlew bootRun
```

Baza konfigurowana w [src/main/resources/application.properties](src/main/resources/application.properties).  
Schemat tworzony automatycznie (`ddl-auto=update`).

Przy starcie seeder dodaje domyślne opcje oraz konto admina (`admin` / `admin`).

---

## REST API

Wszystkie endpointy obsługują CORS (`*`). Bazowy URL: `http://localhost:8080`

### Samochody — `/cars`

| Metoda | Ścieżka | Opis |
|--------|---------|------|
| GET | `/cars` | Wszystkie samochody |
| GET | `/cars/{available}` | Samochody dostępne do wynajmu |
| POST | `/cars` | Dodaj nowy samochód |
| PUT | `/cars` | Zaktualizuj dane samochodu |
| DELETE | `/cars/{carId}` | Usuń samochód |

**CarDto:**
```json
{
  "id": 1,
  "colour": "Czarny",
  "carBrand": "Toyota Corolla",
  "kilometers": 45000,
  "price": 150.00,
  "availability": true,
  "fuel": "95",
  "fuelCapacity": 50.0
}
```

---

### Rezerwacje — `/reservations`

| Metoda | Ścieżka | Opis |
|--------|---------|------|
| GET | `/reservations` | Wszystkie rezerwacje |
| GET | `/reservations/{id}` | Konkretna rezerwacja |
| POST | `/reservations` | Utwórz rezerwację |
| PUT | `/reservations/{id}` | Zaktualizuj rezerwację |
| DELETE | `/reservations/delete/{id}` | Usuń rezerwację |
| POST | `/reservations/add/{reservationId}/{carId}` | Przypisz auto do rezerwacji |

**ReservationDto:**
```json
{
  "id": 1,
  "startDate": "2026-06-01",
  "endDate": "2026-06-07",
  "totalPrice": 1050.00,
  "status": true,
  "userId": 2,
  "carId": 5,
  "optionIds": [1, 3],
  "optionNames": ["GPS", "Dodatkowy kierowca"]
}
```

**Kalkulacja ceny:**
- Cena bazowa = cena auta/dzień × liczba dni
- \+ suma wybranych opcji
- \+ koszty naprawy uszkodzeń

---

### Opcje — `/options`

| Metoda | Ścieżka | Opis |
|--------|---------|------|
| GET | `/options` | Wszystkie opcje |
| POST | `/options` | Dodaj opcję |
| DELETE | `/options/{id}` | Usuń opcję |

Domyślne opcje (dodawane przez seeder):

| Opcja | Cena/dzień |
|-------|-----------|
| GPS | 5,00 PLN |
| Fotelik dziecięcy | 7,50 PLN |
| Dodatkowy kierowca | 10,00 PLN |

---

### Paliwa — `/fuels`

| Metoda | Ścieżka | Opis |
|--------|---------|------|
| GET | `/fuels/prices` | Aktualne ceny paliw (scraping z autocentrum.pl) |
| GET | `/fuels/types` | Lista typów paliw |

**Typy paliw:** `95`, `98`, `ON`, `ON+`, `LPG`

**FuelPriceDto:**
```json
{
  "region": "Mazowieckie",
  "fuelType": "95",
  "price": 6.24
}
```

---

## Model danych

```
USERS ──────────────────── RESERVATIONS ─────── CARS
  id, username, password,    id, start_date,      id, car_brand, colour,
  first_name, last_name,     end_date,            price, kilometers,
  is_admin                   total_price,         availability, fuel,
                             status, mileage,     fuel_capacity
                             ended,
                             user_id FK,          OPTIONS
                             car_id FK    ────── id, name, price
                                    │               (join: RESERVATION_OPTION)
                                    └──── DAMAGE
                                            id, description, repair_cost

                                          FUEL_USAGE
                                            id, start_km, end_km,
                                            fuel_price, total_cost,
                                            fuel_consumption, fuel_type
```

---

## Stack

| Warstwa | Technologia |
|---------|-------------|
| Framework | Spring Boot 3.2.4 |
| Język | Java 17 |
| Build | Gradle |
| Baza danych | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| UI (opcjonalne) | Vaadin 24 (pod `/ui/*`) |
| Scraping HTML | Jsoup 1.14.3 |
| Testy | JUnit 5 + H2 (in-memory) |

---

## Znane ograniczenia

- Brak Spring Security — endpointy są publiczne, brak JWT/sesji
- Hasła przechowywane jako plaintext
- Dane dostępowe do bazy w `application.properties` (nie commitować do repozytorium publicznego)
- Logowanie użytkownika działa tylko in-memory (nie przeżywa restartu)
