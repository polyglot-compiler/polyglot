SOURCE = ../..
PACKAGE = polyglot/util
SUBDIRS = typedump ppg

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
	StdErrorQueue.java \
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
MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

