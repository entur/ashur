resource "google_pubsub_subscription" "FilterNetexFileQueue" {
  name                       = "FilterNetexFileQueue"
  topic                      = "projects/${var.pubsub_topic_project}/topics/FilterNetexFileQueue"
  project                    = var.ashur_project
  labels                     = var.labels
  message_retention_duration = "3600s"
  ack_deadline_seconds       = 600
  retry_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }
}

resource "google_pubsub_subscription" "FilterNetexFileStatusQueue" {
  name                       = "FilterNetexFileStatusQueue"
  topic                      = "projects/${var.pubsub_topic_project}/topics/FilterNetexFileStatusQueue"
  project                    = var.ashur_project
  labels                     = var.labels
  message_retention_duration = "3600s"
  ack_deadline_seconds       = 600
  retry_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }
}
