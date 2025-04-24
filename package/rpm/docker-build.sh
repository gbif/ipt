#!/bin/bash -e

gitTag=$(git describe --tags --abbrev=0)
nr_ver=$(echo $gitTag | sed s/ipt-//)
echo "Building version $nr_ver"

OS_TYPE=$(uname)
if [[ "$OS_TYPE" == "Darwin" ]]; then
  echo "OS type is $OS_TYPE (MacOS)"
  sed -i '' "s|%define nr_ver .*|%define nr_ver $nr_ver|" SPECS/ipt.spec
else
  echo "OS type is $OS_TYPE (Linux)"
  sed -i "s|%define nr_ver .*|%define nr_ver $nr_ver|" SPECS/ipt.spec
fi
CURRENT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"

mkdir -p $CURRENT_DIR/RPMS
chmod 777 $CURRENT_DIR/{RPMS,SOURCES}

echo
echo "EL8 build using Oracle Linux"
docker pull oraclelinux:8
docker run --rm \
       -e nr_ver=$nr_ver \
       -v $CURRENT_DIR/RPMS:/root/rpmbuild/RPMS/ \
       -v $CURRENT_DIR/SOURCES:/root/rpmbuild/SOURCES/ \
       -v $CURRENT_DIR/SPECS:/root/rpmbuild/SPECS/ \
       oraclelinux:8 \
       "/root/rpmbuild/SPECS/rpm-build.sh"

echo
echo "EL9 build using Oracle Linux"
docker pull oraclelinux:9
docker run --rm \
       -e nr_ver=$nr_ver \
       -v $CURRENT_DIR/RPMS:/root/rpmbuild/RPMS/ \
       -v $CURRENT_DIR/SOURCES:/root/rpmbuild/SOURCES/ \
       -v $CURRENT_DIR/SPECS:/root/rpmbuild/SPECS/ \
       oraclelinux:9 \
       "/root/rpmbuild/SPECS/rpm-build.sh"

chmod 755 $CURRENT_DIR/{RPMS,SOURCES}
