# docker-ipt
A docker image for the GBIF IPT running in Tomcat with JRE8. Tomcat is exposed on port 8080 and the IPT runs as the ROOT application. 

## Build docker image

```docker build -t gbif/ipt .```

## Run docker image locally
To run a new docker container, startup tomcat and expose the tomcat port run this:

```docker run -d -P gbif/ipt```
