# Reprepro Debian Repository

## Running the service

    docker build -t daisy/reprepro .
    docker run -d -p 80:80 --name reprepro --link nexus:nexus daisy/reprepro

If docker is not available, use the boot2docker image:

    cd ../boot2docker
    vagrant up
    vagrant ssh

    $ docker build -t daisy/reprepro ~/pipeline-it/reprepro
    $ docker run -d -p 80:80 --name reprepro --link nexus:nexus daisy/reprepro

# Using the service

    echo "deb     http://localhost/debian testing main contrib non-free
    deb-src http://localhost/debian testing main contrib non-free
    " > /etc/apt/sources.list.d/repo.daisy.org.list
    sudo aptitude update
