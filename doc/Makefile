SOURCE = ..

include $(SOURCE)/Rules.mk

DOCS = \
	ASTExtensions.txt \
	TypeSystemExtensions.txt \

release:
	mkdir -p $(REL_DOC)
	mkdir -p $(REL_IMG)
	cp -f logo.eps jif-logo.eps $(REL_IMG)
	cp -f $(DOCS) $(REL_DOC)
	cp -f README.txt $(RELPATH)/README-JLC.txt

