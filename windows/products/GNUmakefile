ROOT:=			../..
COMMON_DIR:=		$(ROOT)/lightcrafts
include			$(COMMON_DIR)/mk/platform.mk

COMMON_PRODUCTS:=	$(wildcard $(COMMON_DIR)/products/*)
DEST_COMMON_PRODUCTS:=	$(filter-out GNUmakefile,$(notdir $(COMMON_PRODUCTS)))

DEST_WINDOWS_SHARES:=	share

##
# Build rules
##

.PHONY: all libs

all: libs $(DEST_WINDOWS_SHARES)

$(DEST_COMMON_PRODUCTS): FORCE
	$(call SYMLINK,$(COMMON_DIR)/products/$@,$@)

$(DEST_WINDOWS_SHARES): FORCE
	$(call SYMLINK,../$@,$@)

libs: $(DEST_COMMON_PRODUCTS)
	$(shell ldd *.dll | grep -o " => .*mingw.*\.dll" | sort | uniq | sed 's/ => \(.*\)/"\1"/g' | xargs -r cp -u -t .)
	$(shell ntldd *.dll | grep -o " => .*mingw.*\.dll" | sort | uniq | sed 's/ => \(.*\)/"\1"/g' | xargs -r cygpath | xargs -r cp -u -t .)

.PHONY: FORCE
FORCE:

##
# Utility rules
##

.PHONY: clean distclean mostlyclean

clean distclean mostlyclean:
	$(RM) *.chm *.dll *.jar *.exe *.log share

# vim:set noet sw=8 ts=8:
