FROM bellsoft/liberica-openjdk-debian:21.0.3-cds

LABEL maintainer="ForeverGreenDam" \
      name="BirdHelp" \
      description="BirdHelp Java Service"

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN if [ -f /etc/apt/sources.list.d/debian.sources ]; then \
        sed -i "s@deb.debian.org@mirrors.tuna.tsinghua.edu.cn@g" /etc/apt/sources.list.d/debian.sources; \
    elif [ -f /etc/apt/sources.list ]; then \
        sed -i "s@deb.debian.org@mirrors.tuna.tsinghua.edu.cn@g" /etc/apt/sources.list; \
    fi \
    && apt-get update -qq \
    && apt-get install -y -qq --no-install-recommends \
        curl \
        libxml2 \
        libreoffice-writer \
        libreoffice-calc \
        libreoffice-impress \
        fonts-wqy-microhei \
        fonts-arphic-uming \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /birdhelp

COPY ./target/BirdHelp-0.0.1-SNAPSHOT.jar ./app.jar

RUN mkdir -p upload

EXPOSE 7890

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar ./app.jar"]
