#!/bin/bash
#
# Regression test for debian/ipt.maintscript (rm_conffile).
#
# Run from the repo root:
#   docker run --rm -v "$PWD/debian:/d:ro" debian:bookworm bash /d/test-rm-conffile-upgrade.sh
#
# Reproduces the bug reported in gbif/ipt#1470: up to 3.2.x the package shipped
# /etc/tomcat9/Catalina/localhost/ipt.xml as a conffile, and dpkg keeps a conffile
# that a newer version stops shipping unless rm_conffile says otherwise.
#
# No tomcat and no systemd here on purpose: this isolates the dpkg conffile
# mechanism and the prior-version choice, which is the part actually in doubt.
# The rm_conffile lines come from debian/ipt.maintscript, so editing the
# prior-version there changes what this test exercises.
set -euo pipefail

OLD_VER='3.2.3'
BUG_VER='3.3.7+020261016205053.gitc601324.16'   # the version the reporter had
FIX_VER='3.3.7+020261101130000.gitdeadbee.17'   # a plausible next CI build
CTX='/etc/tomcat9/Catalina/localhost/ipt.xml'

SNIPPETS=$(grep '^rm_conffile' /d/ipt.maintscript | sed 's|^|dpkg-maintscript-helper |; s|$| -- "$@"|')
if [ -z "$SNIPPETS" ]; then
  echo "FAIL: no rm_conffile line in debian/ipt.maintscript" >&2
  exit 1
fi
echo "Using from debian/ipt.maintscript:"
echo "$SNIPPETS" | sed 's/^/  /'
echo

mk_control() { # $1=dir $2=version
  mkdir -p "$1/DEBIAN"
  cat > "$1/DEBIAN/control" <<EOF
Package: ipt
Version: $2
Architecture: all
Maintainer: test <test@example.invalid>
Section: utils
Priority: optional
Description: synthetic ipt for conffile testing
EOF
}

# --- old package: ships the context file under /etc as a conffile ---
mk_control /b/old "$OLD_VER"
mkdir -p "/b/old$(dirname "$CTX")"
cat > "/b/old$CTX" <<'EOF'
<Context
  docBase="/usr/share/ipt/ipt.war">
  <!-- Enable symlinks for the jars linked from /usr/share/java -->
  <Resources allowLinking="true"/>
  <Parameter name="IPT_DATA_DIR" value="@data-dir@"/>
</Context>
EOF
echo "$CTX" > /b/old/DEBIAN/conffiles
# the old postinst substituted the data dir in place, like the real one did,
# which is why the file ends up as .dpkg-bak rather than deleted
cat > /b/old/DEBIAN/postinst <<EOF
#!/bin/sh
set -e
sed -i 's|value="@data-dir@"|value="/var/lib/ipt"|' $CTX
exit 0
EOF
chmod 755 /b/old/DEBIAN/postinst

# --- buggy package: file moved to /usr/share, no rm_conffile ---
mk_control /b/bug "$BUG_VER"
mkdir -p /b/bug/usr/share/ipt
cp "/b/old$CTX" /b/bug/usr/share/ipt/ipt.xml

# --- fixed package: same, plus the snippets dh_installdeb generates ---
cp -a /b/bug /b/fix
mk_control /b/fix "$FIX_VER"
for s in preinst postinst prerm postrm; do
  { echo '#!/bin/sh'; echo 'set -e'; echo "$SNIPPETS"; echo 'exit 0'; } > "/b/fix/DEBIAN/$s"
  chmod 755 "/b/fix/DEBIAN/$s"
done

for p in old bug fix; do dpkg-deb -b "/b/$p" "/b/ipt-$p.deb" >/dev/null; done

conffiles() { dpkg-query -W -f='${Conffiles}' ipt; }
listed()    { dpkg -L ipt | grep -cx "$CTX" || true; }

report() {
  echo "--- $1 ---"
  echo "  Conffiles:  $(conffiles)"
  echo "  dpkg -L:    $(listed) entry/entries for $CTX"
  echo "  on disk:    $(ls -1 "$(dirname "$CTX")" 2>/dev/null | tr '\n' ' ')"
}

fail() { echo "FAIL: $1" >&2; exit 1; }

echo "=== 1. install $OLD_VER (tomcat9-era, ships the conffile) ==="
dpkg -i /b/ipt-old.deb >/dev/null
report "after $OLD_VER"
[ "$(listed)" = 1 ] || fail "$OLD_VER should own $CTX"

echo
echo "=== 2. upgrade to $BUG_VER (no rm_conffile: the reported bug) ==="
dpkg -i /b/ipt-bug.deb 2>/dev/null >/dev/null
report "after $BUG_VER"
# this assertion guards the test itself: if the bug stops reproducing here,
# a clean step 3 would prove nothing
case "$(conffiles)" in
  *"$CTX"*obsolete*) : ;;
  *) fail "expected $CTX flagged obsolete after $BUG_VER, got '$(conffiles)'" ;;
esac
[ "$(listed)" = 1 ] || fail "expected dpkg -L to still list $CTX after $BUG_VER"

echo
echo "=== 3. upgrade to $FIX_VER (with rm_conffile) ==="
dpkg -i /b/ipt-fix.deb >/dev/null
report "after $FIX_VER"
[ -z "$(conffiles)" ] || fail "conffile still registered after $FIX_VER: '$(conffiles)'"
[ "$(listed)" = 0 ] || fail "dpkg -L still lists $CTX after $FIX_VER"
[ -f "$CTX.dpkg-bak" ] || fail "expected $CTX.dpkg-bak (the old postinst had modified it)"
[ ! -f "$CTX" ] || fail "$CTX should no longer be in place"

echo
echo "PASS"
