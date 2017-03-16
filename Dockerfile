FROM tomcat:jre8
MAINTAINER Markus Döring <mdoering@gbif.org>
EXPOSE 8080
RUN rm -Rf /usr/local/tomcat/webapps \
    && mkdir -p /usr/local/tomcat/webapps/ROOT \
    && mkdir -p /usr/local/ipt \
    && curl -Ls -o ipt.war http://repository.gbif.org/content/groups/gbif/org/gbif/ipt/2.3.4/ipt-2.3.4.war \
    && unzip -d /usr/local/tomcat/webapps/ROOT ipt.war \
    && rm ipt.war
RUN echo "/usr/local/ipt" > /usr/local/tomcat/webapps/ROOT/WEB-INF/datadir.location
CMD ["catalina.sh", "run"]
