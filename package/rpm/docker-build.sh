#!/bin/bash -e

CURRENT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"

mkdir -p $CURRENT_DIR/{RPMS,SRPMS}
chmod 777 $CURRENT_DIR/{RPMS,SOURCES,SRPMS}

docker pull jc21/rpmbuild-centos7

docker run --rm \
       -v $CURRENT_DIR/RPMS:/home/rpmbuilder/rpmbuild/RPMS/ \
       -v $CURRENT_DIR/SOURCES:/home/rpmbuilder/rpmbuild/SOURCES/ \
       -v $CURRENT_DIR/SPECS:/home/rpmbuilder/rpmbuild/SPECS/ \
       -v $CURRENT_DIR/SRPMS:/home/rpmbuilder/rpmbuild/SRPMS/ \
       jc21/rpmbuild-centos7 \
       "rpmbuild/SPECS/rpm-build.sh"

chmod 755 $CURRENT_DIR/{RPMS,SOURCES,SRPMS}

#scp -p ~/rpmbuild/RPMS/noarch/ipt-*.noarch.rpm static-vh.gbif.org:/var/www/html/packages/rpm/
#echo "SCP completed"

#ssh static-vh.gbif.org /usr/bin/createrepo --checkts --update /var/www/html/packages/rpm/
#echo "Repo updated completed"
