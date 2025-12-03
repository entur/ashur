resource "google_storage_bucket_iam_member" "ashur_exchange_storage_iam_member" {
  bucket = var.ashur_exchange_storage_bucket
  role   = var.marduk_service_account_exchange_bucket_role
  member = var.marduk_service_account
}