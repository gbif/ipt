#!/bin/sh
JAVA_OPTS="-Xms64m -Xmx256m"
exec java $JAVA_OPTS -classpath webapp/WEB-INF/classes/:webapp/WEB-INF/lib/* org.gbif.ipt.Server $*