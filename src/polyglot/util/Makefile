SOURCE = ../..
PACKAGE = jltools.util
SUBDIRS =

SOURCES = \
	Annotate.java \
	AnnotatedObject.java \
	Assert.java \
	BitVector.java \
	CodeWriter.java \
	ConcatenatedIterator.java \
	ErrorInfo.java \
	ErrorLimitError.java \
	ErrorQueue.java \
	FilteringIterator.java \
	InternalCompilerError.java \
	NestedMap.java \
	NilMap.java \
	Predicate.java \
	SubtypeSet.java \
	Transformation.java \
	TransformingIterator.java \
	TypeEncoder.java \
	TypeInputStream.java \
	TypeOutputStream.java \
	TypedList.java \
	TypedListIterator.java \
	UnicodeWriter.java \
	UniqueID.java \
	WorkList.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber: cleanclasses
