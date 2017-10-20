# docker-ipt
A docker image for the [GBIF IPT](https://github.com/gbif/ipt) running in Tomcat 8.5 with JRE8 on minimal Alpine Linux. 
Tomcat is exposed on port 8080 and the IPT runs as the ROOT application. 

We regulary publish IPT releases as docker images to docker hub:
https://hub.docker.com/r/gbif/ipt/

## Run docker image locally
To run a new docker container, startup tomcat and expose the tomcat port run this:

```docker run -d -p 8080:8080 gbif/ipt```

You can then access the setup screen of the IPT on port 8080 of your virtual machine.
To find the IP address of your "default" docker machine use this:
```
$ docker-machine ip default
192.168.99.100
```

You can then access the IPT at ```192.168.99.100:8080```
