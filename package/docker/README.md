# Docker IPT

A Docker image for the [GBIF IPT](https://www.gbif.org/ipt).
Tomcat is exposed on port 8080 and the IPT runs as the ROOT application.

The image builds upon the Docker community Tomcat 8.5 / OpenJDK 8 / Debian stable image.

We regularly publish IPT releases as Docker images to Docker Hub: https://hub.docker.com/r/gbif/ipt/

## To run the Docker image

To run a new Docker container, startup Tomcat and expose the Tomcat port run like this:

```
docker run --detach --volume /full/path/to/data-directory:/srv/ipt --publish 8080:8080 gbif/ipt
```

You can then access the setup screen of the IPT on port 8080.

If you need to override the data directory, this can be done with `-e IPT_DATA_DIR=/path/within/container`.

If you need to find the IP address of your "default" Docker machine use `docker-machine ip default`.

**Remember to back up your data directory.**

## Upgrading

Note that, for better consistency with the [Filesystem Hierarchy Standard](https://en.wikipedia.org/wiki/Filesystem_Hierarchy_Standard), from version 2.4.0 the IPT's default data directory is `/srv/ipt`, *not* `/usr/local/ipt`.  This can be overridden, see above.

## To build and deploy the Docker image

```
docker build --build-arg IPT_VERSION=2.4.0 -t gbif/ipt:2.4.0 .
docker push gbif/ipt:2.4.0
```
