SOURCE = ../..
PACKAGE = jltools.types
SUBDIRS =

SOURCES = \
	AccessFlags.java \
	AmbiguousNameType.java \
	AmbiguousType.java \
	ArrayType.java \
	CachingClassResolver.java \
	ClassCleaner.java \
	ClassContext.java \
	ClassResolver.java \
	ClassType.java \
	ClassTypeImpl.java \
	CompoundClassResolver.java \
	ConstructorType.java \
	ConstructorTypeInstance.java \
	EmptyContext.java \
	FieldInstance.java \
	ImportTable.java \
	LoadedClassResolver.java \
	LoadedClassType.java \
	LocalContext.java \
	LocalInstance.java \
	MethodType.java \
	MethodTypeInstance.java \
	MethodTypeInstanceInitializer.java \
	NoClassException.java \
	NullType.java \
	PackageContext.java \
	PackageType.java \
	ParsedClassType.java \
	PrimitiveType.java \
	ReferenceType.java \
	SemanticException.java \
	StandardTypeSystem.java \
	TableClassResolver.java \
	TargetTable.java \
	Type.java \
	TypeContext.java \
	TypeInstance.java \
	TypeSystem.java \
	VariableInstance.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber:
	-rm -f *.class
