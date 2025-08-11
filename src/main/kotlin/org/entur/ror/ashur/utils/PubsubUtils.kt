package org.entur.ror.ashur.utils

import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings
import com.google.pubsub.v1.ProjectSubscriptionName
import io.grpc.ManagedChannelBuilder

fun createSubscriptionAdminClientForEmulator(emulatorHost: String): SubscriptionAdminClient {
    val channel = ManagedChannelBuilder.forTarget(emulatorHost)
        .usePlaintext()
        .build()

    val channelProvider = FixedTransportChannelProvider.create(
        GrpcTransportChannel.create(channel)
    )

    val settings = SubscriptionAdminSettings.newBuilder()
        .setTransportChannelProvider(channelProvider)
        .setCredentialsProvider(NoCredentialsProvider.create())
        .build()

    return SubscriptionAdminClient.create(settings)
}

fun subscriptionExists(projectId: String, subscriptionId: String, emulatorHost: String?): Boolean {
    return try {
        val client = if (emulatorHost != null) {
            createSubscriptionAdminClientForEmulator(emulatorHost)
        } else {
            SubscriptionAdminClient.create()
        }
        client.use {
            val name = ProjectSubscriptionName.of(projectId, subscriptionId)
            it.getSubscription(name) // This will throw an exception if the subscription does not exist
            true
        }
    } catch (_: Exception) {
        false
    }
}
