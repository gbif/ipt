% GBIF-IPT(7)

# IPT

GBIF IPT - Share biodiversity data

## DESCRIPTION

This is a packaged version of the Integrated Publishing Toolkit (IPT),
developed by the Global Biodiversity Information Facility (GBIF).

## CONFIGURATION

Most configuration is done within the IPT web interface.  Two
parameters are set in `/etc/sysconfig/ipt`, though most users won't
need to change them.

* `IPT_DATA_DIR`:
  This specifies where data should be stored.  You should ensure this
  directory is backed up!  The default is `/var/lib/ipt`.

* `IPT_PORT`:
  The port the IPT process will listen on.  The default is 8080.

## STARTING AND STOPPING

Start and stop the IPT using `systemctl`:

- `systemctl start ipt`
- `systemctl status ipt`
- `systemctl restart ipt`
- `systemctl stop ipt`

Set the IPT to start automatically when the server starts up:

- `systemctl enable ipt`

View log output using `journalctl`:

- `journalctl -u ipt`

and/or by looking at the files in /var/lib/ipt/logs.

## WEB SERVER INTEGRATION

Some users choose to install Apache HTTPD web server in front of the
IPT.  An example configuration file for doing this is at
/usr/share/doc/ipt/ipt-vhost.conf.

## RESOURCES

Refer to the [IPT User Manual](https://ipt.gbif.org/manual/) for
documentation and tutorials.

Source code and an issue tracker are is available in [GitHub](https://github.com/gbif/ipt/).

For assistance, write to the [mailing list](https://lists.gbif.org/mailman/listinfo/ipt).
