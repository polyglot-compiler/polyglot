SOURCE = ../..
PACKAGE = jltools/types
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AnonClassType.java \
	ArrayType.java \
	CachingResolver.java \
	ClassContextResolver.java \
	ClassResolver.java \
	ClassType.java \
	CodeInstance.java \
	CompoundResolver.java \
	ConstructorInstance.java \
	Context.java \
	EmptyContextResolver.java \
	FieldInstance.java \
	Flags.java \
	ImportTable.java \
	InitializerInstance.java \
	InnerClassType.java \
	LoadedClassResolver.java \
	LoadedClassType.java \
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
	Types.java \
	TypeSystem.java \
	UnknownQualifier.java \
	UnknownType.java \
	VarInstance.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
