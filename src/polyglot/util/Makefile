
utl_package	= jltools/util
utl_codebase	= ../..
utl_files 	= Annotate.java \
		  AnnotatedObject.java \
		  Assert.java \
		  BitVector.java \
		  CodeWriter.java \
		  ConcatenatedIterator.java \
		  ErrorInfo.java \
		  ErrorQueue.java \
		  ErrorLimitError.java \
		  FilteringIterator.java \
		  InternalCompilerError.java \
		  NestedMap.java \
		  NilMap.java \
		  Predicate.java \
		  SubtypeSet.java \
		  Transformation.java \
		  TransformingIterator.java \
		  TypedList.java \
		  TypedListIterator.java \
		  TypeInputStream.java \
		  TypeOutputStream.java \
		  TypeEncoder.java \
		  UnicodeWriter.java \
		  UniqueID.java \
		  WorkList.java \


UTIL_TARGET = $(patsubst %.java, $(utl_package)/%.class, $(utl_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) util
%:
	cd ../.. && $(MAKE) $@
endif
