# Ashur

Ashur performs a filtering job of NeTEx datasets, using a set of rules currently defined in the [netex-tools](https://github.com/entur/netex-tools) repository.

This component is still a work in progress and is not yet used in any environments.

## Running Ashur locally

A minimal local setup requires a Google PubSub emulator, and a built version of netex-tools in your local Maven repository.

Once the emulator is running, you can start Ashur by running the main method of Main.kt. You should pass the path to your
`application.properties` file as a VM argument, like this:

```
-Dconfig.file=/path/to/your/application.properties
```

Sample of `application.properties` file:
```properties
project.id=test
subscription.id=FilterNetexFileQueue
emulator.host=localhost:8085

input.path=netex-data/input
output.path=netex-data/output

cleanup.enabled=false
file.service.type=local
pubsub.service.type=emulator
gcp.bucket.name=
```

### Google PubSub emulator

See [Google PubSub emulator documentation](https://cloud.google.com/pubsub/docs/emulator) for details on how to install the Google PubSub emulator.  
The emulator is started with the following command:
```
gcloud beta emulators pubsub start
```
and will listen on port 8085 by default.

For the local pubsub emulator to be used when running this app locally, you need to set this property in your `application.properties` file:

```properties
pubsub.service.type=emulator
```

### netex-tools

The netex-tools repository must be built and installed in your local Maven repository.
This can be done with the following command in your local netex-tools repository:
```
mvn clean install -DskipTests
```
