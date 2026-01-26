# SBX Java Client Library

Lightweight Java client for SBX Cloud APIs. Compatible with Java 21+ and Spring Boot 3.x.

## Installation

### Via JitPack (Recommended)

**Maven:**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.socobox</groupId>
    <artifactId>sbxcloud-lib-java</artifactId>
    <version>v0.0.6</version>
</dependency>
```

**Gradle:**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.socobox:sbxcloud-lib-java:v0.0.6'
}
```

> Replace `v0.0.6` with the latest release tag or use `main-SNAPSHOT` for the latest commit.

### Local Build

```xml
<dependency>
    <groupId>com.sbxcloud</groupId>
    <artifactId>sbxcloud-lib-java</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Quick Start

### With Spring Boot (Auto-configuration)

Add to `application.properties`:

```properties
sbx.app-key=your-app-key-uuid
sbx.token=your-bearer-token
sbx.domain=0
sbx.base-url=https://sbxcloud.com
sbx.debug=false
```

Inject and use:

```java
@Service
public class ContactService {
    private final SBXService sbx;

    public ContactService(SBXService sbx) {
        this.sbx = sbx;
    }

    public List<Contact> findActiveContacts() {
        var query = FindQuery.from("contact")
            .andWhereIsEqualTo("status", "ACTIVE")
            .setPageSize(100);

        var response = sbx.find(query, Contact.class);
        return response.success() ? response.results() : List.of();
    }
}
```

### Without Spring Boot

```java
// From environment variables
var sbx = SBXServiceFactory.withEnv();

// Or with custom configuration
var sbx = SBXServiceFactory.builder()
    .appKey("your-app-key")
    .token("your-token")
    .domain(0)
    .baseUrl("https://sbxcloud.com")
    .debug(true)
    .build();
```

## Features

### Data Operations (CRUD)

#### Find

```java
// Simple query
var query = FindQuery.from("contact")
    .andWhereIsEqualTo("status", "ACTIVE")
    .setPage(1)
    .setPageSize(50);

var response = sbx.find(query, Contact.class);

// Find one
var single = sbx.findOne(query, Contact.class);

// Find all (auto-pagination)
var all = sbx.findAll(query, Contact.class);

// By keys
var byKeys = FindQuery.from("contact")
    .whereWithKeys("key1", "key2");
```

#### Query Builder

```java
FindQuery.from("contact")
    // AND conditions
    .newGroupWithAnd()
    .andWhereIsEqualTo("status", "ACTIVE")
    .andWhereIsGreaterThan("age", 18)
    .andWhereIsIn("type", "A", "B", "C")
    .andWhereContains("name", "John")
    .andWhereIsNull("deletedAt")

    // OR conditions
    .newGroupWithOr()
    .orWhereIsEqualTo("priority", "HIGH")
    .orWhereIsLessThan("dueDate", LocalDate.now())

    // Fetch related models
    .fetchModels("account", "owner")
    .fetchReferencingModels("tasks")
    .setAutowire("account")

    // Pagination
    .setPage(1)
    .setPageSize(50)
    .compile();
```

#### Create

```java
var contact = Map.of(
    "name", "John Doe",
    "email", "john@example.com",
    "status", "ACTIVE"
);

var response = sbx.create("contact", contact);
if (response.success()) {
    String newKey = response.keys().get(0);
}
```

#### Update

```java
var updates = Map.of(
    "_KEY", "existing-key",
    "status", "INACTIVE"
);

sbx.update("contact", updates);
```

#### Delete

```java
sbx.delete("contact", "key-to-delete");
sbx.delete("contact", List.of("key1", "key2", "key3"));
```

### Authentication

```java
// Login
var result = sbx.login("user@example.com", "password");
if (result.success()) {
    String token = result.token();
    SBXUser user = result.user();
}

// Validate session
var session = sbx.validateSession();

// Change password
sbx.changePassword("currentPassword", "newPassword", userId);

// Password reset flow
sbx.sendPasswordResetRequest("user@example.com", "Reset Password", "email-template-key");
sbx.resetPassword(userId, "reset-code", "newPassword");

// Check email availability
sbx.checkEmailAvailable("new@example.com");
```

### File Operations

```java
// Upload (base64 content)
sbx.uploadFile("document.pdf", base64Content, folderKey);

// Download
byte[] content = sbx.downloadFile("file-key");

// Delete
sbx.deleteFile("file-key");
```

### Folder Operations

```java
// Create folder
sbx.createFolder("New Folder", parentFolderKey);

// List contents
var contents = sbx.listFolder(folderKey);

// Rename
sbx.renameFolder(folderKey, "Renamed Folder");

// Delete
sbx.deleteFolder(folderKey);
```

### Email

```java
var email = EmailParams.builder()
    .from("sender@example.com")
    .to("recipient@example.com")
    .subject("Hello")
    .templateKey("email-template-key")
    .data(Map.of("name", "John"))
    .build();

sbx.sendEmail(email);
// or V2: sbx.sendEmailV2(email);
```

### Cloud Scripts

```java
// Run with parameters
Map<String, Object> params = Map.of("inputParam", "value");
MyResult result = sbx.runCloudScript("script-key", params, MyResult.class);

// Run without parameters
var result = sbx.runCloudScript("script-key", MyResult.class);

// Test mode
var result = sbx.runCloudScript("script-key", params, true, MyResult.class);
```

### Configuration

```java
// Load app configuration
sbx.loadConfig();

// Get cached config
SBXConfig config = sbx.getConfig();
List<SBXModel> models = config.models();
Map<String, Object> properties = config.properties();
```

### Multi-domain Support

```java
// Create multi-domain service
var sbx = SBXServiceFactory.multidomain("https://sbxcloud.com");

// Switch credentials per tenant
sbx.setMultidomainCredentials(domainId, appKey, token);

// Update just the token
sbx.setToken(newToken);
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `SBX_APP_KEY` | Application key (UUID) |
| `SBX_TOKEN` | Bearer token |
| `SBX_DOMAIN` | Domain ID (numeric) |
| `SBX_BASE_URL` | Base URL (without /api suffix) |

## Repository Pattern (Recommended)

The library provides a Spring Data-like repository pattern for type-safe, boilerplate-free data access.

### Define Your Entity

```java
@SbxModel("contact")                      // Maps to SBX model name
@JsonNaming(SbxNamingStrategy.class)      // Auto-maps key→_KEY, meta→_META
@JsonIgnoreProperties(ignoreUnknown = true)
public record Contact(
    String key,        // Automatically mapped to _KEY
    SBXMeta meta,      // Automatically mapped to _META
    String name,
    String email,
    String status
) implements SbxEntity {
    // Convenience constructor for new entities
    public Contact(String name, String email, String status) {
        this(null, null, name, email, status);
    }
}
```

### Use the Repository

```java
// Get a typed repository - model name inferred from @SbxModel
SbxRepository<Contact> contacts = sbx.repository(Contact.class);

// ========== Simple CRUD ==========

// Find
contacts.findAll();                           // All records
contacts.findById("key123");                  // Returns Optional<Contact>
contacts.findByIds("k1", "k2", "k3");        // Multiple keys
contacts.existsById("key123");               // Boolean check
contacts.count();                            // Total count

// Save (insert or update automatically)
var contact = new Contact("John", "john@example.com", "ACTIVE");
String key = contacts.save(contact);          // Insert, returns new key

var existing = contacts.findById(key).orElseThrow();
var updated = new Contact(existing.key(), existing.meta(),
    "John Updated", existing.email(), existing.status());
contacts.save(updated);                       // Update (has key)

contacts.saveAll(contact1, contact2);        // Batch save

// Delete
contacts.delete(entity);                      // By entity
contacts.deleteById("key123");               // By key
contacts.deleteByIds("k1", "k2", "k3");      // Multiple keys
contacts.deleteAll(entity1, entity2);        // Multiple entities

// ========== Fluent Queries ==========

contacts.query()
    .where(q -> q
        .newGroupWithAnd()
        .andWhereIsEqualTo("status", "ACTIVE")
        .andWhereIsGreaterThan("age", 18)
        .newGroupWithOr()
        .orWhereContains("name", "John"))
    .fetch("account", "owner")
    .page(1, 50)
    .list();                                  // Returns List<Contact>

// Shorthand queries
contacts.findWhere(q -> q.andWhereIsEqualTo("status", "ACTIVE"));

contacts.query()
    .whereEquals("status", "ACTIVE")         // Simple equality
    .first();                                // Optional<Contact>

contacts.query()
    .whereKeys("k1", "k2")                   // By keys
    .list();

// Query results
contacts.query().where(...).count();         // Long
contacts.query().where(...).exists();        // Boolean
contacts.query().where(...).first();         // Optional<T>
contacts.query().where(...).firstOrThrow();  // T or exception
contacts.query().where(...).list();          // List<T> (all pages)
contacts.query().where(...).listPage();      // List<T> (current page)
contacts.query().where(...).execute();       // Full SBXFindResponse<T>
```

### Annotations

| Annotation | Purpose |
|------------|---------|
| `@SbxModel("model_name")` | Maps class to SBX model name |
| `SbxEntity` interface | Provides `key()` and `meta()` for CRUD operations |

## Model Classes

The library uses Java records for immutable data classes:

```java
// Full repository pattern (recommended)
@SbxModel("contact")
@JsonNaming(SbxNamingStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Contact(
    String key,
    SBXMeta meta,
    String name,
    String email,
    String status
) implements SbxEntity {}

// Simple model (without repository, needs @JsonProperty)
public record Contact(
    @JsonProperty("_KEY") String key,
    @JsonProperty("_META") SBXMeta meta,
    String name,
    String email,
    String status
) {}
```

## Error Handling

```java
var response = sbx.find(query, Contact.class);
if (!response.success()) {
    String error = response.getErrorMessage();
    // Handle error
}
```

## Building from Source

```bash
mvn clean install
```

## Utilities

```java
import static com.sbxcloud.sbx.util.Sbx.*;

// Convert to JSON
String json = toJson(entity);
String pretty = toPrettyJson(entity);

// Parse JSON
Contact contact = fromJson(json, Contact.class);

// Convert to/from Map
Map<String, Object> map = toMap(entity);
Contact fromMap = fromMap(map, Contact.class);
```

## Design Philosophy

This library is a Java 21 translation of the [TypeScript SBX client](https://github.com/nicosoto0/sbx-lib-ts), adapted to use modern Java idioms:

| TypeScript | Java 21 |
|------------|---------|
| Generic functions | Generic methods with type inference |
| Interfaces | Records + sealed interfaces |
| `async/await` | Synchronous (Spring WebClient compatible) |
| Dynamic typing | `@SbxModel` annotations + `SbxEntity` interface |
| Object spread | Record `with` patterns / constructors |
| Optional chaining | `Optional<T>` |

The repository pattern provides Spring Data-like ergonomics while the underlying `SBXService` remains available for advanced use cases.

## License

ISC
