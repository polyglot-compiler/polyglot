SOURCE = ../..
PACKAGE = jltools/util
SUBDIRS = typedump

include $(SOURCE)/Rules.mk

SOURCES = \
	AnnotatedObject.java \
	Annotate.java \
	Assert.java \
	BitVector.java \
	CachingTransformingList.java \
	CodeWriter.java \
	ConcatenatedIterator.java \
	ErrorInfo.java \
	ErrorLimitError.java \
	ErrorQueue.java \
	FilteringIterator.java \
	InternalCompilerError.java \
	NestedMap.java \
	NilMap.java \
	Position.java \
	Predicate.java \
	SubtypeSet.java \
	Transformation.java \
	TransformingIterator.java \
	TransformingList.java \
	TypedListIterator.java \
	TypedList.java \
	TypeEncoder.java \
	TypeInputStream.java \
	TypeOutputStream.java \
	UnicodeWriter.java \
	UniqueID.java \
	WorkList.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
