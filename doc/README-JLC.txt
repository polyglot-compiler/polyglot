This file describes running the polyglot translator version 0.9 from the
command line.

The polyglot translator uses the Java2 virtual machine. It has been
successfully compiled with "javac" from Sun's JDK 1.3.0.
jlc requires that java CUP version 0.10j or later be found in the classpath. 

http://www.javasoft.com/products/jdk/1.3/
http://www.cs.princeton.edu/~appel/modern/java/CUP/

--- Invoking The Translator ---

jlc may be run from the command line as follows:


jlc
java polyglot.main.Main

This given the following options which are detailed below.

usage: jlc [options] <source-file>.jl ...

where [options] includes:
  -d <directory>          output directory
  -classpath <path list>  class path
  -sourcepath <path list> source path
  -fqcn                   use fully-qualified class names
  -sx <ext>               set source extension
  -ox <ext>               set output extension
  -dump                   dump the ast
  -scramble [seed]        scramble the ast
  -noserial               disable class serialization
  -nooutput               delete output files after compilation
  -ext <extension>        use language extension
  -c                      compile only to .java
  -post <compiler>        run javac-like compiler after translation
  -v -verbose             print verbose debugging information
  -report <topic>=<level> print verbose debugging information about topic
                          at specified verbosity
    (Allowed topics: [types, loader, jl, frontend, verbose, qq])
  -version                print version info
  -h                      print this message

Output Directory: -d
By default, output files are written in the same directory as the source
files they are derived from. This option specifies a directory which
will become the root directory of the package hierarchy for all output
files. If this directory does not exist then it will be created when the
first output file is written to disk.

Classpath Path: -classpath
Defaults to the value of the "java.class.path" property.  The path list
must be a set of directories separated by the system-dependent path
separator (":" on Unix, ";" on Windows).

Source Path: -sourcepath
Defaults to the current directory plus any directory in which a source
file is found. The path list must be a set of directories separated by
the system-dependent path separator (":" on Unix, ";" on Windows).

Extension: -ext <ext>
Run with extension <ext>.  The class polyglot.ext.<ext>.ExtensionInfo
must be in the classpath.

Fully Qualified Class Names: -fqcn
Defaults to OFF. If this options is given, each class name will be
referenced by its fully qualified name. For example, "String" will
appear throughout the output as "java.lang.String".

Source Extension: -sx
Defaults to the extension of the first file given on the command
line. This option should include the "." dot. For example, "-sx .jl"
would search the source path for files such as "Foo.jl".

Output Extension: -ox
Defaults to ".java". This option should include the "." dot. For
example, "-ox .out" would create output files such as "Foo.out". If the
desired output file name (which is derived from the output directory, if
given, and the output extension) is identical to the source file name,
then the output file name will be extended with an additional "$". In
this fashion, jlc will never overwrite the source file with the output
file. It will, however, overwrite older output files.

Dump: -dump
Defaults to OFF. If this option is given, then the abstract syntax tree
is pretty-printed (to stdout) just before code generation.

Scramble: -scramble
Defaults to OFF. A debugging tool, see polyglot.visit.NodeScrambler for
more info. Optionally takes an argument which is the long integer seed
for the random number generator used to pick which part of the tree is
scrambled.

No Serialization: -noserial
By default all translated class type information is serialized into the
output files. This information will persist into the .class file and
will continue to be available to jlc even if the source or translated
Java file are lost. This allows the distribution of a set of .class
files which still can be used with the polyglot translator. This option
disables class type serialization.

No Output: -nooutput
Delete .java output files after invoking the post-compiler to build
class files.

Compile only: -c
Generate .java output files only.  Do not invoke the post-compiler.

Post Process Compiler: -post
Defaults to NONE. If the post process compiler is set then the
executable with the given name will be invoked after (and only if) all
source files (and their dependencies) have been successfully translated
by jlc. Note that most javac-like compiler require input files to have
the ".java" extension.

Verbose: -v -verbose
Print out profuse debugging information to stderr.

Report: -report <topic>=<level>
Print out topic-specific debugging information to stderr at specified
verbosity.

Version: -version
Print out the current version of the translator and exit.

Help: -h
Print out a summary of the usage options.
