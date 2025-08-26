resource "google_pubsub_topic" "FilterNetexFileQueue" {
  name    = "FilterNetexFileQueue"
  project = var.gcp_resources_project
  labels  = var.labels
}

resource "google_pubsub_subscription" "FilterNetexFileQueue" {
  name                       = "FilterNetexFileQueue"
  topic                      = google_pubsub_topic.FilterNetexFileQueue.name
  project                    = var.gcp_resources_project
  labels                     = var.labels
  message_retention_duration = "3600s"
  ack_deadline_seconds       = 600
  expiration_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }
}

resource "google_pubsub_topic" "FilterNetexFileStatusQueue" {
  name    = "FilterNetexFileStatusQueue"
  project = var.gcp_resources_project
  labels  = var.labels
}

resource "google_pubsub_subscription" "FilterNetexFileStatusQueue" {
  name                       = "FilterNetexFileStatusQueue"
  topic                      = google_pubsub_topic.FilterNetexFileStatusQueue.name
  project                    = var.gcp_resources_project
  labels                     = var.labels
  message_retention_duration = "3600s"
  ack_deadline_seconds       = 30
  expiration_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }
}
