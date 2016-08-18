sbs-braille-tables
==================

This project contains auxiliary tables that are used at SBS to
generate good German Braille using liblouis.

Create Debian package
---------------------

    mvn clean package

Install
-------

    dpkg -i target/*.deb

This will install the SBS braille tables into /usr/local/share/liblouis/tables.

License
-------

The tables contained within this package are free software. See the
file COPYING for copying conditions.
