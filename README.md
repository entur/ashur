# Ashur

Ashur performs a filtering job of NeTEx datasets, using a set of rules currently defined in the [netex-tools](https://github.com/entur/netex-tools) repository.

This component is still a work in progress and is not yet used in any environments.

## How it works

Ashur listens to a Google PubSub topic for messages indicating that a new NeTEx dataset is available in a Google Cloud Storage bucket.
When a message is received, Ashur downloads the dataset, processes it using netex-tools, and uploads the filtered dataset to its own Google Cloud Storage bucket.
Ashur then sends a message to another Google PubSub topic to notify that the filtered dataset is available.

## Running Ashur locally

A minimal local setup requires a Google PubSub emulator, and a built version of netex-tools in your local Maven repository.

Once the emulator is running, you can start Ashur by running the main method of Main.kt. You should pass the paths to your
`application.properties` and `logback.xml` files as VM arguments, like this:

```
-Dspring.config.location=/path/to/your/application.properties -Dlogging.config=/path/to/your/logback.xml
```

Sample of `application.properties` file:
```properties
ashur.netex.input-path=netex-data/input
ashur.netex.output-path=netex-data/output
ashur.netex.cleanup-enabled=false

ashur.gcp.ashur-project-id=test
ashur.gcp.marduk-project-id=test

ashur.local.ashur-bucket-path=/path/to/my/local/ashur/bucket
ashur.local.marduk-bucket-path=/path/to/my/local/marduk/bucket

camel.component.google-pubsub.endpoint=localhost:8085
camel.component.google-pubsub.authenticate=false
camel.component.google-pubsub.projectId=test

spring.profiles.active=local
management.endpoints.web.exposure.include=prometheus
```

Sample of `logback.xml` file:
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} %replace(%X{codespace}){'^(.+)$','[codespace=$1 '}%replace(%X{correlationId}){'^(.+)$','correlationId=$1] '}%msg%n
            </pattern>
        </encoder>
    </appender>
    <root level="trace">
        <appender-ref ref="STDOUT"/>
    </root>
    <logger name="io.grpc.netty" level="WARN"/>
    <logger name="io.grpc" level="WARN"/>
    <logger name="io.netty" level="WARN"/>
    <logger name="org.apache.camel" level="INFO" additivity="false">
        <appender-ref ref="STDOUT" />
    </logger>
</configuration>
```

### Google PubSub emulator

See [Google PubSub emulator documentation](https://cloud.google.com/pubsub/docs/emulator) for details on how to install the Google PubSub emulator.  
The emulator is started with the following command:
```
gcloud beta emulators pubsub start
```
and will listen on port 8085 by default.

For the local pubsub emulator to be used when running this app locally, you need to set these properties in your `application.properties` file:

```properties
camel.component.google-pubsub.endpoint=localhost:8085
camel.component.google-pubsub.authenticate=false
camel.component.google-pubsub.project-id=test
```
