#!/bin/sh

source=$1
target=$2

if [ $# -ne 2 ]
then
  echo usage: `basename $0` source-jltools-dir
  echo example: `basename $0` /foo/jltools /bar/polyglot
  exit 1
fi

if [ ! -d $source ]; then
  echo $source not found
  echo cannot merge $source into $target
  exit 1
fi

if [ `basename $source` != jltools ]; then
  echo cannot merge $source into $target
  exit 1
fi

if [ ! -d `dirname $target` ]; then
  echo `dirname $target` not found
  echo cannot merge $source into $target
  exit 1
fi

(cd $source;
if [ ! -d `dirname $target` ]; then
  echo `dirname $target` 'not found--is it an absolute path?'
  echo cannot merge $source into $target
  exit 1
fi
)

if [ `basename $target` != polyglot ]; then
  echo cannot merge $source into $target
  exit 1
fi

(
cd `dirname $target`

if [ ! -d $target ]; then
    cvs checkout polyglot
fi

if [ ! -d $target ]; then
    echo $target not found
    echo cvs checkout failed
    exit 1
fi
)

(
cd $source

find . -type f -print | grep -v CVS | grep -v \.class$ | while read f
do
  g=`echo $f | sed -e 's/\.jlg/.ppg/g' -e 's/JLgen/PPG/g' -e 's/jlgen/ppg/g' \
                   -e 's/jltools/polyglot/g'`

  echo $f '->' $g

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
