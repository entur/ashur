# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ashur is a NeTEx dataset filtering and normalization service for Entur. It listens to Google Pub/Sub for messages indicating new NeTEx datasets are available, processes them using the [netex-tools](https://github.com/entur/netex-tools) library, and uploads filtered results to Google Cloud Storage.

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
2. **Redelivery Guard** → `RedeliveryGuardProcessor` runs before any status is published: skip (ack) an already-completed request, bounce (nack) one another pod holds, otherwise claim it and continue
3. **Camel Route** → `NetexFilterMessageProcessor` extracts message attributes (codespace, correlationId, filterProfile)
4. **Message Handler** → `NetexFilterMessageHandler` resolves filter config and delegates to FilterService
5. **Filtering** → `FilterService` downloads from Marduk bucket, filters via netex-tools, uploads to Ashur bucket
6. **Status Updates** → Published to `FilterNetexFileStatusQueue` (STARTED, SUCCESS, FAILED)
7. **Claim Completion** → `RedeliveryGuardCompletionProcessor` marks the claim completed, as the last step after SUCCESS is published

### Key Components

- **`camel/`** - Apache Camel route builders for PubSub message processing
  - `NetexFilterRouteBuilder` - Main entry point, defines the processing route
  - `BaseRouteBuilder` - Exception handling and status update routes
  - `NetexFilterMessageProcessor` - Extracts PubSub message and delegates to handler
  - `RedeliveryGuardProcessor` / `RedeliveryGuardCompletionProcessor` - Translate guard decisions into route behaviour and mark the claim completed

- **`filter/`** - Filtering configuration and execution
  - `FilterService` - Core filtering logic: download → unzip → filter → zip → upload
  - `FilterConfigResolver` - Resolves filter profile to netex-tools FilterConfig
  - `StandardImportFilteringProfileConfig` - Default filtering with date-based selection and element pruning
  - `AsIsImportFilteringProfileConfig` - Minimal filtering, preserves most data
  - `IncludeBlocksAndRestrictedJourneysFilteringProfileConfig` - Keeps VehicleScheduleFrame/blocks and preserves private data

- **`sax/`** - Custom SAX-based XML handlers and plugins for netex-tools
  - `plugins/activedates/` - Collects date-related data to filter ServiceJourneys by active dates
  - `plugins/filenames/` - Renames NeTEx files based on codespace conventions
  - `selectors/` - Entity and reference selectors for filtering decisions
  - `handlers/` - XML element handlers for CompositeFrame, ValidBetween, QuayRef, etc.

- **`file/`** - GCS bucket services
  - `MardukBucketService` - Reads input NeTEx files from Marduk exchange bucket
  - `AshurBucketService` - Writes filtered output to Ashur internal and exchange buckets
  - `ClaimStore` / `GcsClaimStore` / `InMemoryClaimStore` - Redelivery-guard claim leases; GCS generation preconditions in the `gcp` profile, an in-memory map in `local`/`test`

- **`pubsub/`** - PubSub message handling
  - `NetexFilterMessageHandler` - Parses message attributes and orchestrates filtering
  - `RedeliveryGuard` - Decides SKIP / PROCESS / BOUNCE for each delivery (see Redelivery Guard below)

### Filter Profiles

Three profiles defined in `FilterProfile` enum:
- **StandardImportFilter** - Filters by active dates (±2 days to +1 year), removes VehicleScheduleFrame, DeadRun, SiteFrame, prunes unreferenced entities
- **AsIsImportFilter** - Passes through with minimal transformation
- **IncludeBlocksAndRestrictedJourneysFilter** - Like the standard filter but keeps VehicleScheduleFrame/blocks and retains private data

A single import run reuses one correlationId across several profiles, so **codespace + correlationId + filterProfile** is what identifies a unit of work. That triple keys the output path, the local working directories, and the redelivery guard's claim.

### Redelivery Guard

Pub/Sub is at-least-once, so the same filter request can be delivered more than once. `RedeliveryGuard` runs before any status is published and claims `claims/{codespace}/{correlationId}/{filterProfile}` in the Ashur internal bucket, using GCS object generation preconditions so only one pod can win:

- no claim → claim it and process
- claim marked `completed` → skip, ack, publish nothing
- claim fresh and not completed (another pod is working on it) → bounce, i.e. nack for later redelivery, never a FAILED
- claim stale (older than `ttl-seconds`) and not completed → the holder is presumed dead, so take it over and process

`completed` is set only by `RedeliveryGuardCompletionProcessor` after upload, exchange-bucket copy, and the SUCCESS publish have all happened — never inferred from a side effect such as the output object existing, which would make a crash mid-pipeline look identical to a finished run. Both claim I/O on evaluation and the completion write are fail-open: the guard is a safety net, never a correctness gate, so a GCS error degrades to the pre-guard behaviour rather than failing a run. Outcomes are counted on `ashur_filter_guard_total{outcome,codespace}`.

### Configuration

Application properties prefixed with `ashur.*`:
- `ashur.netex.input-path` / `output-path` - Local directories for processing
- `ashur.gcp.ashur-bucket-name` / `marduk-bucket-name` - GCS bucket names
- `ashur.gcp.ashur-project-id` / `marduk-project-id` - GCP project IDs
- `ashur.redelivery-guard.enabled` - When false the redelivery guard is a complete no-op
- `ashur.redelivery-guard.ttl-seconds` - Age at which an uncompleted claim is treated as abandoned and may be taken over. Must exceed the worst-case legitimate run duration, or a slow-but-alive run gets taken over mid-flight

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
