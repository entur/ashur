FROM bellsoft/liberica-openjdk-alpine:25.0.4@sha256:b728c08690506dac4ed7150ab40f406236d43f487c51a72f375e817f02d7cbde AS builder
WORKDIR /builder
# Upgrades all system packages to patch known CVEs
RUN apk update \
 && apk upgrade --no-cache \
 && rm -rf /var/cache/apk/*
COPY target/*-SNAPSHOT.jar application.jar
RUN java -Djarmode=tools  -jar application.jar extract --layers --destination extracted

FROM bellsoft/liberica-openjdk-alpine:25.0.4@sha256:b728c08690506dac4ed7150ab40f406236d43f487c51a72f375e817f02d7cbde
# Upgrades all system packages to patch known CVEs.
RUN apk update \
 && apk upgrade --no-cache \
 && apk add --no-cache tini \
 && rm -rf /var/cache/apk/* \
 && echo "fresh apk upgrade 2026-06-12"
WORKDIR /deployments
RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser
USER appuser
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
ENTRYPOINT [ "/sbin/tini", "--", "java", "-jar", "application.jar" ]
