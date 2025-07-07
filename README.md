# Plan testowania wydajności baz danych na Dockerze

## Wymagane technologie i narzędzia

- **Java 17+** (Spring Boot)
- **Maven**
- **Docker & Docker Compose**
- **MySQL 8.0** oraz **PostgreSQL 16**
- **Prometheus** + **Grafana** (monitoring)
- **Micrometer** (integracja metryk z Prometheus)
- **Java Faker** (symulacja użytkowników i danych)
- **JUnit** (testy automatyczne)
- **Testcontainers** (opcjonalnie, do testów integracyjnych)
- **Apache JMeter** lub **Gatling** (opcjonalnie, do testów wydajnościowych)

---

## 1. Stworzenie kontrolerów (REST API)

- Utwórz REST API dla operacji CRUD na encjach: Customer, Product, Order, OrderItem.
- Zaimplementuj endpointy do masowego dodawania, pobierania i usuwania danych.
- Dodaj endpointy do testów transakcyjnych (np. złożone zamówienie z wieloma produktami).

## 2. Zbieranie i analiza metryk (Prometheus + Micrometer)

- Dodaj zależność Micrometer do projektu (`micrometer-registry-prometheus`).
- Skonfiguruj Prometheus do zbierania metryk z aplikacji Spring Boot.
- Upewnij się, że metryki są widoczne w Grafanie (np. czas odpowiedzi, liczba zapytań, błędy).
- Dodaj własne metryki (np. czas wykonania zapytań do bazy, liczba transakcji).
- **Zbieraj i analizuj metryki:**
  - Czas odpowiedzi (response time)
  - Throughput (liczba operacji na sekundę)
  - Zużycie CPU/RAM (monitoring systemowy przez Prometheus/Grafana)

## 3. Testy porównawcze

- Przygotuj skrypty do masowego generowania danych (Java Faker).
- Przeprowadź testy wydajnościowe dla obu baz:
  - Insert, update, select, delete (różne rozmiary danych: 1k, 10k, 100k, 1M rekordów)
  - Testy na pojedynczych i wielu wątkach.
  - Testy dla miliona rekordów (insert, update, select, delete) – wyniki zapisz w pliku (np. CSV).
- Zbieraj i analizuj metryki (czas odpowiedzi, throughput, zużycie CPU/RAM).

## 4. Porównanie różnych typów indeksów

- Utwórz różne typy indeksów (np. pojedyncze, złożone, fulltext, partial).
- Przeprowadź testy wydajnościowe na zapytaniach korzystających z tych indeksów.
- Porównaj wpływ indeksów na czas zapytań i operacji modyfikujących dane.

## 5. Testy transakcyjności (ACID) i poziomów izolacji

- Zaimplementuj testy sprawdzające zachowanie baz przy różnych poziomach izolacji transakcji.
- Przetestuj typowe scenariusze ACID (np. równoczesne zamówienia, wycofanie transakcji).
- Zbierz metryki dotyczące blokad, deadlocków, rollbacków.

## 6. Wielowątkowość

- Przygotuj testy symulujące wielu użytkowników (Java Faker + wielowątkowość w Spring lub narzędzia typu JMeter/Gatling).
- Przeprowadź testy obciążeniowe (różna liczba wątków, różne scenariusze użytkowników).
- Zbierz i porównaj metryki skalowalności i stabilności.

---

## Monitorowane metryki

### 1. Metryki wydajności zapytań
- **Czas wykonania zapytań (db.operation.time)**
  - Mierzy czas wykonania każdego typu operacji (SELECT, INSERT, UPDATE, DELETE)
  - Rozbicie na poszczególne typy zapytań
  - Statystyki: średni czas, percentyle (p95, p99), max/min

- **Wykorzystanie indeksów (db.index.usage)**
  - Licznik wykorzystania poszczególnych indeksów
  - Skuteczność indeksów (trafienia vs pełne przeszukiwania)
  - Wpływ indeksów na wydajność zapytań

- **Złożone zapytania (db.join.duration)**
  - Czas wykonania zapytań z JOIN-ami
  - Liczba tabel w złączeniu
  - Wpływ liczby złączeń na wydajność

### 2. Metryki transakcyjne
- **Transakcje (db.transaction.duration)**
  - Czas trwania transakcji
  - Liczba transakcji na sekundę
  - Stosunek udanych do nieudanych transakcji
  - Liczba rollback'ów

- **Poziomy izolacji (db.transaction.isolation)**
  - Wpływ poziomu izolacji na wydajność
  - Konflikty między transakcjami
  - Deadlock'i i timeouty

### 3. Metryki pamięci i cache
- **Cache (db.cache.hits)**
  - Cache hit ratio
  - Wykorzystanie bufora
  - Efektywność cache'owania zapytań
  - Wpływ wielkości cache na wydajność

- **Zarządzanie pamięcią**
  - Wykorzystanie pamięci przez bazę
  - Częstotliwość wymiatania danych z cache
  - Fragmentacja pamięci

### 4. Metryki współbieżności
- **Blokady (db.lock.wait.time)**
  - Czas oczekiwania na blokady
  - Liczba i typ blokad
  - Konflikty blokad
  - Deadlock'i

- **Równoległe operacje**
  - Liczba równoczesnych połączeń
  - Wydajność przy różnej liczbie wątków
  - Konflikty przy równoległym dostępie

### 5. Metryki danych
- **Rozmiar (db.table.size)**
  - Rozmiar tabel i indeksów
  - Tempo wzrostu danych
  - Fragmentacja tabel
  - Liczba wierszy

- **Operacje (db.operations)**
  - Liczba operacji na sekundę
  - Rozkład typów operacji
  - Błędy i wyjątki

### 6. Metryki systemowe
- **Zasoby systemowe**
  - Wykorzystanie CPU
  - Wykorzystanie RAM
  - IOPS (operacje I/O na sekundę)
  - Wykorzystanie dysku

- **Połączenia**
  - Liczba aktywnych połączeń
  - Czas życia połączeń
  - Wykorzystanie puli połączeń

### Wizualizacja w Grafanie

Wszystkie metryki są dostępne w dedykowanych dashboardach:
1. **Database Performance** - podstawowe metryki wydajnościowe
2. **Advanced Database Metrics** - rozszerzone metryki i analizy
3. **MySQL Overview** - specyficzne metryki dla MySQL
4. **PostgreSQL Overview** - specyficzne metryki dla PostgreSQL
5. **Spring Boot Statistics** - metryki aplikacji

Każdy dashboard zawiera:
- Wykresy czasowe (trendy)
- Histogramy rozkładu
- Heatmapy dla analizy wzorców
- Tabele z szczegółowymi statystykami
- Alerty dla wartości krytycznych

---

## Przykładowa kolejność prac

1. Utwórz modele, repozytoria i serwisy dla encji.
2. Dodaj kontrolery REST.
3. Skonfiguruj Prometheus, Micrometer i Grafanę.
4. Przygotuj skrypty do generowania danych (Java Faker).
5. Zaimplementuj testy wydajnościowe i transakcyjne (w tym testy na milionie rekordów, wyniki zapisz w pliku CSV).
6. Dodaj i przetestuj różne typy indeksów.
7. Przeprowadź testy wielowątkowe.
8. Analizuj wyniki w Grafanie.

---

## Dodatkowe uwagi

- Wszystkie testy uruchamiaj na obu bazach (zmiana profilu Spring: `mysql`/`postgres`).
- Wyniki i wnioski dokumentuj na bieżąco (np. w osobnym pliku `results.md`).
- W razie potrzeby rozbuduj testy o kolejne scenariusze (np. testy awarii, backup/restore).
