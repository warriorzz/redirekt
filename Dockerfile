FROM adoptopenjdk/openjdk16 as builder

COPY . .

RUN ./gradlew --no-daemon installDist

FROM adoptopenjdk/openjdk16

WORKDIR /usr/app

COPY --from=builder build/install/redirekt ./

ENTRYPOINT ["/usr/app/bin/redirekt"]