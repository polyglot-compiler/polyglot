SOURCE = ../..
PACKAGE = polyglot/types
SUBDIRS = reflect

include $(SOURCE)/Rules.mk

SOURCES = \
	TypeSystem.java \
	AnonClassType.java \
	ArrayType.java \
	LazyClassInitializer.java \
	CachingResolver.java \
	ClassContextResolver.java \
	ClassResolver.java \
	ClassType.java \
	CodeInstance.java \
	CompoundResolver.java \
	ConstructorInstance.java \
	Context.java \
	FieldInstance.java \
	Flags.java \
	ImportTable.java \
	InitializerInstance.java \
	InnerClassType.java \
	LoadedClassResolver.java \
	SourceClassResolver.java \
	LocalClassType.java \
	LocalInstance.java \
	MemberClassType.java \
	MemberInstance.java \
	MethodInstance.java \
	NamedQualifier.java \
	NamedType.java \
	NoClassException.java \
	NullType.java \
	Package.java \
	PackageContextResolver.java \
	ParsedAnonClassType.java \
	ParsedClassType.java \
	ParsedInnerClassType.java \
	ParsedLocalClassType.java \
	ParsedMemberClassType.java \
	ParsedTopLevelClassType.java \
	ParsedType.java \
	PrimitiveType.java \
	ProcedureInstance.java \
	Qualifier.java \
	ReferenceType.java \
	Resolver.java \
	SemanticException.java \
	TableResolver.java \
	TopLevelClassType.java \
	Type.java \
	TypeObject.java \
	TypeExt.java \
	Types.java \
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

