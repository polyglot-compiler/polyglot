SOURCE = ../..
PACKAGE = polyglot/types
SUBDIRS = reflect

include $(SOURCE)/Rules.mk

SOURCES = \
	TypeSystem.java \
	ArrayType.java \
	LazyClassInitializer.java \
	CachingResolver.java \
	Importable.java \
	ClassContextResolver.java \
	ClassResolver.java \
	ClassType.java \
	CodeInstance.java \
	CompoundResolver.java \
	PlaceHolder.java \
	ConstructorInstance.java \
	Context.java \
	FieldInstance.java \
	Flags.java \
	ImportTable.java \
	InitializerInstance.java \
	LoadedClassResolver.java \
	SourceClassResolver.java \
	LocalInstance.java \
	MemberInstance.java \
	MethodInstance.java \
	Named.java \
	NoClassException.java \
	NullType.java \
	Package.java \
	PackageContextResolver.java \
	ParsedClassType.java \
	PrimitiveType.java \
	ProcedureInstance.java \
	Qualifier.java \
	ReferenceType.java \
	Resolver.java \
	SemanticException.java \
	TableResolver.java \
	Type.java \
	TypeObject.java \
	UnknownQualifier.java \
	UnknownType.java \
	VarInstance.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

