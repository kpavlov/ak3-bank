#!/usr/bin/env bash

mvn clean verify

java -jar \
    -ea \
    -server \
    -XshowSettings:vm \
    -Xms32m -Xmx32m \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:5005 \
    -Djava.security.egd=file:/dev/./urandom \
    -Duser.timezone=UTC \
    -Dvisualvm.display.name=SimpleBank \
    target/simple-bank-exec.jar

