assembly/SOURCES := $(assembly/BASEDIR)/pom.xml \
                    $(shell [ -d $(assembly/BASEDIR)/src/main/ ] && find $(assembly/BASEDIR)/src/main/ -type f | sed 's/ /\\ /g')

.SECONDARY : assembly/SOURCES
assembly/SOURCES : $(assembly/SOURCES)

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).dmg         : assembly/SOURCES
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).exe         : assembly/SOURCES
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).deb         : assembly/SOURCES
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).rpm         : assembly/SOURCES
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.zip   : assembly/SOURCES
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-mac.zip     : assembly/SOURCES
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-win.zip     : assembly/SOURCES
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-minimal.zip : assembly/SOURCES

$(assembly/BASEDIR)/target/assembly-$(assembly/VERSION)-mac/daisy-pipeline/bin/pipeline2   : assembly/SOURCES
$(assembly/BASEDIR)/target/assembly-$(assembly/VERSION)-linux/daisy-pipeline/bin/pipeline2 : assembly/SOURCES
