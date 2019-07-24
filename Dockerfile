FROM tomcat:8.5-jre8-alpine
LABEL MAINTAINERS="Markus Döring <mdoering@gbif.org>, Matthew Blissett <mblissett@gbif.org>"

ARG ipt_version
ENV IPT_VERSION=${ipt_version}
ENV IPT_DATA_DIR=/srv/ipt

RUN apk add --no-cache \
    curl \
    unzip \
    && rm -rf /var/cache/apk/*

RUN rm -Rf /usr/local/tomcat/webapps \
    && mkdir -p /usr/local/tomcat/webapps/ROOT \
    && mkdir -p /srv/ipt \
    && curl -LSsfo ipt.war https://repository.gbif.org/repository/releases/org/gbif/ipt/${IPT_VERSION}/ipt-${IPT_VERSION}.war \
    && unzip -d /usr/local/tomcat/webapps/ROOT ipt.war \
    && rm -f ipt.war

VOLUME /srv/ipt

EXPOSE 8080
CMD ["catalina.sh", "run"]
