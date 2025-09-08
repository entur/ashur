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
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.CamelConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Profile("local", "test")
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
        val projectId = appConfig.gcp.ashurProjectId

        val topicClient = createTopicAdminClient()
        val topics = listOf<ProjectTopicName>(
            ProjectTopicName.of(projectId, Constants.FILTER_NETEX_FILE_SUBSCRIPTION),
            ProjectTopicName.of(projectId, Constants.FILTER_NETEX_FILE_STATUS_TOPIC)
        )

        for (topic in topics) {
            val topicId = topic.topic
            try {
                logger.info("Checking if topic $topicId exists in project $projectId")
                topicClient.getTopic(topic)
                logger.info("Detected existing topic $topicId in project $projectId")
            } catch (_: NotFoundException) {
                logger.info("Creating topic $topicId in project $projectId...")
                topicClient.createTopic(topic)
                logger.info("Done creating topic $topicId in project $projectId")
            } catch (e: Exception) {
                logger.error("Failed to check or create topic $topicId in project $projectId", e)
                throw e
            }
        }

        val subscriptionClient = createSubscriptionAdminClient()
        val subscriptions = listOf<ProjectSubscriptionName>(
            ProjectSubscriptionName.of(projectId, Constants.FILTER_NETEX_FILE_SUBSCRIPTION),
            ProjectSubscriptionName.of(projectId, Constants.FILTER_NETEX_FILE_STATUS_TOPIC)
        )

        for (subscription in subscriptions) {
            val topicName = ProjectTopicName.of(projectId, subscription.subscription)
            try {
                logger.info("Checking if subscription $subscription exists in project $projectId")
                subscriptionClient.getSubscription(subscription)
                logger.info("Detected existing subscription $subscription in project $projectId")
            } catch (_: NotFoundException) {
                logger.info("Creating subscription $subscription in project $projectId...")
                subscriptionClient.createSubscription(subscription, topicName, PushConfig.getDefaultInstance(), 600)
                logger.info("Done creating subscription $subscription in project $projectId")
            } catch (e: Exception) {
                logger.error("Failed to check or create subscription $subscription in project $projectId", e)
                throw e
            }
        }
    }
}
