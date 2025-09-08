import org.apache.camel.CamelContext
import org.apache.camel.ConsumerTemplate
import org.apache.camel.ProducerTemplate
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.entur.ror.ashur.AshurApplication
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getPathOfFilteredFile
import org.entur.ror.ashur.getStatus
import org.entur.ror.ashur.toPubsubMessage
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PubSubEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.assertEquals

@Testcontainers
@CamelSpringBootTest
@SpringBootTest(classes = [AshurApplication::class])
class NetexFilterRouteBuilderIntegrationTest {

    @Autowired
    lateinit var producerTemplate: ProducerTemplate

    @Autowired
    lateinit var consumerTemplate: ConsumerTemplate

    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var context: CamelContext

    private val testCorrelationId = "test-correlation-id"
    private val testCodespace = "test-codespace"
    private val testSource = "test-source"
    private val testFilteringProfile = "StandardImportFilter"

    companion object {
        @Container
        val pubsubEmulator = PubSubEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:emulators")
        )

        @JvmStatic
        @DynamicPropertySource
        fun registerPubSubProperties(registry: DynamicPropertyRegistry) {
            pubsubEmulator.start()
            registry.add(
                "spring.cloud.gcp.pubsub.emulator-host",
                { pubsubEmulator.emulatorEndpoint }
            )
            registry.add(
                "camel.component.google-pubsub.endpoint",
                { pubsubEmulator.emulatorEndpoint }
            )
        }
    }

    fun sendFilterMessageToPubsub(netexFilePath: String) {
        val ashurProjectId = appConfig.gcp.ashurProjectId
        producerTemplate.requestBodyAndHeader(
            "google-pubsub:$ashurProjectId:${Constants.FILTER_NETEX_FILE_SUBSCRIPTION}",
            "",
            "CamelGooglePubsubAttributes",
            mapOf(
                "EnturDatasetReferential" to testCodespace,
                "RutebankenCorrelationId" to testCorrelationId,
                "EnturFilteringProfile" to testFilteringProfile,
                "RutebankenTargetFileHandle" to netexFilePath,
                "NetexSource" to testSource,
            )
        )
    }

    fun pathOfFilteredFile(fileName: String) = "${testCodespace}/${testCorrelationId}/${testSource}/filtered_${fileName}"

    @Test
    fun `test filter route processes message successfully`() {
        val mardukProjectId = appConfig.gcp.mardukProjectId

        sendFilterMessageToPubsub(netexFilePath = "src/test/resources/testfile.zip")

        val startedMessage = consumerTemplate.receive(
            "google-pubsub:$mardukProjectId:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true",
            5000
        )

        val successMessage = consumerTemplate.receive(
            "google-pubsub:$mardukProjectId:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true",
            10000
        )

        assertEquals(testCorrelationId, startedMessage.toPubsubMessage().getCorrelationId())
        assertEquals(testCorrelationId, successMessage.toPubsubMessage().getCorrelationId())

        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_STARTED, startedMessage.toPubsubMessage().getStatus())
        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED, successMessage.toPubsubMessage().getStatus())

        val pathOfFilteredFile = pathOfFilteredFile("testfile.zip")
        assertEquals(pathOfFilteredFile, successMessage.toPubsubMessage().getPathOfFilteredFile())
    }

    @Test
    fun `test filter route processes message but fails because file does not exist`() {
        val mardukProjectId = appConfig.gcp.mardukProjectId

        sendFilterMessageToPubsub(netexFilePath = "unknown-file.zip")

        val startedMessage = consumerTemplate.receive(
            "google-pubsub:$mardukProjectId:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true",
            5000
        )

        val failedMessage = consumerTemplate.receive(
            "google-pubsub:$mardukProjectId:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true",
            10000
        )

        assertEquals(testCorrelationId, startedMessage.toPubsubMessage().getCorrelationId())
        assertEquals(testCorrelationId, failedMessage.toPubsubMessage().getCorrelationId())

        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_STARTED, startedMessage.toPubsubMessage().getStatus())
        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_FAILED, failedMessage.toPubsubMessage().getStatus())
    }
}
