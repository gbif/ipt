#!/bin/bash -ex

spec_file='rpmbuild/SPECS/ipt.spec'

if [ $(stat -c %U $spec_file) = "UNKNOWN" ]; then
  echo "fixing ownership to avoid rpmbuild errors."
  echo "Someday this will not be necessary when this update sees wider distribution: https://github.com/rpm-software-management/rpm/issues/2 "
  original_uid=$(stat -c %u $spec_file)
  original_gid=$(stat -c %g $spec_file)
  sudo chown -R rpmbuilder:rpmbuilder rpmbuild
fi

function finish {
  # reset uid:gid if we had to fix it up during the build
  if [ "$original_uid" ]; then
	  echo "resetting ownership."
	  sudo chown -R $original_uid:$original_gid rpmbuild
  fi
}
trap finish EXIT

# download and install all RPMs listed as BuildRequires
sudo yum-builddep -y ${spec_file}

# download all source and patch files
spectool -g -R ${spec_file}

# build it
rpmbuild -ba ${spec_file}
