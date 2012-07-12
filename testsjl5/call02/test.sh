#!/bin/bash
jl5c -sourcepath raw/ -noserial raw/*.jl5 -d classes/ 
jl5c src/Call02.jl5 -classpath classes/ -d classes/
