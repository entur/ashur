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
    enabled = false
  }
  logging {
    log_bucket        = var.log_bucket
    log_object_prefix = "ror-ashur-internal-gcp-${var.bucket_instance_suffix}"
  }
}