resource "google_storage_bucket" "storage_bucket" {
  name                        = "ror-ashur-internal-gcp-${var.bucket_instance_suffix}"
  force_destroy               = var.force_destroy
  location                    = var.location
  project                     = var.gcs_bucket_project
  storage_class               = var.storage_class
  labels                      = var.labels
  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
  versioning {
    enabled = true
  }
  logging {
    log_bucket        = var.log_bucket
    log_object_prefix = "ror-ashur-internal-gcp-${var.bucket_instance_suffix}"
  }
  lifecycle_rule {
    condition {
      age        = var.bucket_retention_period
      with_state = "ANY"
    }
    action {
      type = "Delete"
    }
  }
  # Redelivery-guard claim leases (claims/{codespace}/{correlationId}) are never deleted by the app;
  # they only have to outlive the 7-day Pub/Sub message retention. Delete them at 8 days (a day of
  # margin) so they don't linger the full retention period. The blanket rule above already GCs them
  # eventually, so this is a faster-cleanup optimisation, not a correctness requirement.
  lifecycle_rule {
    condition {
      age            = 8
      matches_prefix = ["claims/"]
      with_state     = "ANY"
    }
    action {
      type = "Delete"
    }
  }
}

resource "google_storage_bucket" "exchange_bucket" {
  name                        = "ror-ashur-exchange-gcp-${var.bucket_instance_suffix}"
  force_destroy               = var.force_destroy
  location                    = var.location
  project                     = var.gcs_bucket_project
  storage_class               = var.storage_class
  labels                      = var.labels
  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
  versioning {
    enabled = true
  }
  logging {
    log_bucket        = var.log_bucket
    log_object_prefix = "ror-ashur-exchange-gcp-${var.bucket_instance_suffix}"
  }
  lifecycle_rule {
    condition {
      age        = var.bucket_retention_period
      with_state = "ANY"
    }
    action {
      type = "Delete"
    }
  }
}