#!/bin/bash
set -euo pipefail

echo
echo
echo "Building snapshot Docker image"
docker build --pull --build-arg GIT_REVISION=$(git rev-parse --verify --short=7 HEAD) -t gbif/ipt:master -f package/docker/Dockerfile .

mvnVersion=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Project version from Maven is $mvnVersion"

echo "Tagging for docker.gbif.org"
docker tag gbif/ipt:master "docker.gbif.org/ipt:$mvnVersion"
echo "Pushing to docker.gbif.org"
docker push "docker.gbif.org/ipt:$mvnVersion"

if [[ "${IS_M2RELEASEBUILD:-false}" == "true" ]]; then
  gitTag=$(git describe --tags --abbrev=0)
  dockerTag=$(echo $gitTag | sed s/ipt-//)
  echo "Checking out release $gitTag for Docker build $dockerTag"
  git checkout $gitTag

  echo "Building Docker image for release $dockerTag using $gitTag"
  docker build --pull --build-arg GIT_REVISION=$(git rev-parse --verify --short=7 HEAD) -t gbif/ipt:$dockerTag -f package/docker/Dockerfile .

  echo "Tagging release $dockerTag as latest"
  docker tag "gbif/ipt:$dockerTag" "gbif/ipt:latest"
  echo "Tagging release $dockerTag as latest for docker.gbif.org"
  docker tag "gbif/ipt:$dockerTag" "docker.gbif.org/ipt:latest"
  echo "Tagging release $dockerTag for docker.gbif.org"
  docker tag "gbif/ipt:$dockerTag" "docker.gbif.org/ipt:$dockerTag"
  echo "Pushing release $dockerTag to docker.gbif.org"
  docker push "docker.gbif.org/ipt:$dockerTag"
  echo "Pushing release $dockerTag to Docker hub"
  docker push "gbif/ipt:$dockerTag"
  echo "Pushing latest to docker.gbif.org"
  docker push "docker.gbif.org/ipt:latest"
  echo "Pushing release latest to Docker hub"
  docker push "gbif/ipt:latest"

  echo "Checking out master"
  git checkout master

  echo
  echo
  echo "Building RPM package"
  # Run docker-build.sh, extract the line containing 'Building version', and grab the version number
  nr_ver=$(cd package/rpm && ./docker-build.sh | grep 'Building version' | sed 's/Building version //')

  echo "Version: $nr_ver"

  echo
  echo "Uploading RPMs"
  scp -p package/rpm/RPMS/noarch/ipt*.el8.noarch.rpm jenkins-deploy@apache.gbif.org:/var/www/html/packages/el8/rpm/
  scp -p package/rpm/RPMS/noarch/ipt*.el9.noarch.rpm jenkins-deploy@apache.gbif.org:/var/www/html/packages/el9/rpm/
  ssh jenkins-deploy@apache.gbif.org /var/www/html/packages/reindex

  git add package/rpm/SPECS/ipt.spec
  git commit -m "Update RPM version to $nr_ver" && git push origin $(git rev-parse --abbrev-ref HEAD) || echo "Nothing changed."
else
  echo "Not a release build, value of IS_M2RELEASEBUILD is ${IS_M2RELEASEBUILD:-unset}"
fi
