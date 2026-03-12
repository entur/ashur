# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build the project
mvn clean package

# Run all tests (unit + integration)
mvn verify

# Run unit tests only
mvn test

# Run a single test class
mvn test -Dtest=FilterServiceTest

# Run a single test method
mvn test -Dtest=FilterServiceTest#testFilterWithStandardProfile
```

## Local Development Setup

1. **Start Google Pub/Sub emulator** (required):
   ```bash
   gcloud beta emulators pubsub start
   ```
   Default port: 8085

2. **Run the application** with VM arguments pointing to config files:
   ```
   -Dspring.config.location=/path/to/application.properties -Dlogging.config=/path/to/logback.xml
   ```

3. **Required config files**: See README.md for sample `application.properties` and `logback.xml` templates.

## Architecture Overview

Ashur is a NeTEx dataset filtering service that:
- Listens to Google Pub/Sub for new dataset notifications
- Downloads NeTEx datasets from Marduk GCS bucket
- Applies filtering rules using netex-tools library
- Uploads filtered results to Ashur GCS bucket
- Notifies downstream systems via Pub/Sub

### Key Components

**Message Processing (Apache Camel)**
- `NetexFilterRouteBuilder` - Main Camel route handling Pub/Sub subscription
- `NetexFilterMessageHandler` - Extracts message attributes and delegates filtering
- MDC processors manage correlation IDs and codespace context for logging

**Filtering Pipeline**
- `FilterService` - Orchestrates download → unzip → filter → upload flow
- `FilterConfigResolver` - Resolves filter config and validates codespace authorization
- `FilterProfile` enum: `StandardImportFilter`, `AsIsImportFilter`, `IncludeBlocksAndRestrictedJourneysFilter`

**SAX Plugins** (`sax/plugins/`)
- `ActiveDatesPlugin` - Date-range filtering with handlers for calendars, day types, operating periods
- `FileNamePlugin` - NeTEx filename metadata management
- Entity selectors in `sax/selectors/` - Fine-grained filtering for blocks, stop assignments, interchanges

**Cloud Storage** (`file/`)
- `AshurBucketService` / `MardukBucketService` - GCS bucket operations
- `AbstractBlobStoreService` - Base class supporting both GCS and local filesystem

### Domain Concepts

- **NeTEx**: European standard for public transport data exchange
- **Codespace**: Namespace identifier for transport operators (e.g., "RUT", "NSB")
- **Filter Profile**: Determines which data elements to include/exclude; some profiles are restricted to specific codespaces
- **Block**: Vehicle scheduling unit linking service journeys to physical vehicles

### Configuration

Spring `@ConfigurationProperties` in `AppConfig.kt`:
- `ashur.netex.*` - Input/output paths, cleanup settings
- `ashur.gcp.*` - GCP project IDs and bucket names
- `ashur.local.*` - Local development blobstore path
- `ashur.profile-security.*` - Codespace allowlists for restricted filter profiles
