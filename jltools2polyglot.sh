#!/bin/sh

source=$1
target=$2

if [ $# -ne 2 ]
then
  echo usage: `basename $0` source-jltools-dir target-jltools-dir
  echo example:
  echo "   " cvs checkout jltools
  echo "   " `basename $0` /foo/bar/old/jltools jltools
  exit 1
fi

if [ ! -d $source ]; then
  echo cannot merge $source into $target
  exit 1
fi

if [ ! -d $target ]; then
  echo cannot merge $source into $target
  exit 1
fi

if [ `basename $source` != jltools ]; then
  echo cannot merge $source into $target
  exit 1
fi

if [ `basename $target` != jltools ]; then
  echo cannot merge $source into $target
  exit 1
fi

set -x

(
cd $source

find . -type f -print | while read f
do
  g=`echo $f | sed -e 's/\.jlg/.ppg/g' -e 's/JLgen/PPG/g' -e 's/jlgen/ppg/g' \
                   -e 's/jltools/polyglot/g'`

  mkdir -p $target/`dirname $g` 2>/dev/null
  cp $f $target/$g
done
)

(
cd $target
find . -type f | grep -v CVS | while read f; do
  sed -e 's/jlgen/ppg/g' -e 's/JLgen/PPG/g' -e 's/jltools/polyglot/g' \
      -e 's/JLtools/Polyglot/g' -e 's/JLTools/Polyglot/g' \
      -e 's/JLTOOLS/POLYGLOT/g' $f > $f.x
  mv $f.x $f
done
)
