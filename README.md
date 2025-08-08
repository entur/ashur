# Ashur

Ashur performs a filtering job of NeTEx datasets, using a set of rules currently defined in the [netex-tools](https://github.com/entur/netex-tools) repository.

This component is still a work in progress and is not yet used in any environments.

## Running Ashur locally

A minimal local setup requires a Google PubSub emulator, and a built version of netex-tools in your local Maven repository.

Once the emulator is running, you can start Ashur by running the main method of Main.kt. You should pass the paths to your
`application.properties` and `logback.xml` files as VM arguments, like this:

```
-Dconfig.file=/path/to/your/application.properties -Dlogging.config=/path/to/your/logback.xml
```

Sample of `application.properties` file:
```properties
ashur.pubsub.project.id=test
subscription.id=FilterNetexFileQueue

input.path=netex-data/input
output.path=netex-data/output

cleanup.enabled=false
file.service.type=local
gcp.bucket.name=

camel.component.google-pubsub.endpoint=localhost:8085
camel.component.google-pubsub.authenticate=false
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
```

### netex-tools

The netex-tools repository must be built and installed in your local Maven repository.
This can be done with the following command in your local netex-tools repository:
```
mvn clean install -DskipTests
```
