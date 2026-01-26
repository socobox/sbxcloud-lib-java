# SBX Java Client Library

Java 21+ / Spring Boot 3.x client for SBX Cloud APIs.
Mirrors the TypeScript library at ../ts-client-lib/

## Git Rules
- NEVER attribute commits to Claude (no "Co-Authored-By: Claude" or "Generated with Claude Code")
- Keep authorship to the local GitHub user only
- Use conventional commits: `<type>(<scope>): <subject>`

## Quick Reference

### Build & Test
```bash
mvn clean test           # run tests
mvn clean package        # build JAR
mvn clean install        # install to local .m2
```

### Project Structure
```
src/main/java/com/sbxcloud/sbx/
├── client/       # SBXService, SBXServiceFactory
├── config/       # Spring Boot auto-configuration
├── exception/    # SBXException
├── model/        # Records: SBXResponse, SBXFindResponse, FindQuery types, etc.
└── query/        # FindQuery fluent builder
```

### Key Classes
- `SBXService` - main service (852 LOC), all API operations
- `SBXServiceFactory` - factory pattern with builder, env vars support
- `FindQuery` - fluent query builder with logical groups
- `SBXAutoConfiguration` - Spring Boot auto-config

### Publishing (JitPack)
1. Push to GitHub
2. Create a release tag (e.g., `v1.0.0`)
3. JitPack builds automatically at: https://jitpack.io/#OWNER/REPO

Users add:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependency>
    <groupId>com.github.OWNER</groupId>
    <artifactId>REPO</artifactId>
    <version>TAG</version>
</dependency>
```

### TypeScript Parity Checklist
All features from ts-client-lib implemented:
- [x] find(), findOne(), findAll() with auto-pagination
- [x] create(), update(), delete() with 100-item chunking
- [x] Authentication: login, validateSession, password ops
- [x] Files: upload, download, delete
- [x] Folders: create, list, rename, delete
- [x] Email: sendEmail, sendEmailV2
- [x] Cloud scripts: runCloudScript
- [x] Config: loadConfig, getConfig
- [x] Multi-domain support
- [x] FindQuery fluent builder with AND/OR groups

### Configuration
Spring Boot properties:
```properties
sbx.app-key=xxx
sbx.token=xxx
sbx.domain=0
sbx.base-url=https://sbxcloud.com
sbx.debug=false
```

Environment variables: SBX_APP_KEY, SBX_TOKEN, SBX_DOMAIN, SBX_BASE_URL

### Java 21+ Features Used
- Records for all model classes (immutable DTOs)
- Pattern matching (instanceof)
- Switch expressions
- var keyword
- List.of(), Map.of()

### Dependencies
- Spring Web (RestClient)
- Jackson (JSON serialization)
- SLF4J (logging API only)
- Optional: spring-boot-autoconfigure
