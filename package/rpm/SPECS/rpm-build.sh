#!/bin/bash -ex

yum install -y rpmdevtools yum-utils

cd /root

spec_file='rpmbuild/SPECS/ipt.spec'
uid=$(stat -c %u $spec_file)
gid=$(stat -c %g $spec_file)

function finish {
	echo "Setting ownership to $uid:$gid"
	chown -R $uid:$gid rpmbuild
}
trap finish EXIT

# download and install all RPMs listed as BuildRequires
yum-builddep -y ${spec_file}

# download all source and patch files
spectool -g -R --define "nr_ver $nr_ver" ${spec_file}

# ===== DEBUG STEPS =====
echo "[DEBUG] STARTING ARCHITECTURE ANALYSIS"

# 1. Check %install content
echo "[DEBUG] PHASE 1: Checking installed files"
rpmbuild -bi --nodeps --define="nr_ver $nr_ver" ${spec_file}

# 2. Detect binaries
echo "[DEBUG] PHASE 2: Scanning for compiled binaries in BUILDROOT"
find ~/rpmbuild/BUILDROOT/ -type f -exec file {} + | grep -i -E 'elf|executable|shared lib' && {
    echo "[WARNING] Found binary files forcing x86_64 build"
} || echo "[OK] No binaries detected"

# 3. Check spec file paths
echo "[DEBUG] PHASE 3: Checking spec for arch-specific paths"
grep -E '/usr/(bin|sbin|lib64|libexec|lib)' ${spec_file} && {
    echo "[WARNING] Found arch-specific paths in %files"
} || echo "[OK] No problematic paths in spec"

# 4. Verify dependencies (if x86_64 package exists)
if [ -d ~/rpmbuild/RPMS/x86_64 ]; then
    echo "[DEBUG] PHASE 4: Checking package dependencies"
    rpm -qp --requires ~/rpmbuild/RPMS/x86_64/*.rpm 2>/dev/null | grep -i '64bit' && {
        echo "[WARNING] Found arch-specific dependencies"
    } || echo "[OK] No arch-specific dependencies"
fi

# build it
rpmbuild -bb --define="nr_ver $nr_ver" ${spec_file}
