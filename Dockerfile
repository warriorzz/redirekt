FROM eclipse-temurin:17 as builder

RUN apt update && apt install wget -y

COPY . .
RUN curl -S https://raw.githubusercontent.com/sindresorhus/github-markdown-css/main/github-markdown-dark.css > ./src/main/resources/static/style.css
RUN wget https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png -O ./src/main/resources/static/favicon.png

RUN case "$(arch)" in \
               amd64|x86_64) \
                 BINARY_URL='https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse'; \
                 ;; \
               aarch64) \
                 BINARY_URL='https://api.adoptium.net/v3/binary/latest/17/ga/linux/aarch64/jdk/hotspot/normal/eclipse'; \
                 ;; \
            esac; \
        wget -O /tmp/openjdk.tar.gz ${BINARY_URL};

RUN ./gradlew --no-daemon installDist

FROM ubuntu:latest as custom-java

COPY --from=builder /tmp/openjdk.tar.gz /tmp/openjdk.tar.gz

RUN mkdir -p /opt/java/openjdk; \
	tar --extract \
	    --file /tmp/openjdk.tar.gz \
	    --directory /opt/java/openjdk \
	    --strip-components 1 \
	    --no-same-owner \
	; \
    rm -rf /tmp/openjdk.tar.gz;

ENV JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:$PATH"

WORKDIR /usr/app

ENV PORT=8088
ENV DASHBOARD_PORT=8089
EXPOSE 8089
EXPOSE 8088

ENTRYPOINT ["/bin/bash"]

FROM custom-java

COPY --from=builder build/install/redirekt ./

ENTRYPOINT ["/usr/app/bin/redirekt"]