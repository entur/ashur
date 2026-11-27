resource "google_storage_bucket_iam_member" "ashur_exchange_storage_iam_member" {
  bucket = var.ashur_exchange_storage_bucket
  role   = var.marduk_service_account_exchange_bucket_role
  member = var.marduk_service_account
}

# marduk reads ashur's filtering results from this subscription. The subscription is in ashur's project,
# so marduk needs an explicit grant. It replaces marduk's team-wide folder rule
# (tf-gcp-iam application_rules/ror-marduk-<env>.yaml), so marduk keeps this access when that rule is removed.
resource "google_pubsub_subscription_iam_member" "marduk_filter_status_subscriber" {
  project      = var.ashur_project
  subscription = google_pubsub_subscription.FilterNetexFileStatusQueue.name
  role         = "roles/pubsub.subscriber"
  member       = var.marduk_service_account
}
