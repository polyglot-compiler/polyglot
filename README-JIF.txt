This is release 0.9 of the Jif compiler. Jif is a Java language
extension written using the JLtools extensible compiler toolkit.
It is an implementation of the JFlow language described in the 1999
paper, Practical Mostly-Static Information Flow Control, published
in the Proceedings of the 26th ACM Symposium on Principles of
Programming Languages (POPL), January 1999, pp. 228-241, by Andrew
C. Myers.

Please see the file README for information about the restrictions on
redistributing this software. See src/jltools/ext/jif/AUTHORS for information
on authorship for this software.

To run Jif:
----------

A script named "jif" is created in the bin directory.

usage: jif [options] <source-file>.jif ...

where [options] includes:
 -d <directory>          output directory
 -sourcepath <path list> source path
 -fqcn                   use fully-qualified class names
 -sx <ext>               set source extension
 -ox <ext>               set output extension
 -dump                   dump the ast
 -noserial               disable class serialization
 -nooutput               delete output java files after compilation
 -ext <extension>        use language extension
 -c                      compile only to .java
 -post <compiler>        run javac-like compiler after translation
 -v -verbose             print verbose debugging information
 -report <topic>=<level> print verbose debugging information about topic
                         at specified verbosity
   (Allowed topics: [verbose, jif, frontend, types, jl, solver])
 -version                print version info
 -h                      print this message

 -stop_constraint <n>    Halt when the nth constraint is added
