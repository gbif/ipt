#!/bin/bash
set -euo pipefail

echo
echo
echo "Building RPM package"
# Run docker-build.sh, extract the line containing 'Building version', and grab the version number
nr_ver=$(cd package/rpm && ./docker-build.sh | grep 'Building version' | sed 's/Building version //')

echo "Version: $nr_ver"

echo "Uploading RPMs"
echo "Current working directory: $(pwd)"
echo "RPM files to upload:"
ls -l RPMS/noarch/ipt*.rpm || echo "No RPM files found"

scp -p ../package/rpm/RPMS/noarch/ipt*.el8.noarch.rpm jenkins-deploy@apache.gbif.org:/var/www/html/packages/el8/rpm/
scp -p ../package/rpm/RPMS/noarch/ipt*.el9.noarch.rpm jenkins-deploy@apache.gbif.org:/var/www/html/packages/el9/rpm/
ssh jenkins-deploy@apache.gbif.org /var/www/html/packages/reindex

git add SPECS/ipt.spec
git commit -m "Update RPM version to $nr_ver" && git push origin $(git rev-parse --abbrev-ref HEAD) || echo "Nothing changed."
