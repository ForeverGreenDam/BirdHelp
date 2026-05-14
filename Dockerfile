FROM bellsoft/liberica-openjdk-debian:21.0.3-cds

LABEL maintainer="ForeverGreenDam" \
      name="BirdHelp" \
      description="BirdHelp Java Service"

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /birdhelp

COPY ./target/BirdHelp-0.0.1-SNAPSHOT.jar ./app.jar

RUN mkdir -p upload

EXPOSE 7890

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar ./app.jar"]
