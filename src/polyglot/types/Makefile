
tp_package	= jltools/types
tp_codebase	= ../..
tp_files 	= AccessFlags.java	\
			AmbiguousType.java \
			ArrayType.java  \
			ClassResolver.java \
			ClassType.java \
			ClassTypeImpl.java \
			CompoundClassResolver.java \
			ConstructorType.java \
			ConstructorTypeInstance.java \
			FieldInstance.java \
			ImportTable.java \
			LoadedClassResolver.java \
			LoadedClassType.java \
			LocalContext.java \
			MethodType.java \
			MethodTypeInstance.java \
			NoClassException.java \
			NullType.java \
			ParsedClassType.java \
			PrimitiveType.java \
			TableClassResolver.java \
			Type.java \
			TypeCheckError.java \
			TypeInstance.java \
			TypeSystem.java \
			SourceFileClassResolver.java \
			StandardTypeSystem.java 


TYPES_TARGET = $(patsubst %.java, $(tp_package)/%.class, $(tp_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) types
%:
	cd ../.. && $(MAKE) $@
endif
