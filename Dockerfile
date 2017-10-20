FROM tomcat:8.5-jre8-alpine
MAINTAINER Markus Döring <mdoering@gbif.org>
RUN apk add --update curl && \
    apk add --update unzip && \
    rm -rf /var/cache/apk/*
RUN rm -Rf /usr/local/tomcat/webapps \
    && mkdir -p /usr/local/tomcat/webapps/ROOT \
    && mkdir -p /usr/local/ipt \
    && curl -Ls -o ipt.war http://repository.gbif.org/content/groups/gbif/org/gbif/ipt/2.3.5/ipt-2.3.5.war \
    && unzip -d /usr/local/tomcat/webapps/ROOT ipt.war \
    && rm ipt.war
EXPOSE 8080
RUN echo "/usr/local/ipt" > /usr/local/tomcat/webapps/ROOT/WEB-INF/datadir.location
CMD ["catalina.sh", "run"]
