# GBIF IPT Packaging

RPM spec for creating an IPT (Integrated Publishing Toolkit) package on CentOS.

## Usage

```
make clean
make rpm
make deploy
```

The RPM is published to http://packages.gbif.org/rpm/

```
[gbif]
name=GBIF RPM Packages
baseurl=http://packages.gbif.org/rpm
enabled=1
gpgcheck=0
```

Ideas and some code from https://github.com/istenrot/varnish-modules-rpm-build remain.
