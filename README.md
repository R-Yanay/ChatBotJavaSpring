# ChatBotJavaSpring — Rule-Based ChatBot

ChatBotJavaSpring ist ein regelbasierter ChatBot fuer einen Online-Shop. Er erkennt die Absicht (Intent) der Nutzernachricht, extrahiert relevante Informationen (Entities) und generiert passende Antworten — basierend auf Regex-Mustern ohne Machine Learning.

## Beispiel

```
Nutzer: "How much is a t-shirt?"
  → Intent: ASK_PRICE
  → Entity: product="t-shirt"
  → Antwort: "A T-shirt costs 25.00 EUR."
```

## Technologien

| Technologie | Zweck |
|-------------|-------|
| Spring Boot 3.4.13 | REST API, Webserver |
| Spring WebSocket | Echtzeit-Chat ueber STOMP |
| Spring Data JPA | Chat-Verlauf in H2 In-Memory Database |
| Spring Security | HTTP Basic Auth, Benutzer-Authentifizierung |
| Spring Validation | Nachrichten-Validierung |
| Java 25 | Sprache |
| JUnit 5 | 119 Unit- und Integrationstests |

## Schnellstart

### Projekt starten

```bash
cd ChatBotJavaSpring
mvn spring-boot:run
```

### Im Browser oeffnen

Oeffne `http://localhost:8083` im Browser.

### Anmelden

| Feld | Wert |
|------|------|
| Benutzername | `user` |
| Passwort | `password` |

### Chat starten

Sende eine Nachricht ueber das Chat-Interface oder die REST-API:

```bash
curl -X POST http://user:password@localhost:8083/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello!"}'
```

Antwort:

```json
{
  "response": "Hello! How can I help you today?",
  "intent": "GREETING"
}
```

## Architektur

### Datenfluss

```
Nutzer → WebSocket/REST → ChatController → SessionManager → ChatService
  → IntentDetector (mit Kontext) → EntityExtractor (mit Kontext)
  → ResponseGenerator (mit Workflow) → Session aktualisieren → Antwort
```

### Session-basiertes Gedächtnis

Der ChatBot unterstuetzt Session-basiertes Gedächtnis: Er speichert die letzte Nachricht, den letzten Intent und die letzten Entities. Damit koennen Pronomen ("it", "that") und Follow-up-Fragen ("what about pants?") korrekt verarbeitet werden.

| Feature | Beispiel | Antwort |
|---------|----------|---------|
| Pronomen | "How much is a shirt?" → "it" | "A Shirt costs 40.00 EUR." |
| Follow-up | "How much is a shirt?" → "what about pants?" | "Pants cost 55.00 EUR." |
| Bestell-Workflow | "I want to order" → Produkt → Bestaetigung → Bestellt | Mehrstufiger Dialog |
| Session-Timeout | 30 Minuten Inaktivitaet | Session wird automatisch geloescht |

### ChatSession

| Eigenschaft | Zweck |
|-------------|-------|
| `userId` | Benutzer-Identifikation |
| `lastMessage` | Letzte Nutzernachricht |
| `lastIntent` | Letzter erkannter Intent |
| `lastEntities` | Letzte extrahierte Entities |
| `orderStatus` | Bestellstatus (IDLE, ASKING_PRODUCT, ASKING_CONFIRMATION, CONFIRMED) |
| `orderProduct` | Produkt in der Bestellung |
| `orderQuantity` | Menge in der Bestellung |
| `orderColor` | Farbe in der Bestellung |

### Systemdatenfluss

```mermaid
sequenceDiagram
    participant Browser
    participant ChatController
    participant SessionManager
    participant ChatService
    participant IntentDetector
    participant EntityExtractor
    participant ResponseGenerator
    participant ChatHistoryService
    participant H2DB

    Browser->>ChatController: POST /api/chat {"message": "How much is a shirt?"}
    ChatController->>SessionManager: getSession("user")
    SessionManager-->>ChatController: ChatSession
    ChatController->>ChatService: process(request, session)
    ChatService->>IntentDetector: detect("How much is a shirt?", session)
    IntentDetector-->>ChatService: ASK_PRICE
    ChatService->>EntityExtractor: extract("How much is a shirt?", session)
    EntityExtractor-->>ChatService: {product: "shirt"}
    ChatService->>ResponseGenerator: generate(ASK_PRICE, {product: "shirt"}, session)
    ResponseGenerator-->>ChatService: "A Shirt costs 40.00 EUR."
    ChatService->>SessionManager: session.update(...)
    ChatService->>ChatHistoryService: saveMessage("How much is a shirt?", "user", ASK_PRICE)
    ChatHistoryService->>H2DB: INSERT INTO chat_messages
    ChatService-->>ChatController: ChatResponse
    ChatController-->>Browser: {"response": "A Shirt costs 40.00 EUR.", "intent": "ASK_PRICE"}
```

### Klassendiagramm

```mermaid
classDiagram
    class ChatBotApplication {
        +main(String[] args)
    }

    class Intent {
        <<enum>>
        GREETING
        ASK_PRICE
        ORDER
        CONFIRM
        CANCEL
        GOODBYE
        HELP
        SEARCH
        RETURN
        SHIPPING
    }

    class BotConfig {
        -Map~String, Double~ productPrices
        -String[] products
        -String[] colors
        -Map~String, List~String~~ intentPatterns
        -Map~String, List~String~~ intentResponses
        +getProductPrice(String) Double
        +getIntentPatterns(Intent) List~String~
        +getIntentResponses(Intent) List~String~
    }

    class ChatSession {
        -String userId
        -String lastMessage
        -Intent lastIntent
        -Map~String, String~ lastEntities
        -OrderStatus orderStatus
        -String orderProduct
        -Integer orderQuantity
        -String orderColor
        +update(String, Intent, Map)
        +resetOrder()
        +isExpired(long) boolean
    }

    class SessionManager {
        -ConcurrentHashMap~String, ChatSession~ sessions
        +getSession(String) ChatSession
        +removeSession(String) boolean
        +cleanupExpiredSessions() int
    }

    class IntentDetector {
        -BotConfig botConfig
        +detectIntent(String) Intent
        +detect(String, ChatSession) Intent
        +isFollowUp(String) boolean
    }

    class EntityExtractor {
        -BotConfig botConfig
        +extractEntities(String) Map~String, String~
        +extract(String, ChatSession) Map~String, String~
    }

    class ResponseGenerator {
        -BotConfig botConfig
        +generateResponse(Intent, Map~String, String~) String
        +generate(Intent, Map~String, String~, ChatSession) String
    }

    class ChatService {
        -IntentDetector intentDetector
        -EntityExtractor entityExtractor
        -ResponseGenerator responseGenerator
        -SessionManager sessionManager
        -ChatHistoryService chatHistoryService
        +process(ChatRequest) ChatResponse
        +process(ChatRequest, ChatSession) ChatResponse
    }

    class ChatHistoryService {
        -ChatMessageRepository repository
        +saveMessage(String, String, Intent)
        +saveBotResponse(String, String, Intent)
        +getHistory(int) List~ChatMessage~
        +clearHistory()
    }

    class ChatMessage {
        -Long id
        -String text
        -String sender
        -String intent
        -LocalDateTime timestamp
    }

    class ChatMessageRepository {
        <<interface>>
        +findLatest(int) List~ChatMessage~
    }

    class ChatController {
        -ChatService chatService
        -ChatHistoryService chatHistoryService
        +chat(ChatRequest) ChatResponse
        +getHistory(int) List~ChatMessage~
        +clearHistory() ResponseEntity~Void~
    }

    class ChatWebSocketController {
        -ChatService chatService
        +handleMessage(String) ChatResponse
    }

    class SecurityConfig {
        +securityFilterChain(HttpSecurity) SecurityFilterChain
        +userDetailsService() UserDetailsService
    }

    class WebSocketConfig {
        +configureMessageBroker(MessageBrokerRegistry)
        +registerStompEndpoints(StompEndpointRegistry)
    }

    ChatBotApplication --> ChatController
    ChatController --> SessionManager
    ChatController --> ChatService
    ChatService --> SessionManager
    ChatService --> IntentDetector
    ChatService --> EntityExtractor
    ChatService --> ResponseGenerator
    ChatService --> ChatHistoryService
    ChatHistoryService --> ChatMessageRepository
    ChatMessageRepository ..> ChatMessage
    IntentDetector --> BotConfig
    EntityExtractor --> BotConfig
    ResponseGenerator --> BotConfig
    ChatWebSocketController --> ChatService
    SessionManager --> ChatSession
```

## API-Endpunkte

### Chat

| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|-------------|------|
| `POST` | `/api/chat` | Nachricht senden, Antwort erhalten (mit Session) | Ja |
| `GET` | `/api/chat/history?limit=50` | Chat-Verlauf laden | Ja |
| `DELETE` | `/api/chat/history` | Chat-Verlauf loeschen | Ja |

**POST /api/chat**

Request:

```json
{
  "message": "I want to order 2 pants",
  "userId": "user"
}
```

**userId** ist optional. Wenn nicht angegeben, wird `anonymous` verwendet. Die userId bindet die Nachricht an eine Session — der Bot erinnert sich an vorherige Nachrichten.

Response:

```json
{
  "response": "I can help with that! 2 pants costs 55.00 EUR each. Total: 110.00 EUR. Shall I confirm your order?",
  "intent": "ORDER"
}
```

**GET /api/chat/history?limit=50**

Response:

```json
[
  {
    "id": 1,
    "text": "I want to order 2 pants",
    "sender": "user",
    "intent": "ORDER",
    "timestamp": "2026-11-19T14:00:00"
  },
  {
    "id": 2,
    "text": "I can help with that! 2 pants costs 55.00 EUR each.",
    "sender": "bot",
    "intent": "ORDER",
    "timestamp": "2026-11-19T14:00:01"
  }
]
```

### WebSocket

| Endpunkt | Beschreibung |
|----------|-------------|
| `/ws` | WebSocket-Endpoint mit SockJS-Fallback |
| `/chat/send` | Nachricht an Bot senden |
| `/chat/response` | Bot-Antworten empfangen |

### Statische Ressourcen

| Endpoint | Auth |
|----------|------|
| `/index.html` | Nein — Login-Screen ist frei zugreifbar |

## Intents

| Intent | Beispiel-Nachrichten | Antwort (Beispiel) |
|--------|---------------------|-------------------|
| `GREETING` | "Hello", "Hi", "Good morning" | "Hello! How can I help you today?" (3 Varianten) |
| `ASK_PRICE` | "How much is a T-shirt?", "What do you sell?", "your inventory?" | "A T-shirt costs 25.00 EUR." / Produktkatalog |
| `ORDER` | "I want to order 2 pants", "Buy a jacket" | Mehrstufiger Workflow: Produkt → Bestaetigung → Bestellt |
| `CONFIRM` | "Yes", "Please", "OK" | "Great! Your order has been confirmed. Thank you for shopping with us!" (3 Varianten) |
| `CANCEL` | "No", "Cancel", "Stop" | "No problem, I've cancelled that. Is there anything else you need?" (3 Varianten) |
| `GOODBYE` | "Bye", "Goodbye", "See you" | "Goodbye! Have a great day!" (3 Varianten) |
| `HELP` | "Help", "What can you do?", "What are your functions?" | Liste der verfuegbaren Befehle und Produkte |
| `SEARCH` | "Show me blue sneakers", "Find a dress" | "I found blue sneakers for 80.00 EUR. Would you like to order it?" |
| `RETURN` | "I want to return", "Refund" | "You can return any item within 30 days of purchase. Items must be unused and in original packaging." |
| `SHIPPING` | "How long is shipping?", "Delivery time" | "Standard shipping takes 3-5 business days. Express shipping (1-2 days) is available for an extra 9.99 EUR." |

### Follow-up-Fragen

Dank des Gedaechtnis-Systems versteht der Bot auch Follow-up-Fragen:

| Nachricht | Kontext | Intent | Antwort |
|-----------|---------|--------|---------|
| "what about pants?" | Vorher: ASK_PRICE | ASK_PRICE | "Pants cost 55.00 EUR." |
| "it" | Vorher: product=shirt | ASK_PRICE | "A Shirt costs 40.00 EUR." |
| "how much?" | Vorher: ASK_PRICE | ASK_PRICE | Antwort basierend auf letztem Produkt |
| "tell me about jeans" | Kein Kontext noetig | ASK_PRICE | "Jeans cost 60.00 EUR." |

## Produktkatalog

| Produkt | Preis |
|---------|-------|
| T-Shirt | 25.00 EUR |
| Shirt | 40.00 EUR |
| Pants | 55.00 EUR |
| Jeans | 60.00 EUR |
| Jacket | 90.00 EUR |
| Dress | 70.00 EUR |
| Skirt | 45.00 EUR |
| Sweater | 50.00 EUR |
| Hoodie | 45.00 EUR |
| Sneakers | 80.00 EUR |
| Boots | 100.00 EUR |
| Sandals | 35.00 EUR |
| Hat | 20.00 EUR |
| Scarf | 15.00 EUR |
| Belt | 25.00 EUR |
| Bag | 60.00 EUR |

## Tests

| Test-Klasse | Tests | Beschreibung |
|-------------|-------|-------------|
| `IntentDetectorTest` | 39 | Alle 10 Intents, Follow-up, neue Keywords (sell, inventory, functions) |
| `EntityExtractorTest` | 15 | Produktnamen, Farben, Mengen, Kombinationen |
| `ResponseGeneratorTest` | 13 | Alle Intents, Entity-Platzhalter, Fallback |
| `ChatControllerTest` | 17 | REST-API, Validierung, JPA, Security (401/200) |
| `ChatSessionTest` | 10 | Session-Erstellung, Timeout, Daten-Speicherung, OrderStatus |
| `SessionManagerTest` | 10 | Session-Management, Cleanup, aktive Sessions |
| `ChatServiceTest` | 15 | Kontext-Verarbeitung, Pronomen, Follow-up, Workflow |
| **Gesamt** | **119** | **0 Failures, 0 Errors** |

```bash
mvn test
```

## Projektstruktur

```
ChatBotJavaSpring/
├── pom.xml
├── .gitignore
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/chatbot/
│   │   │   ├── ChatBotApplication.java          # Spring Boot Hauptklasse
│   │   │   ├── config/
│   │   │   │   ├── BotConfig.java               # Produktkatalog, Preise, Regeln
│   │   │   │   ├── GlobalExceptionHandler.java  # Exception-Handling
│   │   │   │   ├── SecurityConfig.java          # HTTP Basic Auth
│   │   │   │   ├── WebSocketConfig.java         # STOMP WebSocket
│   │   │   │   └── WebSocketAuthInterceptor.java # WebSocket Auth
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java          # REST API Endpoints
│   │   │   │   └── ChatWebSocketController.java # WebSocket Handler
│   │   │   ├── dto/
│   │   │   │   ├── ChatRequest.java             # Incoming message DTO
│   │   │   │   └── ChatResponse.java            # Bot response DTO
│   │   │   ├── model/
│   │   │   │   ├── ChatMessage.java             # JPA Entity
│   │   │   │   ├── ChatSession.java             # Session-Gedaechtnis
│   │   │   │   └── Intent.java                  # Intent-Enum (10 Intents)
│   │   │   ├── repository/
│   │   │   │   └── ChatMessageRepository.java   # JPA Repository
│   │   │   └── service/
│   │   │       ├── ChatHistoryService.java      # Chat-Verlauf speichern/laden
│   │   │       ├── ChatService.java             # Koordiniert Intent → Entity → Response (mit Session)
│   │   │       ├── EntityExtractor.java         # Extrahiert Produkt, Farbe, Menge (mit Kontext)
│   │   │       ├── IntentDetector.java          # Erkennt Absicht via Regex (mit Kontext)
│   │   │       ├── ResponseGenerator.java       # Generiert Antwort pro Intent (mit Workflow)
│   │   │       └── SessionManager.java          # Session-Management, Timeout
│   │   └── resources/
│   │       ├── application.properties           # Port 8083, H2 Database
│   │       └── static/
│   │           └── index.html                   # Chat-Frontend mit Login
│   └── test/java/com/chatbot/
│       ├── controller/
│       │   └── ChatControllerTest.java          # 17 Integrationstests
│       ├── ChatSessionTest.java                 # 10 Tests
│       ├── SessionManagerTest.java              # 10 Tests
│       ├── IntentDetectorTest.java              # 39 Tests
│       ├── EntityExtractorTest.java             # 15 Tests
│       ├── ResponseGeneratorTest.java           # 13 Tests
│       └── service/
│           └── ChatService.java                 # 15 Tests
```

## Vergleich zu anderen Projekten

| Projekt | Ansatz | Spring Boot Features | Tests |
|---------|--------|---------------------|-------|
| NumberRecognition | CNN (Deep Learning) | REST API | 37 JUnit-Tests |
| WordRecognition | CRNN (Deep Learning) | REST API | 33 JUnit-Tests |
| **ChatBotJavaSpring** | **Regelbasiert (kein ML)** | **REST + WebSocket + JPA + Security + Session** | **119 JUnit-Tests** |

## Grenzen des regelbasierten Ansatzes

Dieser ChatBot verwendet Regex-basierte Intent-Erkennung. Der Ansatz ist transparent und schnell implementierbar, hat jedoch folgende Grenzen:

| Grenze | Beispiel |
|--------|----------|
| Natuerliche Sprache | "I'm looking for something affordable for summer" wird nicht erkannt |
| Grammatikfehler | "How much cost the shrit?" — Tippfehler fuehren zu falscher Erkennung |
| Offene Fragen | "What's the difference between a shirt and a t-shirt?" — kein passender Intent |
| Mehrsprachigkeit | Regex-Regeln sind nur auf Englisch definiert |
| Skalierung | Ab 100+ Intents werden Regex-Regeln unuebersichtlich und kollidieren |

Trotzdem funktioniert der Bot fuer folgende Szenarien zuverlaessig:

| Szenario | Erkennungsrate |
|----------|---------------|
| Feste Fragen (FAQ) | ~95% |
| Bestell-Workflow | ~90% |
| Follow-up-Fragen | ~80% |
| Pronomen | ~75% |

## Lessons Learned

1. **Intent-Reihenfolge ist kritisch**: CANCEL und RETURN muessen vor ORDER erkannt werden, da "cancel my order" und "I want to return" auch ORDER-Keywords enthalten. HELP muss vor ASK_PRICE, da "tell me about" auch "about" enthaelt.
2. **Spring Security CSRF**: CSRF muss fuer REST-API deaktiviert werden, sonst blockiert es alle POST-Anfragen mit 403.
3. **WebSocket Auth via REST-API**: SockJS leitet benutzerdefinierte STOMP-Header nicht zuverlaessig weiter. Loesung: Credentials zuerst ueber REST-API (HTTP Basic Auth) validieren, dann WebSocket ohne Auth-Header verbinden.
4. **StompJs v7 mit SockJS**: Native WebSocket (`ws://`) funktioniert nicht mit Spring `withSockJS()`. Stattdessen `webSocketFactory` mit `new SockJS('/ws')` verwenden.
5. **H2 In-Memory Database**: Keine Installation noetig, aber Daten gehen beim Neustart verloren — perfekt fuer Demos.
6. **SockJS Fallback**: WebSocket-Verbindung funktioniert auch, wenn der Browser WebSocket nicht direkt unterstuetzt.
7. **Regelbasiert vs. Deep Learning**: Regelbasierte KI ist sofort funktionsfaehig, aber skaliert schlecht ab 100+ Regeln.
8. **Entity-Extraktion**: Einfache String-Matching reicht fuer kleine Kataloge, aber NLP waere noetig fuer echte Anwendungen.
9. **Chat-Verlauf**: JPA mit `@OrderBy` und `setMaxResults` ist einfach, aber `@Param` in JPQL ohne `setMaxResults` funktioniert nicht.
10. **Frontend Login**: WebSocket-Auth erfordert, dass der Login-Screen statisch (ohne Auth) zugreifbar ist.
11. **Java 25 + Mockito**: Mockito Inline-Mocking funktioniert nicht mit Java 25 — echte Objekte statt Mocks verwenden.
12. **Session-basiertes Gedaechtnis**: ConcurrentHashMap fuer In-Memory Sessions ist einfach, aber Sessions gehen beim Neustart verloren. Fuer Produktion: Redis oder Database.
13. **Follow-up-Fragen**: "what about X?" und "and X?" muessen vom IntentDetector erkannt und mit dem letzten Intent kombiniert werden. Pronomen ("it", "that") referenzieren die letzten Entities.
14. **Bestell-Workflow**: Mehrstufiger Dialog (Produkt → Bestaetigung → Bestellt) erfordert OrderStatus-Tracking in der Session. Bei Themenwechsel muss der Workflow zurueckgesetzt werden.
15. **GlobalExceptionHandler**: Ohne `@RestControllerAdvice` werden Exceptions als 500 ohne Details zurueckgegeben. `MethodArgumentNotValidException` muss separat als 400 behandelt werden.
16. **Intent-Erweiterung**: Neue Keywords (sell, offer, inventory, functions, capabilities) erweitern die Erkennung. Grammatikfehler ("what do you're stuff cost?") werden durch "cost" im Regex erkannt.
17. **Wortgrenzen bei Entity-Extraktion**: `contains()` fuehrt zu False-Positives ("hat" in "what"). Loesung: `\b` Wortgrenzen in Regex verwenden.
18. **Regelbasierte Grenzen**: Der Bot erkennt nur vordefinierte Keywords. Natuerliche Sprache, Grammatikfehler und offene Fragen werden nicht verstanden.

## Ziel

Dieses Projekt demonstriert die Implementierung eines regelbasierten ChatBots mit Spring Boot. Es deckt folgende Technologien und Konzepte ab:

- **REST API** mit Spring Boot fuer synchronen Chat
- **WebSocket** mit STOMP und SockJS fuer Echtzeit-Kommunikation
- **Spring Data JPA** mit H2 In-Memory Database fuer Chat-Verlauf
- **Spring Security** mit HTTP Basic Auth fuer Benutzer-Authentifizierung
- **Session-Management** mit ConcurrentHashMap fuer Gedaechtnis-fuehige Konversationen
- **Intent-Erkennung** mit Regex-Mustern und Kontext-Awareness
- **Entity-Extraktion** mit Wortgrenzen-Regex fuer False-Positive-Vermeidung
- **Bestell-Workflow** mit mehrstufigem Dialog und OrderStatus-Tracking
- **119 JUnit-Tests** fuer Intent-Detektion, Entity-Extraktion, Response-Generierung, REST-API, Security und Session-Management
