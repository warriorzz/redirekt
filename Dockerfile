FROM eclipse-temurin:17 as builder

COPY . .
RUN curl -S https://raw.githubusercontent.com/sindresorhus/github-markdown-css/main/github-markdown-dark.css > ./src/main/resources/static/style.css

RUN ./gradlew --no-daemon installDist

FROM eclipse-temurin:17

WORKDIR /usr/app

COPY --from=builder build/install/redirekt ./

ENV PORT=8088
ENV DASHBOARD_PORT=8089
EXPOSE 8089
EXPOSE 8088

ENTRYPOINT ["/usr/app/bin/redirekt"]