#!/bin/sh

find doc bugs polyglot -type f | grep -v CVS/Repository | while read f; do
  sed -e 's/ppg/ppg/g' -e 's/PPG/PPG/g' -e 's/polyglot/polyglot/g' \
      -e 's/Polyglot/Polyglot/g' -e 's/Polyglot/Polyglot/g' \
      -e 's/POLYGLOT/POLYGLOT/g' $f > $f.x
  mv $f.x $f
done

find polyglot -name Repository | grep CVS | while read f; do
  sed -e 's%jltools/jltools%jltools/polyglot%' \
      -e 's%util/jlgen%util/ppg%g' $f > $f.x
  mv $f.x $f
done

find CVS -type f -print | while read f; do
  sed -e 's/ppg/ppg/g' -e 's/PPG/PPG/g' -e 's/polyglot/polyglot/g' \
      -e 's/Polyglot/Polyglot/g' -e 's/Polyglot/Polyglot/g' \
      -e 's/POLYGLOT/POLYGLOT/g' $f > $f.x
  mv $f.x $f
done
