FROM adoptopenjdk/openjdk16 as builder

COPY . .
RUN curl -S https://raw.githubusercontent.com/sindresorhus/github-markdown-css/main/github-markdown-dark.css > ./src/main/resources/static/style.css

RUN ./gradlew --no-daemon installDist

FROM adoptopenjdk/openjdk16

WORKDIR /usr/app

COPY --from=builder build/install/redirekt ./


ENTRYPOINT ["/usr/app/bin/redirekt"]