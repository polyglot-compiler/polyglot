#!/bin/sh
#
# newext.sh - create a new Polyglot extension from the skeleton
#
# This script creates a copy of polyglot/skel, instantiating
# it with the name of the extension.
#
########################################################################

usage_error() {
    echo "$0: too few arguments"
    echo "Usage: $0 dir package LanguageName ext"
    echo "  where dir          - name to use for the top-level directory"
    echo "                       and for the compiler script"
    echo "        package      - name to use for the Java package"
    echo "        LanguageName - full name of the language"
    echo "        ext          - file extension for source files"
    echo ""
    echo "package and LanguageName must be legal Java identifiers"
    echo "examples:"
    echo "        $0 polyj polyj PolyJ pj"
    echo "        $0 atomjava polyglot.ext.atomjava AtomJava aj"
    exit 1
}

check() {
    if [ -z "$1" ]; then
        usage_error
    fi
}

skel_small="skel"
skel_pkg="skelpkg"
skel_pkgdir="skelpkgdir"
skel_large="Skel"
skel_ext="sx"

ext_small=$1; check "$ext_small"; shift
ext_pkg=$1; check "$ext_pkg"; shift
ext_large=$1; check "$ext_large"; shift
ext_ext=$1; check "$ext_ext"; shift
ext_srcdir=`echo "$ext_pkg" | sed 's%\.%/%g'`

subst_srcdir="s%/src/$skel_small%/src/$ext_srcdir%g"
subst_pkgdir="s%$skel_pkgdir%$ext_srcdir%g"
subst_pkg="s%$skel_pkg%$ext_pkg%g"
subst_small="s%$skel_small%$ext_small%g"
subst_large="s%$skel_large%$ext_large%g"
subst_ext="s%$skel_ext%$ext_ext%g"

sed_opt="-e $subst_pkgdir -e $subst_pkg -e $subst_small -e $subst_large -e $subst_ext"
sed_fopt="-e $subst_srcdir -e $subst_small -e $subst_large -e $subst_ext"

set -x
base=`pwd`
(
cd `dirname $0`/..
find skel \( -path '*/CVS/*' -o -name CVS \) -prune -o -print | while read i; do
    j=`echo "$i" | sed $sed_fopt`
    if [ -d "$i" ]; then
        mkdir -p "$base/$j"
    elif [ "$i" = "skel/README" ]; then
        cp "$i" "$base/$j"
    elif [ -f "$i" ]; then
        sed $sed_opt "$i" > "$base/$j"
    fi
done

mkdir -p "$base/$ext_small/lib"
cp -f "lib/polyglot.jar" "lib/jflex.jar" "lib/ppg.jar" "lib/java_cup.jar" "$base/$ext_small/lib"
)

cd "$base/$ext_small/lib"
chmod 0644 *.jar