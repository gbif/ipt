all: rpm

clean:
	rm -rf RPMS SRPMS

rpm:
	bash docker-build.sh

deploy-testing:
	scp -p RPMS/noarch/ipt*noarch.rpm 'static-vh.gbif.org:/var/www/html/packages/testing/rpm/'
	ssh static-vh.gbif.org sudo /usr/bin/createrepo --checkts --update /var/www/html/packages/testing/rpm/

deploy:
	scp -p RPMS/noarch/ipt*noarch.rpm 'static-vh.gbif.org:/var/www/html/packages/rpm/'
	ssh static-vh.gbif.org sudo /usr/bin/createrepo --checkts --update /var/www/html/packages/rpm/

.PHONY: all
