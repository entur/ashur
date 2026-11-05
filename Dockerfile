FROM bellsoft/liberica-openjdk-alpine:25.0.5.0.3-5@sha256:5f60a2c005b48298a9fa65fdaaa117a92a50e8da26891598e367eeb6a37ae4a0 AS builder
WORKDIR /builder
# Upgrades all system packages to patch known CVEs
RUN apk update \
 && apk upgrade --no-cache \
 && rm -rf /var/cache/apk/*
COPY target/*-SNAPSHOT.jar application.jar
RUN java -Djarmode=tools  -jar application.jar extract --layers --destination extracted

FROM bellsoft/liberica-openjdk-alpine:25.0.5.0.3-5@sha256:5f60a2c005b48298a9fa65fdaaa117a92a50e8da26891598e367eeb6a37ae4a0
# Upgrades all system packages to patch known CVEs.
RUN apk update \
 && apk upgrade --no-cache \
 && apk add --no-cache tini \
 && rm -rf /var/cache/apk/* \
 && echo "fresh apk upgrade 2026-10-28"
WORKDIR /deployments
RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser
USER appuser
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
ENTRYPOINT [ "/sbin/tini", "--", "java", "-jar", "application.jar" ]
