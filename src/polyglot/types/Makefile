
tp_package	= jltools/types
tp_codebase	= ../..
tp_files 	= AccessFlags.java	\
			ArrayType.java  \
			ClassResolver.java \
			ClassType.java \
			ConstructorType.java \
			Context.java \
			FieldType.java \
			ImportTable.java \
			JavaClass.java \
			JavaClassImpl.java \
			LoadedJavaClass.java \
			MethodType.java \
			NoClassException.java \
			NullType.java \
			PrimitiveType.java \
			Type.java \
			TypeSystem.java 
#			StandardTypeSystem.java \


TYPES_TARGET = $(patsubst %.java, $(tp_package)/%.class, $(tp_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) types
%:
	cd ../.. && $(MAKE) $@
endif
