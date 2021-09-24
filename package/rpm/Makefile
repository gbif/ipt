all: rpm

clean:
	rm -rf RPMS SRPMS SOURCES/ipt.7

# Build here, as CentOS 8 no longer includes "ronn".
SOURCES/ipt.7:
	ronn --roff --organization Global\ Biodiversity\ Information\ Facility SOURCES/ipt.7.ronn > SOURCES/ipt.7

rpm: SOURCES/ipt.7
	bash docker-build.sh

deploy-testing:
	scp -p RPMS/noarch/ipt*.el7.noarch.rpm 'static-vh.gbif.org:/var/www/html/packages/el7/testing/rpm/'
	scp -p RPMS/noarch/ipt*.el8.noarch.rpm 'static-vh.gbif.org:/var/www/html/packages/el8/testing/rpm/'
	ssh static-vh.gbif.org sudo /usr/bin/createrepo --checkts --update /var/www/html/packages/el7/testing/rpm/
	ssh static-vh.gbif.org sudo /usr/bin/createrepo --checkts --update /var/www/html/packages/el8/testing/rpm/

deploy:
	scp -p RPMS/noarch/ipt*.el7.noarch.rpm 'static-vh.gbif.org:/var/www/html/packages/el7/rpm/'
#	scp -p RPMS/noarch/ipt*.el8.noarch.rpm 'static-vh.gbif.org:/var/www/html/packages/el8/rpm/'
	ssh static-vh.gbif.org sudo /usr/bin/createrepo --checkts --update /var/www/html/packages/el7/rpm/
#	ssh static-vh.gbif.org sudo /usr/bin/createrepo --checkts --update /var/www/html/packages/el8/rpm/

.PHONY: all
