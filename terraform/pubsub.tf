resource "google_pubsub_subscription" "FilterNetexFileQueue" {
  name                       = "FilterNetexFileQueue"
  topic                      = "projects/${var.pubsub_topic_project}/topics/FilterNetexFileQueue"
  project                    = var.ashur_project
  labels                     = var.labels
  message_retention_duration = "3600s"
  ack_deadline_seconds       = 600
  expiration_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }
}

resource "google_pubsub_subscription_iam_member" "ashur_filter_subscription_iam" {
  project      = var.ashur_project
  member       = var.service_account
  role         = var.service_account_pubsub_subscriber_role
  subscription = google_pubsub_subscription.FilterNetexFileQueue.name
  depends_on   = [google_pubsub_subscription.FilterNetexFileQueue]
}

resource "google_pubsub_subscription" "FilterNetexFileStatusQueue" {
  name                       = "FilterNetexFileStatusQueue"
  topic                      = "projects/${var.pubsub_topic_project}/topics/FilterNetexFileStatusQueue"
  project                    = var.ashur_project
  labels                     = var.labels
  message_retention_duration = "3600s"
  ack_deadline_seconds       = 600
  expiration_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }
}

resource "google_pubsub_subscription_iam_member" "ashur_filter_status_subscription_iam" {
  project      = var.ashur_project
  member       = var.service_account
  role         = var.service_account_pubsub_publisher_role
  subscription = google_pubsub_subscription.FilterNetexFileStatusQueue.name
  depends_on   = [google_pubsub_subscription.FilterNetexFileStatusQueue]
}