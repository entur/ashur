package org.entur.ror.ashur.pubsub

import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider
import com.google.api.gax.rpc.NotFoundException
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PushConfig
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.CamelConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Profile("local")
@Component
class PubSubInitializer(
    private val appConfig: AppConfig,
    private val camelConfig: CamelConfig,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun createChannelProvider(): InstantiatingGrpcChannelProvider =
        InstantiatingGrpcChannelProvider.newBuilder()
            .setChannelConfigurator { b -> b.usePlaintext() }
            .setEndpoint(camelConfig.googlePubsub.endpoint)
            .build()

    private fun topicAdminSettings(): TopicAdminSettings =
        TopicAdminSettings.newBuilder()
            .setTransportChannelProvider(createChannelProvider())
            .setCredentialsProvider { null }
            .build()

    private fun subscriptionAdminSettings(): SubscriptionAdminSettings =
        SubscriptionAdminSettings.newBuilder()
            .setTransportChannelProvider(createChannelProvider())
            .setCredentialsProvider { null }
            .build()

    private fun createTopicAdminClient(settings: TopicAdminSettings = topicAdminSettings()): TopicAdminClient =
        TopicAdminClient.create(settings)

    private fun createSubscriptionAdminClient(settings: SubscriptionAdminSettings = subscriptionAdminSettings()): SubscriptionAdminClient =
        SubscriptionAdminClient.create(settings)

    @PostConstruct
    fun init() {
        val projectId = appConfig.pubsub.projectId
        val topicId = appConfig.pubsub.subscription
        val subscriptionId = appConfig.pubsub.subscription

        val topicClient = createTopicAdminClient()
        val topicName = ProjectTopicName.of(projectId, topicId)

        topicClient.use { client ->
            try {
                logger.info("Checking if topic $topicId exists in project $projectId")
                client.getTopic(topicName)
            } catch (_: NotFoundException) {
                logger.info("Creating topic $topicId in project $projectId...")
                client.createTopic(topicName)
                logger.info("Done creating topic $topicId in project $projectId")
            } catch (e: Exception) {
                logger.error("Failed to check or create topic $topicId in project $projectId", e)
                throw e
            }
        }

        val subscriptionClient = createSubscriptionAdminClient()
        val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)

        subscriptionClient.use { client ->
            try {
                logger.info("Checking if subscription $subscriptionName exists in project $projectId")
                client.getSubscription(subscriptionName)
            } catch (_: NotFoundException) {
                logger.info("Creating subscription $subscriptionName in project $projectId...")
                client.createSubscription(subscriptionName, topicName, PushConfig.getDefaultInstance(), 10)
                logger.info("Done creating subscription $subscriptionName in project $projectId")
            } catch (e: Exception) {
                logger.error("Failed to check or create subscription $subscriptionName in project $projectId", e)
                throw e
            }
        }
    }
}
