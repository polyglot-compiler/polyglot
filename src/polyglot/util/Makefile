
utl_package	= jltools/util
utl_codebase	= ../..
utl_files 	= Annotate.java \
			AnnotatedObject.java \
			Assert.java \
			CodeWriter.java \
			ConcatenatedIterator.java \
			FilteringIterator.java \
			NestedMap.java \
			NilMap.java \
			Predicate.java \
			Transformation.java \
			TransformingIterator.java \
			TypedList.java \
			TypedListIterator.java \
			UnicodeWriter.java \
			WorkList.java \


UTIL_TARGET = $(patsubst %.java, $(utl_package)/%.class, $(utl_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) util
%:
	cd ../.. && $(MAKE) $@
endif
