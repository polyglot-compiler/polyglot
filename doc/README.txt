This file describes running the jltools translator version 1.0 (jlc) from the
command line.

The jltools translator uses the Java2 virtual machine. It has been
successfully compiled with "javac" from Sun's JDK 1.2.1 and with
"jikes" version 1.04 (14 Sep 99) from IBM Research. jlc requires that
java CUP version 0.10j or later be found in the classpath. 

http://www.javasoft.com/products/jdk/1.2/
http://www10.software.ibm.com/developerworks/opensource/jikes/project/
http://www.cs.princeton.edu/~appel/modern/java/CUP/

--- Invoking The Translator ---

jlc may be run from the command line as follows:

java jltools.main.Main

This given the following options which are detailed below.

usage: jltools.main.Main [options] File.jl ...

where [options] includes:
 -d <directory>          output directory
 -S <path list>          source path
 -fqcn                   use fully-qualified class names
 -sx <ext>               set source extension
 -ox <ext>               set output extension
 -dump                   dump the ast
 -scramble [seed]        scramble the ast
 -op                     use op extension
 -post <compiler>        run javac-like compiler after translation
 -v -verbose             print verbose debugging info
 -version                print version info
 -h                      print this message

Output Directory: -d
By default, output files are written in the same directory as the
source files they are derived from. This option specifies a directory
which will become the root directory of the package hierarchy for all
output files. If this directory does not exists then it will we
created when the first output file is written to disk.

Source Path: -S
Defaults to the current directory plus any directory in which a source
file is found. The path list must be a set of directories separated by
the system-dependent path separator (":" on solaris, ";" on NT). See
"Where Source File Are Found" below. 

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
Defaults to OFF. If this options is given, then the abstract syntax
tree is printed out (to stdout) after each stage of the translator.

Scramble: -scramble
Defaults to OFF. A debugging tool, see jltools.visit.NodeScrambler for
more info. Optionally takes an argument which is the long integer seed
for the random number generator used to pick which part of the tree is
scrambled. 

ObjectPrimitive Extension: -op
Defaults to OFF. Enables the ObjectPrimitive extension. This is a
modification to the standard java type system in which primtive types
(int, float, byte, ...) are subtypes of java.lang.Object. See the
documentation in the source code in the "jltools.ext.op" package.

Post Process Compiler: -post
Defaults to NONE. If the post process compiler is set then the
executable with the given name will be invoked after (and only if) all
source files (and their dependencies) have been successfully
translated by jlc. Note that most javac-like compiler require input
files to have the ".java" extension. 

Verbose: -v -verbose
Print out profuse debugging information to strerr.

Version: -version
Print out the current version of the translator and exit.

Help: -h
Print out a summary of the usage options.


--- Where Source Files Are Found ---

When the jltools translator looks for a class by the name
"foo.bar.Quux" it first searchs for that class in any file given on
the command line. If none of these files contain the desired class,
then the source path is searched next. For example, if the source
extension is ".jl" and the source path is "mydir:." then the
translator looks for files "mydir/foo/bar/Quux.jl" and
"./foo/bar/Quux.jl" if neither of these files exist, then the classpath
is used to find an appropriate class file. (Note that this sequence is
subject to change.)


--- Miscellaneous ---

The solaris release includes an additional file jlc.c which invokes
the java interpreter. This file may be compiled into an executable
which provides a shortcut to the translator.
