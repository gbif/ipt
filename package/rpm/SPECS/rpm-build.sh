#!/bin/bash -ex

yum install -y rpmdevtools yum-utils

cd /root

spec_file='rpmbuild/SPECS/ipt.spec'
uid=$(stat -c %u $spec_file)
gid=$(stat -c %g $spec_file)

function finish {
	echo "Setting ownership."
	chown -R $uid:$gid rpmbuild
}
trap finish EXIT

# download and install all RPMs listed as BuildRequires
yum-builddep -y ${spec_file}

# download all source and patch files
spectool -g -R --define "nr_ver $nr_ver" ${spec_file}

# build it
rpmbuild -bb --define="nr_ver $nr_ver" ${spec_file}
