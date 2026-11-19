# ChatBotJavaSpring — Rule-Based ChatBot

**RuleBot** ist ein **regelbasierter ChatBot** für einen Online-Shop, der auf englische Nachrichten mit Wenn-Dann-Regeln antwortet — **ohne Deep Learning**. Im Gegensatz zu NumberRecognition und WordRecognition (die neuronale Netze verwenden) arbeitet RuleBot mit klar definierten Regeln, die vollständig kontrollierbar und sofort funktionsfähig sind.

## Was ist RuleBot?

RuleBot erkennt die Absicht (Intent) des Nutzers, extrahiert wichtige Informationen (Entities) und generiert passende Antworten — alles über Regex-basierte Mustererkennung.

```
Nutzer: "How much is a t-shirt?"
  → Intent: ASK_PRICE
  → Entity: product="t-shirt"
  → Antwort: "A T-shirt costs 25.00 Euro."
```

## Technologien

| Technologie | Zweck |
|-------------|-------|
| Spring Boot 3.4.13 | REST API, Webserver |
| Spring WebSocket | Echtzeit-Chat über STOMP |
| Spring Data JPA | Chat-Verlauf in H2 In-Memory Database |
| Spring Security | HTTP Basic Auth, Benutzer-Authentifizierung |
| Spring Validation | Nachrichten-Validierung |
| Java 25 | Sprache |
| JUnit 5 | 76 Unit- und Integrationstests |

## Schnellstart

### 1. Projekt starten

```bash
cd ChatBotJavaSpring
mvn spring-boot:run
```

### 2. Im Browser öffnen

Öffne `http://localhost:8083` im Browser.

### 3. Anmelden

| Feld | Wert |
|------|------|
| Benutzername | `user` |
| Passwort | `password` |

### 4. Chat starten

Schicke eine Nachricht über das Chat-Interface oder die REST-API:

```bash
curl -X POST http://user:password@localhost:8083/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello!"}'
```

Antwort:

```json
{
  "response": "A t-shirt costs 25.00 EUR.",
  "intent": "ASK_PRICE"
}
```

## Architektur

### CRNN vs. Regelbasiert

| Projekt | AI-Typ | Spring Boot Features |
|---------|--------|---------------------|
| NumberRecognition | CNN (Deep Learning) | REST API |
| WordRecognition | CRNN (Deep Learning) | REST API |
| **ChatBotJavaSpring** | **Regelbasiert (kein ML)** | **REST + WebSocket + JPA + Security** |

### Datenfluss

```
Nutzer → WebSocket/REST → ChatController → IntentDetector → EntityExtractor → ResponseGenerator → Antwort
```

### Systemdatenfluss

```mermaid
sequenceDiagram
    participant Browser
    participant ChatController
    participant ChatService
    participant IntentDetector
    participant EntityExtractor
    participant ResponseGenerator
    participant ChatHistoryService
    participant H2DB

    Browser->>ChatController: POST /api/chat {"message": "How much is a t-shirt?"}
    ChatController->>ChatService: processMessage("How much is a t-shirt?")
    ChatService->>IntentDetector: detectIntent("How much is a t-shirt?")
    IntentDetector-->>ChatService: ASK_PRICE
    ChatService->>EntityExtractor: extractEntities("How much is a t-shirt?")
    EntityExtractor-->>ChatService: {product: "t-shirt"}
    ChatService->>ResponseGenerator: generateResponse(ASK_PRICE, {product: "t-shirt"})
    ResponseGenerator-->>ChatService: "A T-shirt costs 25.00 Euro."
    ChatService->>ChatHistoryService: saveMessage("How much is a t-shirt?", "user", ASK_PRICE)
    ChatHistoryService->>H2DB: INSERT INTO chat_messages
    ChatService->>ChatHistoryService: saveBotResponse("A T-shirt costs 25.00 Euro.", "bot", ASK_PRICE)
    ChatHistoryService->>H2DB: INSERT INTO chat_messages
    ChatService-->>ChatController: ChatResponse
    ChatController-->>Browser: {"response": "A T-shirt costs 25.00 Euro.", "intent": "ASK_PRICE"}
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

    class IntentDetector {
        -BotConfig botConfig
        +detectIntent(String) Intent
    }

    class EntityExtractor {
        -BotConfig botConfig
        +extractEntities(String) Map~String, String~
    }

    class ResponseGenerator {
        -BotConfig botConfig
        +generateResponse(Intent, Map~String, String~) String
    }

    class ChatService {
        -IntentDetector intentDetector
        -EntityExtractor entityExtractor
        -ResponseGenerator responseGenerator
        -ChatHistoryService chatHistoryService
        +processMessage(String) ChatResponse
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
    ChatController --> ChatService
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
```

## API-Endpunkte

### Chat

| Methode | Endpoint | Beschreibung | Auth |
|---------|----------|-------------|------|
| `POST` | `/api/chat` | Nachricht senden, Antwort erhalten | Ja |
| `GET` | `/api/chat/history?limit=50` | Chat-Verlauf laden | Ja |
| `DELETE` | `/api/chat/history` | Chat-Verlauf löschen | Ja |

**POST /api/chat**

Request:

```json
{
  "message": "I want to order 2 pants"
}
```

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
| `/index.html` | Nein — Login-Screen ist frei zugänglich |

## Intents

| Intent | Beispiel-Nachrichten | Antwort (Beispiel) |
|--------|---------------------|-------------------|
| `GREETING` | "Hello", "Hi", "Good morning" | "Hello! How can I help you today?" (3 Varianten) |
| `ASK_PRICE` | "How much is a T-shirt?", "What's the price?" | "A t-shirt costs 25.00 EUR." |
| `ORDER` | "I want to order 2 pants", "Buy a jacket" | "I can help with that! 2 pants costs 55.00 EUR each. Total: 110.00 EUR. Shall I confirm your order?" |
| `CONFIRM` | "Yes", "Please", "OK" | "Great! Your order has been confirmed. Thank you for shopping with us!" (3 Varianten) |
| `CANCEL` | "No", "Cancel", "Stop" | "No problem, I've cancelled that. Is there anything else you need?" (3 Varianten) |
| `GOODBYE` | "Bye", "Goodbye", "See you" | "Goodbye! Have a great day!" (3 Varianten) |
| `HELP` | "Help", "What can you do?" | Liste der verfügbaren Befehle und Produkte |
| `SEARCH` | "Show me blue sneakers", "Find a dress" | "I found blue sneakers for 80.00 EUR. Would you like to order it?" |
| `RETURN` | "I want to return", "Refund" | "You can return any item within 30 days of purchase. Items must be unused and in original packaging." |
| `SHIPPING` | "How long is shipping?", "Delivery time" | "Standard shipping takes 3-5 business days. Express shipping (1-2 days) is available for an extra 9.99 EUR." |

## Produktkatalog

| Produkt | Preis |
|---------|-------|
| T-Shirt | 25.00 Euro |
| Shirt | 40.00 Euro |
| Pants | 55.00 Euro |
| Jeans | 60.00 Euro |
| Jacket | 90.00 Euro |
| Dress | 70.00 Euro |
| Skirt | 45.00 Euro |
| Sweater | 50.00 Euro |
| Hoodie | 45.00 Euro |
| Sneakers | 80.00 Euro |
| Boots | 100.00 Euro |
| Sandals | 35.00 Euro |
| Hat | 20.00 Euro |
| Scarf | 15.00 Euro |
| Belt | 25.00 Euro |
| Bag | 60.00 Euro |

## Tests

| Test-Klasse | Tests | Beschreibung |
|-------------|-------|-------------|
| `IntentDetectorTest` | 31 | Alle 10 Intents, Fall-Insensitivity, Regel-Reihenfolge |
| `EntityExtractorTest` | 15 | Produktnamen, Farben, Mengen, Kombinationen |
| `ResponseGeneratorTest` | 13 | Alle Intents, Entity-Platzhalter, Fallback |
| `ChatControllerTest` | 17 | REST-API, Validierung, JPA, Security (401/200) |
| **Gesamt** | **76** | **0 Failures, 0 Errors** |

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
│   │   │   │   ├── SecurityConfig.java          # HTTP Basic Auth
│   │   │   │   └── WebSocketConfig.java         # STOMP WebSocket
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java          # REST API Endpoints
│   │   │   │   └── ChatWebSocketController.java # WebSocket Handler
│   │   │   ├── dto/
│   │   │   │   ├── ChatRequest.java             # Incoming message DTO
│   │   │   │   └── ChatResponse.java            # Bot response DTO
│   │   │   ├── model/
│   │   │   │   ├── ChatMessage.java             # JPA Entity
│   │   │   │   └── Intent.java                  # Intent-Enum (10 Intents)
│   │   │   ├── repository/
│   │   │   │   └── ChatMessageRepository.java   # JPA Repository
│   │   │   └── service/
│   │   │       ├── ChatService.java             # Koordiniert Intent → Entity → Response
│   │   │       ├── ChatHistoryService.java      # Chat-Verlauf speichern/laden
│   │   │       ├── EntityExtractor.java         # Extrahiert Produkt, Farbe, Menge
│   │   │       ├── IntentDetector.java          # Erkennt Absicht via Regex
│   │   │       └── ResponseGenerator.java       # Generiert Antwort pro Intent
│   │   └── resources/
│   │       ├── application.properties           # Port 8083, H2 Database
│   │       └── static/
│   │           └── index.html                   # Chat-Frontend mit Login
│   └── test/java/com/chatbot/
│       ├── controller/
│       │   └── ChatControllerTest.java          # 17 Integrationstests
│       └── service/
│           ├── EntityExtractorTest.java         # 15 Tests
│           ├── IntentDetectorTest.java          # 31 Tests
│           └── ResponseGeneratorTest.java       # 13 Tests
```

## Vergleich zu deinen Projekten

| Projekt | AI-Typ | Spring Boot Features | Tests |
|---------|--------|---------------------|-------|
| NumberRecognition | CNN (Deep Learning) | REST API | 37 JUnit-Tests |
| WordRecognition | CRNN (Deep Learning) | REST API | 33 JUnit-Tests |
| **ChatBotJavaSpring** | **Regelbasiert (kein ML)** | **REST + WebSocket + JPA + Security** | **76 JUnit-Tests** |

## Lessons Learned

1. **Intent-Reihenfolge ist kritisch**: CANCEL und RETURN müssen vor ORDER erkannt werden, da "cancel my order" und "I want to return" auch ORDER-Keywords enthalten.
2. **Spring Security CSRF**: CSRF muss für REST-API deaktiviert werden, sonst blockiert es alle POST-Anfragen mit 403.
3. **WebSocket Auth**: HTTP Basic Auth funktioniert über STOMP-Connect-Header mit Base64-encoded Credentials.
4. **H2 In-Memory Database**: Keine Installation nötig, aber Daten gehen beim Neustart verloren — perfekt für Demos.
5. **SockJS Fallback**: WebSocket-Verbindung funktioniert auch, wenn der Browser WebSocket nicht direkt unterstützt.
6. **Regelbasiert vs. Deep Learning**: Regelbasierte KI ist sofort funktionsfähig, aber skaliert schlecht ab 100+ Regeln.
7. **Entity-Extraktion**: Einfache String-Matching reicht für kleine Kataloge, aber NLP wäre nötig für echte Anwendungen.
8. **Chat-Verlauf**: JPA mit `@OrderBy` und `setMaxResults` ist einfach, aber `@Param` in JPQL ohne `setMaxResults` funktioniert nicht.
9. **Frontend Login**: WebSocket-Auth erfordert, dass der Login-Screen statisch (ohne Auth) zugänglich ist.
10. **Java 25 + Mockito**: Mockito Inline-Mocking funktioniert nicht mit Java 25 — echte Objekte statt Mocks verwenden.

## Ziel

Nach dem Projekt kannst du sagen:

> "Ich habe einen regelbasierten ChatBot mit Spring Boot gebaut, der Echtzeit-Chat über WebSocket, Chat-Verlauf mit JPA und Benutzer-Authentifizierung mit Spring Security unterstützt. Das Projekt hat 76 JUnit-Tests und deckt REST API, WebSocket, JPA und Security ab."
