#!/bin/bash -e

gitTag=$(git describe --tags --abbrev=0)
nr_ver=$(echo $gitTag | sed s/ipt-//)

echo "Building version $nr_ver"

CURRENT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"

mkdir -p $CURRENT_DIR/RPMS
chmod 777 $CURRENT_DIR/{RPMS,SOURCES}

echo
echo "CentOS 7 build"
docker pull quay.io/centos/centos:centos7
docker run --rm \
       -e nr_ver=$nr_ver \
       -v $CURRENT_DIR/RPMS:/root/rpmbuild/RPMS/ \
       -v $CURRENT_DIR/SOURCES:/root/rpmbuild/SOURCES/ \
       -v $CURRENT_DIR/SPECS:/root/rpmbuild/SPECS/ \
       quay.io/centos/centos:centos7 \
       "/root/rpmbuild/SPECS/rpm-build.sh"

echo
echo "CentOS Stream8 build"
docker pull quay.io/centos/centos:stream8
docker run --rm \
       -e nr_ver=$nr_ver \
       -v $CURRENT_DIR/RPMS:/root/rpmbuild/RPMS/ \
       -v $CURRENT_DIR/SOURCES:/root/rpmbuild/SOURCES/ \
       -v $CURRENT_DIR/SPECS:/root/rpmbuild/SPECS/ \
       quay.io/centos/centos:stream8 \
       "/root/rpmbuild/SPECS/rpm-build.sh"

echo
echo "CentOS Stream9 build"
docker pull quay.io/centos/centos:stream9
docker run --rm \
       -e nr_ver=$nr_ver \
       -v $CURRENT_DIR/RPMS:/root/rpmbuild/RPMS/ \
       -v $CURRENT_DIR/SOURCES:/root/rpmbuild/SOURCES/ \
       -v $CURRENT_DIR/SPECS:/root/rpmbuild/SPECS/ \
       quay.io/centos/centos:stream9 \
       "/root/rpmbuild/SPECS/rpm-build.sh"

chmod 755 $CURRENT_DIR/{RPMS,SOURCES}
