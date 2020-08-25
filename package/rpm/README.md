# GBIF IPT Packaging

This is a CentOS RPM package for installing the IPT.

## Usage

```shell
yum-config-manager --add-repo https://packages.gbif.org/gbif.repo
yum install ipt
```

Configure the IPT's data directory and port by editing `/etc/sysconfig/ipt`, if required.  Then:

```
systemctl enable ipt
systemctl start ipt
```

The IPT will run on the specified port.  After startup, logs are in the IPT's data directory.  If you need to see startup logs, you can use `journalctl -u ipt`

Make sure to **backup the IPT data directory**, which is `/var/lib/ipt` by default.

## Building

(This section is for GBIF developers.)

1. Edit `SPECS/ipt.spec` with a new version number at the top, and a release comment at the bottom.
2. `make clean && make rpm`
3. **Test this RPM!** Install it in a CentOS VM (or Docker container) and see that it runs.
4. `make deploy`

The RPM is published to https://packages.gbif.org/rpm/

```
[gbif]
name=GBIF RPM Packages
baseurl=https://packages.gbif.org/rpm
enabled=1
gpgcheck=0
```

Ideas and some code from https://github.com/istenrot/varnish-modules-rpm-build remain.
