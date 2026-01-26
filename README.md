# SBX Java Client Library

Java 21+ client for SBX Cloud APIs. Spring Boot 3.x compatible.

## Installation

### Maven (JitPack)

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
    <version>v0.0.11</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.socobox:sbxcloud-lib-java:v0.0.11'
}
```

---

## Quick Start

### 1. Define Your Entity

```java
import com.sbxcloud.sbx.annotation.SbxModel;
import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.model.SBXMeta;

@SbxModel("contact")  // Maps to SBX model name - handles all Jackson config
public record Contact(
    String key,        // Auto-mapped to _KEY
    SBXMeta meta,      // Auto-mapped to _META
    String name,
    String email,
    String status
) implements SbxEntity {

    // Constructor for creating new records (without key/meta)
    public Contact(String name, String email, String status) {
        this(null, null, name, email, status);
    }
}
```

### 2. Initialize the Service

```java
// From environment variables (SBX_APP_KEY, SBX_TOKEN, SBX_DOMAIN, SBX_BASE_URL)
var sbx = SBXServiceFactory.withEnv();

// Or with explicit config
var sbx = SBXServiceFactory.builder()
    .appKey("your-app-key")
    .token("your-token")
    .domain(96)
    .baseUrl("https://sbxcloud.com")
    .build();
```

### 3. Use the Repository

```java
// Get typed repository
SbxRepository<Contact> contacts = sbx.repository(Contact.class);

// Create (meta is ignored, null values are ignored)
var contact = new Contact("John Doe", "john@example.com", "ACTIVE");
String key = contacts.save(contact);

// Read
Optional<Contact> found = contacts.findById(key);
List<Contact> all = contacts.findAll();

// Update - only _KEY and changed fields needed (partial update)
var updated = new Contact(key, null, "John Updated", null, null);
contacts.save(updated);  // Only updates name, other fields unchanged

// Delete
contacts.deleteById(key);
```

**Important:**
- `meta` is read-only and always stripped before create/update
- `null` values are stripped, enabling partial updates
- For updates, only `key` + changed fields are required

---

## Repository API

### CRUD Operations

```java
SbxRepository<Contact> repo = sbx.repository(Contact.class);

// Find
repo.findById("key")              // Optional<Contact>
repo.findByIds("k1", "k2")        // List<Contact>
repo.findAll()                    // List<Contact>
repo.findAll(page, pageSize)      // SBXFindResponse<Contact>
repo.existsById("key")            // boolean
repo.count()                      // long

// Save (auto insert/update based on key presence)
repo.save(entity)                 // String (returns key)
repo.saveAll(e1, e2, e3)          // List<String>

// Delete
repo.delete(entity)               // void
repo.deleteById("key")            // void
repo.deleteByIds("k1", "k2")      // void
repo.deleteAll(e1, e2)            // void
```

### Query Builder

```java
// Fluent queries
repo.query()
    .where(q -> q
        .andWhereIsEqualTo("status", "ACTIVE")
        .andWhereIsGreaterThan("age", 18)
        .andWhereContains("name", "John"))
    .fetch("account", "department")
    .page(1, 50)
    .list();

// Query results
repo.query().where(...).list()           // List<T> - all pages
repo.query().where(...).listPage()       // List<T> - current page only
repo.query().where(...).first()          // Optional<T>
repo.query().where(...).firstOrThrow()   // T or exception
repo.query().where(...).count()          // long
repo.query().where(...).exists()         // boolean
repo.query().where(...).execute()        // SBXFindResponse<T>

// Shortcuts
repo.findWhere(q -> q.andWhereIsEqualTo("status", "ACTIVE"))
repo.query().whereEquals("status", "ACTIVE").first()
repo.query().whereKeys("k1", "k2").list()
```

### Where Conditions

```java
// AND conditions
.andWhereIsEqualTo(field, value)
.andWhereIsNotEqualTo(field, value)
.andWhereIsGreaterThan(field, value)
.andWhereIsGreaterOrEqualTo(field, value)
.andWhereIsLessThan(field, value)
.andWhereIsLessOrEqualTo(field, value)
.andWhereContains(field, "text")      // LIKE %text%
.andWhereStartsWith(field, "text")    // LIKE text%
.andWhereEndsWith(field, "text")      // LIKE %text
.andWhereIsIn(field, "a", "b", "c")
.andWhereIsNotIn(field, "a", "b")
.andWhereIsNull(field)
.andWhereIsNotNull(field)

// OR conditions (same methods with "or" prefix)
.orWhereIsEqualTo(field, value)
// ...

// Groups
.newGroupWithAnd()   // Start AND group
.newGroupWithOr()    // Start OR group

// By keys
.whereWithKeys("key1", "key2")
```

---

## Entity Definition

### Minimal (Recommended)

```java
@SbxModel("contact")
public record Contact(
    String key,          // Primary key (_KEY) - null for new records
    SBXMeta meta,        // Read-only metadata (_META) - always null for create/update
    String name,
    String email
) implements SbxEntity {}
```

The `@SbxModel` annotation automatically:
- Sets the SBX model name for queries
- Maps `key` → `_KEY` in JSON
- Maps `meta` → `_META` in JSON (read-only, stripped on create/update)
- Ignores unknown JSON properties
- Strips null values (enables partial updates)

### With Convenience Constructor

```java
@SbxModel("contact")
public record Contact(
    String key,
    SBXMeta meta,
    String name,
    String email
) implements SbxEntity {

    // For creating new records
    public Contact(String name, String email) {
        this(null, null, name, email);
    }
}
```

### Without Repository (Manual Jackson)

```java
public record Contact(
    @JsonProperty("_KEY") String key,
    @JsonProperty("_META") SBXMeta meta,
    String name,
    String email
) {}
```

---

## Direct Service API

For advanced use cases, use `SBXService` directly:

```java
// Find
var query = FindQuery.from(Contact.class)
    .andWhereIsEqualTo("status", "ACTIVE")
    .setPageSize(50);
SBXFindResponse<Contact> response = sbx.find(query, Contact.class);

// Create/Update (with Map)
sbx.create("contact", Map.of("name", "John", "email", "john@example.com"));
sbx.update("contact", Map.of("_KEY", "existing-key", "name", "Updated"));

// Delete
sbx.delete("contact", "key-to-delete");
sbx.delete("contact", List.of("k1", "k2", "k3"));
```

---

## Utilities

```java
import static com.sbxcloud.sbx.util.Sbx.*;

// JSON conversion
String json = toJson(entity);
String pretty = toPrettyJson(entity);
Contact contact = fromJson(json, Contact.class);

// Map conversion
Map<String, Object> map = toMap(entity);
Contact fromMap = fromMap(map, Contact.class);
```

---

## Spring Boot Integration

### Configuration

```properties
# application.properties
sbx.app-key=your-app-key-uuid
sbx.token=your-bearer-token
sbx.domain=96
sbx.base-url=https://sbxcloud.com
sbx.debug=false
```

### Usage

```java
@Service
public class ContactService {
    private final SbxRepository<Contact> contacts;

    public ContactService(SBXService sbx) {
        this.contacts = sbx.repository(Contact.class);
    }

    public List<Contact> findActive() {
        return contacts.findWhere(q -> q.andWhereIsEqualTo("status", "ACTIVE"));
    }

    public Contact create(String name, String email) {
        var contact = new Contact(name, email, "ACTIVE");
        String key = contacts.save(contact);
        return contacts.findById(key).orElseThrow();
    }
}
```

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `SBX_APP_KEY` | Application key (UUID) |
| `SBX_TOKEN` | Bearer token |
| `SBX_DOMAIN` | Domain ID (integer) |
| `SBX_BASE_URL` | Base URL (e.g., `https://sbxcloud.com`) |

---

## Additional Features

### Authentication

```java
sbx.login("user@example.com", "password");
sbx.validateSession();
sbx.changePassword("current", "new", userId);
```

### Files

```java
sbx.uploadFile("doc.pdf", base64Content, folderKey);
byte[] content = sbx.downloadFile("file-key");
sbx.deleteFile("file-key");
```

### Folders

```java
sbx.createFolder("New Folder", parentKey);
sbx.listFolder(folderKey);
sbx.renameFolder(key, "New Name");
sbx.deleteFolder(key);
```

### Email

```java
var email = EmailParams.builder()
    .to("recipient@example.com")
    .subject("Hello")
    .templateKey("template-key")
    .data(Map.of("name", "John"))
    .build();
sbx.sendEmail(email);
```

### Cloud Scripts

```java
var result = sbx.runCloudScript("script-key", Map.of("param", "value"), ResultType.class);
```

---

## License

ISC
