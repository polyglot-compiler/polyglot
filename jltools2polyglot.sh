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
  echo cannot merge $source into $PWD/target
  exit 1
fi

set -x

target=$PWD/jltools

cvs checkout jltools

(
cd $source

find . -type f -print | grep -v CVS | grep -v \.class$ | while read f
do
  g=`echo $f | sed -e 's/\.jlg/.ppg/g' -e 's/JLgen/PPG/g' -e 's/jlgen/ppg/g' \
                   -e 's/jltools/polyglot/g'`

  mkdir -p `dirname $target/$g` 2>/dev/null
  cp $f $target/$g
done
)

(
cd $target

find . -type f -exec egrep -li jltools | grep -v CVS | while read f; do
  sed -e 's/\<jlgen\>/ppg/g' \
      -e 's/\<JLgen\>/PPG/g' \
      -e 's/\<jltools\>/polyglot/g' \
      -e 's/\<JLtools\>/Polyglot/g' \
      -e 's/\<JLTools\>/Polyglot/g' \
      -e 's/\<JLTOOLS\>/POLYGLOT/g' $f > $f.x
  mv $f.x $f
done
)
