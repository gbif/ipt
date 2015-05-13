# Permission settings for IPT data Directory

For people who sets up Tomcat & IPT on UNIX servers (Linux, BSDs),
when there is something not working, and it seems you've followed the
manual properly, sometimes it could be the permission that plagues.

# Details

But, what permission settings are right for IPT data directory? It
depends on the user that is designated to run Tomcat.

In your shell, type

```
$ ps waux | grep tomcat
```

should reveal who is running Tomcat. So assuming the user is
"tomcatuser," and user "tomcatuser" belongs to group "tomcatgroup,"
and the ownership of the IPT data directory (and it's child folders
and files) should be

```
tomcatuser:tomcatgroup
```

Then, with mod 0775 of the IPT data directory, both of the tomcatuser
and tomcatgroup have write permission, some issues about IPT settings
or using the existing IPT directory will be resolved.

As to how the system assigns which user to run Tomcat, it is actually
depended on whether Tomcat is set up as a daemon and used a non-root
user, or, it's actually you firing up the ./startup.sh so Tomcat is
running as the user you used to log in. Either way the IPT data
directory should be owned by the user which runs Tomcat, so settings
and modifications can be saved.