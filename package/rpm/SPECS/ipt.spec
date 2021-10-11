%define nr_ver 2.5.1
# Set to -RC1 etc for release candidates, and <percent>nil for releases
%define nr_ver_extra %nil
# Set to 0.1 etc for release candidates, and 1 etc for releases
%define release_number 1

Name: ipt
Version: %{nr_ver}
Release: %{release_number}%{dist}
License: ASL 2.0
URL: https://www.gbif.org/ipt
Source0: https://repository.gbif.org/repository/gbif/org/gbif/ipt/%{nr_ver}%{nr_ver_extra}/ipt-%{nr_ver}%{nr_ver_extra}.war
Source1: ipt.service
Source2: ipt.sysconfig
Source3: ipt-vhost.conf
Source4: ipt.7
Summary: GBIF Integrated Publishing Toolkit (IPT)
BuildArch: noarch

%{?systemd_requires}
%define _unitdir /usr/lib/systemd/system

Requires: java >= 1:1.8.0
Requires: jetty-runner

Requires(pre): /usr/sbin/useradd, /usr/bin/getent
Requires(post): systemd
Requires(preun): systemd
Requires(postun): systemd


%description
The Global Biodiversity Information Facility (GBIF) provides this Integrated Publishing Toolkit (IPT) to
facilitate sharing biodiversity data as Darwin Core Archives (DWCA).

This package runs a single, standalone instance of the IPT.

Documentation is available on https://ipt.gbif.org/manual

%prep
cp %{SOURCE0} ipt.war
cp %{SOURCE1} %{SOURCE2} %{SOURCE3} %{SOURCE4} .

%install
install -D -p -m 644 ipt.war %{buildroot}%{_javadir}/gbif/ipt.war
install -D -p -m 644 ipt.sysconfig %{buildroot}%{_sysconfdir}/sysconfig/ipt
install -D -p -m 644 ipt.service %{buildroot}%{_unitdir}/ipt.service
install -D -p -m 644 ipt.7 %{buildroot}%{_mandir}/man7/ipt.7
mkdir -p %{buildroot}%{_localstatedir}/lib/ipt

%files
%{_javadir}/gbif/ipt.war
%{_unitdir}/ipt.service
%config(noreplace) %{_sysconfdir}/sysconfig/ipt
%dir %attr(0755, ipt, ipt) %{_localstatedir}/lib/ipt

%{_mandir}/man7/*.7*
%doc ipt-vhost.conf

%pre
/usr/bin/getent group ipt > /dev/null || /usr/sbin/groupadd --system ipt
/usr/bin/getent passwd ipt > /dev/null || /usr/sbin/useradd --system --group ipt --no-user-group --home-dir %{_localstatedir}/lib/ipt --shell /sbin/nologin ipt

%post
%systemd_post ipt.service

%preun
%systemd_preun ipt.service

%postun
%systemd_postun_with_restart ipt.service

%changelog
* Mon Sep 06 2021 Matthew Blissett <mblissett@gbif.org> - 2.5.1-1
- Publish IPT 2.5.1 release.
* Mon Aug 23 2021 Matthew Blissett <mblissett@gbif.org> - 2.5.0-1
- Publish IPT 2.5.0 release.
* Fri Jun 25 2021 Matthew Blissett <mblissett@gbif.org> - 2.5.0RC1-0.1
- Publish IPT 2.5.0-RC1 pre-release.
* Tue Sep 08 2020 Matthew Blissett <mblissett@gbif.org> - 2.4.2-1
- Publish IPT 2.4.2 release.
* Wed Sep 02 2020 Matthew Blissett <mblissett@gbif.org> - 2.4.1-1
- Publish IPT 2.4.1 release.
* Wed Jul 24 2019 Matthew Blissett <mblissett@gbif.org> - 2.4.0-1
- Publish IPT 2.4.0 release.
* Tue Nov 06 2018 Matthew Blissett <mblissett@gbif.org> - 2.3.6-1
- Publish IPT 2.3.6 release.
* Tue Nov 06 2018 Matthew Blissett <mblissett@gbif.org> - 2.3.5-1
- Publish IPT 2.3.5 release.
* Tue Nov 06 2018 Matthew Blissett <mblissett@gbif.org> - 2.3.4-1
- Publish IPT 2.3.4 release.
* Thu Nov 17 2016 Matthew Blissett <mblissett@gbif.org> - 2.3.2-0.1
- First GBIF IPT release.
