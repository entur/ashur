resource "google_logging_metric" "filtering_failures" {
  name    = "ashur/filtering_failures"
  project = var.ashur_project

  # Scope the metric to the custom log bucket where Ashur's k8s logs are
  # routed. Without this, the metric reads from _Default and never matches.
  bucket_name = "projects/${var.ashur_project}/locations/${var.log_bucket_location}/buckets/${var.log_bucket_name}"

  description = "Counts NeTEx filtering runs that ended with status FAILED in Ashur."

  filter = <<-EOT
    resource.type="k8s_container"
    resource.labels.namespace_name="ashur"
    jsonPayload.message:"Publishing processing status FAILED"
  EOT

  metric_descriptor {
    metric_kind  = "DELTA"
    value_type   = "INT64"
    unit         = "1"
    display_name = "Ashur filtering failures"
  }
}

data "google_monitoring_notification_channel" "slack_alerts" {
  count = var.enable_slack_notifications ? 1 : 0

  project      = var.ashur_project
  display_name = "Ashur Slack Alert"
  type         = "slack"
}

resource "google_monitoring_alert_policy" "filtering_failures" {
  project      = var.ashur_project
  display_name = "Ashur filtering failures"
  combiner     = "OR"
  user_labels  = var.labels

  conditions {
    display_name = "Failed filtering runs in the last 10 minutes"
    condition_threshold {
      filter          = "metric.type=\"logging.googleapis.com/user/${google_logging_metric.filtering_failures.name}\" resource.type=\"logging_bucket\""
      comparison      = "COMPARISON_GT"
      threshold_value = 0
      duration        = "0s"
      aggregations {
        alignment_period   = "600s"
        per_series_aligner = "ALIGN_SUM"
      }
      trigger {
        count = 1
      }
    }
  }

  notification_channels = data.google_monitoring_notification_channel.slack_alerts[*].name

  alert_strategy {
    auto_close = "28800s"
  }
}
