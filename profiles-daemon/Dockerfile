# No alpine need glibc
FROM openjdk:11-jre-slim-bullseye
# replaced at build time..
LABEL name="cortex-actions-service" \
      vendor="CognitiveScale" \
      version=6 \
      summary="Service for management of profileById api." \
      description="Darmon for profile fetch"
WORKDIR /app
RUN apt update \
    && apt upgrade -y \
    && apt clean
COPY ./profiles-daemon/build/libs/profiles-*-SNAPSHOT.jar ./libs/profiles.jar
RUN echo "default:x:1001:0:Default Application User:/app:/sbin/nologin" >> /etc/passwd \
&& chown -R 1001 /app
USER default
EXPOSE 8080
CMD ["java","-jar","./libs/profiles.jar"]
