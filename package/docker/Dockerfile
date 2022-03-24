FROM maven:3.8-jdk-8 AS builder

ARG GIT_REVISION=docker

# Use GBIF's Maven mirror
RUN mkdir -p /root/.m2
COPY package/docker/m2-settings.xml /root/.m2/settings.xml

WORKDIR /usr/src/ipt
COPY pom.xml .
COPY src src
# Set the build number (shown in the footer) from the provided Git revision
RUN perl -pi -e 's(\${buildNumber})('${GIT_REVISION}')' src/main/resources/application.properties && \
    mvn -DskipTests install && \
    rm target/*.war && mv target/ipt-* target/ipt

FROM tomcat:8.5-jdk8
LABEL MAINTAINERS="Matthew Blissett <mblissett@gbif.org>"

ARG IPT_NAME=ROOT

ENV IPT_DATA_DIR=/srv/ipt

COPY --from=builder /usr/src/ipt/target/ipt /usr/local/tomcat/webapps/${IPT_NAME}

VOLUME /srv/ipt

EXPOSE 8080
CMD ["catalina.sh", "run"]

# Example for customizing the IPT in the Docker build
# (Many approaches are possible, such as changing the CSS, about.ftl etc).
#RUN curl -LSsfo /usr/local/tomcat/webapps/${IPT_NAME}/images/BID-v2.png https://cloud.gbif.org/bid/images/BID-v2.png \
#    && perl -pi -e 's[GBIF-2015-standard-ipt.png][BID-v2.png]' /usr/local/tomcat/webapps/${IPT_NAME}/WEB-INF/pages/inc/menu.ftl
