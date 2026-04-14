# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ashur is a NeTEx dataset filtering and normalization service for Entur. It listens to Google Pub/Sub for messages indicating new NeTEx datasets are available, processes them using the [netex-tools](https://github.com/entur/netex-tools) library, and uploads filtered results to Google Cloud Storage.

## Build & Test Commands

```bash
# Build the project
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=FilterServiceTest

# Run a single test method
mvn test -Dtest=FilterServiceTest#hasNoJourneysInFilteredDataset

# Skip tests during build
mvn package -DskipTests
```

## Running Locally

Requires Google PubSub emulator running on port 8085:
```bash
gcloud beta emulators pubsub start
```

Run Main.kt with VM arguments pointing to local config files:
```
-Dspring.config.location=/path/to/application.properties -Dlogging.config=/path/to/logback.xml
```

## Architecture

### Message Flow

1. **PubSub Subscription** → `NetexFilterRouteBuilder` receives messages from `FilterNetexFileQueue`
2. **Camel Route** → `NetexFilterMessageProcessor` extracts message attributes (codespace, correlationId, filterProfile)
3. **Message Handler** → `NetexFilterMessageHandler` resolves filter config and delegates to FilterService
4. **Filtering** → `FilterService` downloads from Marduk bucket, filters via netex-tools, uploads to Ashur bucket
5. **Status Updates** → Published to `FilterNetexFileStatusQueue` (STARTED, SUCCESS, FAILED)

### Key Components

- **`camel/`** - Apache Camel route builders for PubSub message processing
  - `NetexFilterRouteBuilder` - Main entry point, defines the processing route
  - `BaseRouteBuilder` - Exception handling and status update routes
  - `NetexFilterMessageProcessor` - Extracts PubSub message and delegates to handler

- **`filter/`** - Filtering configuration and execution
  - `FilterService` - Core filtering logic: download → unzip → filter → zip → upload
  - `FilterConfigResolver` - Resolves filter profile to netex-tools FilterConfig
  - `StandardImportFilteringProfileConfig` - Default filtering with date-based selection and element pruning
  - `AsIsImportFilteringProfileConfig` - Minimal filtering, preserves most data

- **`sax/`** - Custom SAX-based XML handlers and plugins for netex-tools
  - `plugins/activedates/` - Collects date-related data to filter ServiceJourneys by active dates
  - `plugins/filenames/` - Renames NeTEx files based on codespace conventions
  - `selectors/` - Entity and reference selectors for filtering decisions
  - `handlers/` - XML element handlers for CompositeFrame, ValidBetween, QuayRef, etc.

- **`file/`** - GCS bucket services
  - `MardukBucketService` - Reads input NeTEx files from Marduk exchange bucket
  - `AshurBucketService` - Writes filtered output to Ashur internal and exchange buckets

- **`pubsub/`** - PubSub message handling
  - `NetexFilterMessageHandler` - Parses message attributes and orchestrates filtering

### Filter Profiles

Two profiles defined in `FilterProfile` enum:
- **StandardImportFilter** - Filters by active dates (±2 days to +1 year), removes VehicleScheduleFrame, DeadRun, SiteFrame, prunes unreferenced entities
- **AsIsImportFilter** - Passes through with minimal transformation

### Configuration

Application properties prefixed with `ashur.*`:
- `ashur.netex.input-path` / `output-path` - Local directories for processing
- `ashur.gcp.ashur-bucket-name` / `marduk-bucket-name` - GCS bucket names
- `ashur.gcp.ashur-project-id` / `marduk-project-id` - GCP project IDs

### Key Dependencies

- **netex-tools** (`org.entur.ror:netex-pipeline`, `netex-tools-lib`) - Core NeTEx filtering engine
- **Apache Camel** - Message routing and PubSub integration
- **Spring Boot** - Application framework
- **entur-helpers** - Entur's shared GCS and PubSub utilities

## PubSub Message Attributes

Messages consumed from `FilterNetexFileQueue` contain:
- `RutebankenTargetFileHandle` - Path to input NeTEx file in Marduk bucket
- `EnturDatasetReferential` - Codespace identifier
- `RutebankenCorrelationId` - Request correlation ID
- `EnturFilteringProfile` - Filter profile to apply
- `NetexSource` - Source system identifier
- `FileCreatedTimestamp` - Optional timestamp for CompositeFrame modification

Status messages published to `FilterNetexFileStatusQueue` include:
- `Status` - STARTED, SUCCESS, or FAILED
- `FilteredNetexFilePath` - Path to output file (on success)
- `FilteringErrorCode` - Error code (on failure)
