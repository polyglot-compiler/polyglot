SOURCE = ../..
PACKAGE = jltools/util
SUBDIRS = typedump

include $(SOURCE)/Rules.mk

SOURCES = \
	Assert.java \
	BitVector.java \
	CachingTransformingList.java \
	CodeWriter.java \
	Copy.java \
	CollectionUtil.java \
	ConcatenatedIterator.java \
	Enum.java \
	ErrorInfo.java \
	ErrorLimitError.java \
	ErrorQueue.java \
	FilteringIterator.java \
	InternalCompilerError.java \
	NestedMap.java \
	NilMap.java \
	Pair.java \
	Position.java \
	Predicate.java \
	StringUtil.java \
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

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
