This file describes running the polyglot translator version 1.0 (jlc) from the
command line.

The polyglot translator uses the Java2 virtual machine. It has been
successfully compiled with "javac" from Sun's JDK 1.2.1 and with
"jikes" version 1.04 (14 Sep 99) from IBM Research. jlc requires that
java CUP version 0.10j or later be found in the classpath. 

http://www.javasoft.com/products/jdk/1.2/
http://www10.software.ibm.com/developerworks/opensource/jikes/project/
http://www.cs.princeton.edu/~appel/modern/java/CUP/

--- Invoking The Translator ---

jlc may be run from the command line as follows:

java polyglot.main.Main

This given the following options which are detailed below.

usage: polyglot.main.Main [options] File.jl ...

where [options] includes:
 -d <directory>          output directory
 -S <path list>          source path
 -fqcn                   use fully-qualified class names
 -sx <ext>               set source extension
 -ox <ext>               set output extension
 -dump                   dump the ast
 -scramble [seed]        scramble the ast
 -noserial               disable class serialization
 -op                     use op extension
 -post <compiler>        run javac-like compiler after translation
 -v -verbose             print verbose debugging info
 -version                print version info
 -h                      print this message

Output Directory: -d
By default, output files are written in the same directory as the
source files they are derived from. This option specifies a directory
which will become the root directory of the package hierarchy for all
output files. If this directory does not exist then it will be
created when the first output file is written to disk.

Source Path: -S
Defaults to the current directory plus any directory in which a source
file is found. The path list must be a set of directories separated by
the system-dependent path separator (":" on solaris, ";" on NT). See
"Where Source Files Are Found" below. 

Fully Qualified Class Names: -fqcn
Defaults to OFF. If this options is given, each class name will be
referenced by its fully qualified name. For example, "String" will
appear throughout the output as "java.lang.String". If this options is
set, then import statments will not be included in the output files
(since they are unnecessary).

Source Extension: -sx
Defaults to the extension of the first file given on the command
line. This option should include the "." dot. For example, "-sx .jl"
would search the source path for files such as "Foo.jl".

Output Extension: -ox
Defaults to ".java". This option should include the "." dot. For
example, "-ox .out" would create output files such as "Foo.out". If the
desired output file name (which is derived from the output directory,
if given, and the output extension) is identical to the source file
name, then the output file name will be extended with an additional
"$". In this fashion, jlc will never overwrite the source file with
the output file. It will, however, overwrite older output files.

Dump: -dump
Defaults to OFF. If this option is given, then the abstract syntax
tree is printed out (to stdout) after each stage of the translator.

Scramble: -scramble
Defaults to OFF. A debugging tool, see polyglot.visit.NodeScrambler for
more info. Optionally takes an argument which is the long integer seed
for the random number generator used to pick which part of the tree is
scrambled. 

No Serialization: -noserial
By default all translated class type information is serialized into
the output files. This information will persist into the .class file
and will continue to be available to jlc even if the source or
translated Java file are lost. This allows the distribution of a set
of .class files which still can be used with the polyglot
translator. This option disables class type serialization.

ObjectPrimitive Extension: -op
Defaults to OFF. Enables the ObjectPrimitive extension. This is a
modification to the standard java type system in which primtive types
(int, float, byte, ...) are subtypes of java.lang.Object. See the
documentation in the source code in the "polyglot.ext.op" package.

Post Process Compiler: -post
Defaults to NONE. If the post process compiler is set then the
executable with the given name will be invoked after (and only if) all
source files (and their dependencies) have been successfully
translated by jlc. Note that most javac-like compiler require input
files to have the ".java" extension. 

Verbose: -v -verbose
Print out profuse debugging information to stderr.

Version: -version
Print out the current version of the translator and exit.

Help: -h
Print out a summary of the usage options.


--- Where Class Definitions Are Found ---

1. When the polyglot translator looks for a class by the name
   "foo.bar.Quux" it first searches for that class in any file given
   on the command line. If the class is found one of these files, then
   this definition is used and the remainder of the steps are
   skipped.

2. If none of these files contain the desired class, then the source
   path is searched  next. For example, if the source extension is
   ".jl" and the source path is "mydir:." then the translator looks
   for files "mydir/foo/bar/Quux.jl" and "./foo/bar/Quux.jl". (The
   source path may be set using the -S options, see above.)

3. Regardless of whether or not a source file is found, the translator
   searches the classpath (defined as normal through the environment
   and command-line options to the interpreter) for the desired class.

4. If no source file exists, and no class is found then an error is
   reported (skipping the rest of the steps below).

5. If a source file is found, but no class, then the source file is
   parsed. If it contains the desired class definition (which it
   should) then that definition is used and the remainder of the steps
   are skipped. (If it does not contain this definition, an error is
   reported and the remainder of the steps are skipped.

6. If a class is found but no source file, then the class is examined
   for jlc class type information. If the class contains no class type
   information (this is the case if the class file was compiled from
   raw Java source rather than jlc translated output) then this class
   is used as the desired class definition (skipping all steps below).

7. (class, but no still no source) If the class does contain jlc class
   type information, then the version number of translator used to
   translate the source which created the given class file is compared
   against the version of the current instantiation of the translator.
   If the versions are compatible, then the jlc class type information
   is used as the desired definiton. If the versions are incompatible
   (see the documentation in Compiler.java) then an error is reported.
   In either case, all remaining steps are skipped.

8. If both a suitable source file and class are found, we have a
   choice. If the class definition does not contain jlc class type
   information then the source file is parsed as the definition found
   in this file is used as desired definiton and we stop here. If the
   class does contain jlc class type information, then continue.

9. (source and class with jlc info) Next, the last modification date of
   the source file is compared to the last modification date of the
   source file used to generate the class file. If the source file is
   more recent, the it is parsed as used as the desired definition and
   all remaining steps are skipped.

10. (source and class with jlc info) Next, the jlc version of the class
    and of the current translator are compared (as in 7.). If the
    verisions are incompatible, then we use the definition from the
    parsed source file. If the versions are compatible, then we use
    the definition given by the jlc class type information.

Finally, if at any point an error occurs while reading the jlc class type
information (e.g., if this information exists but is corrupted), then
an error is reported. 


--- Miscellaneous ---

The Solaris release includes an additional file jlc.c which invokes
the java interpreter. This file may be compiled to create an executable
that provides a shortcut to the translator.
