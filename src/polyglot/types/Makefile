
tp_package	= jltools/types
tp_codebase	= ../..
tp_files 	= AccessFlags.java	\
			ArrayType.java  \
			ClassResolver.java \
			ClassType.java \
			ConstructorType.java \
			FieldInstance.java \
			ImportTable.java \
			JavaClass.java \
			JavaClassImpl.java \
			LoadedJavaClass.java \
			LocalContext.java \
			MethodType.java \
			MethodTypeInstance.java \
			NoClassException.java \
			NullType.java \
			PrimitiveType.java \
			Type.java \
			TypeCheckError.java \
			TypeInstance.java \
			TypeSystem.java 
#			StandardTypeSystem.java \


TYPES_TARGET = $(patsubst %.java, $(tp_package)/%.class, $(tp_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) types
%:
	cd ../.. && $(MAKE) $@
endif
