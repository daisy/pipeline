FROM        ubuntu:trusty
RUN         apt-get update
RUN         apt-get install -y reprepro apache2 maven xsltproc wget
ADD         repo.daisy.org.conf /etc/apache2/sites-available/repo.daisy.org.conf
ADD         index.html /var/packages/index.html
RUN         a2dissite 000-default
RUN         a2ensite repo.daisy.org
ADD         distributions /var/packages/debian/conf/distributions
ADD         REPOSITORIES /var/packages/debian/conf/REPOSITORIES
ADD         ARTIFACTS /var/packages/debian/conf/ARTIFACTS
ADD         maven-metadata-to-versions.xsl /bin/maven-metadata-to-versions.xsl
ADD         update-repository.sh /bin/update-repository.sh
ADD         crons.conf /tmp/crons.conf
RUN         chmod +x /bin/update-repository.sh
RUN         crontab /tmp/crons.conf
RUN         touch /var/log/update-repository.log
EXPOSE      80
ENTRYPOINT  ["/bin/bash", "-c", "service apache2 start && cron && tail -f /var/log/update-repository.log"]
