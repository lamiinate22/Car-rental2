Opis
Aplikacja to zaawansowany system zarządzania wypożyczalnią samochodów, który
umożliwia użytkownikom rezerwację pojazdów, śledzenie historii rezerwacji, a także
zarządzanie flotą samochodową. System jest zaprojektowany z myślą o dostarczaniu
intuicyjnego interfejsu użytkownika oraz solidnego zaplecza administracyjnego, które
umożliwia zarządzanie rezerwacjami, użytkownikami i opcjami dodatkowymi. Aplikacja
wspiera również logowanie i autoryzację użytkowników, zapewniając bezpieczeństwo i
integralność danych.

Główne komponenty aplikacji
Rezerwacja samochodów: Użytkownicy mogą przeglądać dostępne samochody, wybierać
daty wynajmu, oraz dodawać opcje dodatkowe takie jak GPS, foteliki dziecięce czy
dodatkowi kierowcy. Po dokonaniu rezerwacji użytkownik otrzymuje szczegółowe informacje
o swojej rezerwacji.
Zarządzanie użytkownikami: Administratorzy mogą zarządzać użytkownikami, dodawać
nowych użytkowników, aktualizować ich dane oraz usuwać konta. Aplikacja wspiera rolę
administratora, która ma dostęp do dodatkowych funkcjonalności administracyjnych.
Zarządzanie flotą samochodową: Administratorzy mogą dodawać nowe samochody do
floty, aktualizować ich dane techniczne, stan dostępności, oraz usuwać samochody z
systemu.
Opcje dodatkowe: Użytkownicy mogą dodawać różne opcje dodatkowe do swoich
rezerwacji. Administratorzy mogą zarządzać tymi opcjami, dodając nowe, aktualizując
istniejące i usuwając niepotrzebne.

Architektura aplikacji

Aplikacja składa się z front-endu oraz back-endu. Front-end zrealizowany jest w technologii
Vaadin, co umożliwia tworzenie interaktywnych i dynamicznych interfejsów użytkownika bez
konieczności korzystania z JavaScript. Back-end oparty jest na Spring Boot, zapewniając
solidne i skalowalne zaplecze serwerowe, zarządzanie bazą danych oraz usługi RESTful
API.

Proces rezerwacji
1. Wybór samochodu: Użytkownik przegląda dostępne samochody i wybiera ten, który
chce wynająć.
2. Wybór dat rezerwacji: Użytkownik wybiera daty rozpoczęcia i zakończenia
rezerwacji.
3. Dodanie opcji dodatkowych: Użytkownik wybiera opcje dodatkowe, które chce
dodać do rezerwacji (np. GPS, fotelik dziecięcy).
4. Zatwierdzenie rezerwacji: Po wypełnieniu wszystkich wymaganych pól, użytkownik
zatwierdza rezerwację. System oblicza całkowitą cenę rezerwacji i zapisuje dane w
bazie danych.
5. Wyświetlenie szczegółów rezerwacji: Po zatwierdzeniu rezerwacji użytkownik
otrzymuje szczegółowe informacje o swojej rezerwacji, które są wyświetlane na
stronie.

Lista technologii i języków użytych w projekcie
1. Java: Główny język programowania używany do tworzenia logiki aplikacji oraz
integracji z bazą danych.
2. Spring Boot: Framework używany do tworzenia aplikacji back-endowej. Oferuje
wsparcie dla tworzenia RESTful API, zarządzania zależnościami oraz integracji z
bazą danych.
3. Vaadin: Framework front-endowy do tworzenia dynamicznych i interaktywnych
interfejsów użytkownika w Javie.
4. MySQL: System zarządzania bazą danych używany do przechowywania danych
aplikacji, takich jak użytkownicy, rezerwacje, samochody i opcje dodatkowe.
5. Hibernate: Framework ORM (Object-Relational Mapping) używany do mapowania
obiektów Java na relacyjne struktury bazy danych.
6. Maven: Narzędzie do zarządzania projektem i zależnościami.
7. Spring Security: Używany do zarządzania bezpieczeństwem i autoryzacją
użytkowników.
8. Lombok: Biblioteka Java, która redukuje ilość kodu potrzebnego do tworzenia klas
Java poprzez automatyczne generowanie getterów, setterów, konstruktorów itp.

Lista funkcjonalności
Funkcjonalności użytkowników
1. Rejestracja użytkownika: Nowi użytkownicy mogą rejestrować się w systemie,
podając swoje dane osobowe i informacje logowania.
2. Logowanie: Zarejestrowani użytkownicy mogą logować się do systemu.
3. Przeglądanie dostępnych samochodów: Użytkownicy mogą przeglądać listę
dostępnych samochodów do wynajęcia.
4. Rezerwacja samochodów: Użytkownicy mogą rezerwować wybrane samochody na
określony czas, wybierając daty rezerwacji oraz opcje dodatkowe.
5. Wyświetlanie szczegółów rezerwacji: Po dokonaniu rezerwacji, użytkownicy mogą
wyświetlać szczegóły swojej rezerwacji, w tym daty wynajmu, całkowitą cenę, oraz
dodane opcje dodatkowe.
6. Wyświetlanie rodzajów paliw i ich cen w każdym województwie
Funkcjonalności administracyjne
1. Zarządzanie użytkownikami: Administratorzy mogą dodawać, edytować i usuwać
użytkowników, a także przypisywać im role administracyjne.
2. Zarządzanie samochodami: Administratorzy mogą dodawać nowe samochody do
floty, edytować dane techniczne istniejących samochodów oraz usuwać samochody
z systemu.
3. Zarządzanie rezerwacjami: Administratorzy mogą przeglądać, edytować i usuwać
rezerwacje, a także kończyć rezerwacje, aktualizując stan samochodów i naliczając
dodatkowe opłaty.
Dodatkowe funkcjonalności
1. Wyliczanie całkowitej ceny rezerwacji: System automatycznie oblicza całkowitą
cenę rezerwacji na podstawie długości wynajmu, ceny samochodu za dzień oraz cen
wybranych opcji dodatkowych.
2. Aktualizacja stanu samochodu: Po zakończeniu rezerwacji, system automatycznie
aktualizuje stan samochodu, w tym przebieg i dostępność.
3. Bezpieczeństwo danych: Aplikacja zapewnia bezpieczeństwo danych
użytkowników poprzez mechanizmy autoryzacji i autentykacji, oraz szyfrowanie
danych wrażliwych.
4. Obsługa różnych typów paliw: System uwzględnia różne typy paliw podczas
obliczania kosztów rezerwacji, bazując na aktualnych cenach paliw w różnych
regionach.
5. Intuicyjny interfejs użytkownika: Dzięki zastosowaniu Vaadin, aplikacja oferuje
nowoczesny, intuicyjny interfejs użytkownika, który ułatwia korzystanie z systemu
zarówno użytkownikom, jak i administratorom.

Integracja z zewnętrznymi serwisami
1. Pobieranie aktualnych cen paliw: System integruje się z zewnętrznymi serwisami
w celu pobierania aktualnych cen paliw, które są używane do obliczania kosztów
rezerwacji.

Testowanie i dokumentacja
1. Testy jednostkowe i integracyjne: Aplikacja zawiera szeroki zestaw testów
jednostkowych i integracyjnych, które zapewniają poprawność działania wszystkich
komponentów.
2. Dokumentacja API: Aplikacja jest wyposażona w dokumentację API, która
umożliwia deweloperom łatwe zrozumienie i integrację z systemem.
