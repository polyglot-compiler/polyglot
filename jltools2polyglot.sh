#!/bin/sh

source=$1

if [ $# -ne 1 ]
then
  echo usage: `basename $0` source-jltools-dir
  echo example: `basename $0` /foo/bar/old/jltools
  exit 1
fi

if [ ! -d $source ]; then
  echo cannot merge $source into $PWD/jltools
  exit 1
fi

if [ `basename $source` != jltools ]; then
  echo cannot merge $source into $PWD/jltools
  exit 1
fi

set -x

target=$PWD/jltools

if [ ! -d $target ]
then
    cvs checkout jltools
fi

(
cd $source

find . -type f -print | grep -v CVS | grep -v \.class$ | while read f
do
  g=`echo $f | sed -e 's/\.jlg/.ppg/g' -e 's/JLgen/PPG/g' -e 's/jlgen/ppg/g' \
                   -e 's/jltools/polyglot/g'`

  mkdir -p `dirname $target/$g` 2>/dev/null
  sed -e 's/\<jlgen\>/ppg/g' \
      -e 's/\.jlg\>/.ppg/g' \
      -e 's/JLgen/PPG/g' \
      -e 's/\<jltools\>/polyglot/g' \
      -e 's/\<JLtools\>/Polyglot/g' \
      -e 's/\<JLTools\>/Polyglot/g' \
      -e 's/\<JLTOOLS\>/POLYGLOT/g' $f > $target/$g
done
)
