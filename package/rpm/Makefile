all: rpm

clean:
	rm -rf RPMS SRPMS SOURCES/ipt.7

# Build here, as CentOS 8 no longer includes "ronn".
SOURCES/ipt.7:
	ronn --roff --organization Global\ Biodiversity\ Information\ Facility SOURCES/ipt.7.ronn > SOURCES/ipt.7

rpm: SOURCES/ipt.7
	bash docker-build.sh

deploy-testing:
	scp -p RPMS/noarch/ipt*noarch.rpm 'static-vh.gbif.org:/var/www/html/packages/testing/rpm/'
	ssh static-vh.gbif.org sudo /usr/bin/createrepo --checkts --update /var/www/html/packages/testing/rpm/

deploy:
	scp -p RPMS/noarch/ipt*noarch.rpm 'static-vh.gbif.org:/var/www/html/packages/rpm/'
	ssh static-vh.gbif.org sudo /usr/bin/createrepo --checkts --update /var/www/html/packages/rpm/

.PHONY: all
