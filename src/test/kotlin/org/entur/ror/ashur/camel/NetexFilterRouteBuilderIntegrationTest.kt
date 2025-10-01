import org.apache.camel.CamelContext
import org.apache.camel.ConsumerTemplate
import org.apache.camel.ProducerTemplate
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.entur.ror.ashur.AshurApplication
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.PubSubEmulatorTestBase
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getPathOfFilteredFile
import org.entur.ror.ashur.getStatus
import org.entur.ror.ashur.toPubsubMessage
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import kotlin.test.assertEquals

@Testcontainers
@CamelSpringBootTest
@SpringBootTest(classes = [AshurApplication::class])
class NetexFilterRouteBuilderIntegrationTest: PubSubEmulatorTestBase() {
    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var producerTemplate: ProducerTemplate

    @Autowired
    lateinit var consumerTemplate: ConsumerTemplate

    @Autowired
    lateinit var context: CamelContext

    private val testCorrelationId = "test-correlation-id"
    private val testCodespace = "test-codespace"
    private val testSource = "test-source"
    private val testFilteringProfile = "StandardImportFilter"

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

    fun copyTestZipFileToMardukTestBucket() {
        val resource = this::class.java.getResource("testfile.zip")  ?: throw IllegalArgumentException("Test zip file was not found on classpath")
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.mardukBucketName}")
        if (!target.exists()) {
            target.mkdirs()
        }
        val targetFile = File(target, "testfile.zip")
        if (!targetFile.exists()) {
            targetFile.createNewFile()
        }
        resource.openStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun cleanupTestZipFiles() {
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.mardukBucketName}")
        if (target.exists()) {
            target.deleteRecursively()
        }
    }

    @Test
    fun `test filter route processes message successfully`() {
        copyTestZipFileToMardukTestBucket()
        val mardukProjectId = appConfig.gcp.mardukProjectId

        sendFilterMessageToPubsub(netexFilePath = "testfile.zip")
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

        cleanupTestZipFiles()
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
