dnl ASTNode.m4
dnl
dnl Nick Mathewson, 1999
dnl
dnl M4 macros to generate AST nodes for JLTools.
dnl
dnl The general assumptions work something like this: All AST nodes descend
dnl from a common parent, jltools.ast.Node.  The default package is 
dnl jltools.ast, but it can be overridden.
dnl 
dnl The following commands are recognized:
dnl    NODES_ARE_IMMUTABLE: Announces that code for immutable nodes is to
dnl          be generated. 
dnl    NODES_ARE_MUTABLE: Announces that code for mutable nodes is to
dnl          be generated. [default]
dnl
dnl    AST_PACKAGE(P): Sets the package name for generated code to <P>.
dnl
dnl    AST_NODE(Name, 'abstract'|'concrete', Parent): Begins constructing
dnl        a new node for a class named <Name>, which is abstract/concrete
dnl        depending on the second argument, and has a parent class <Parent>
dnl
dnl    AST_VARS(var*): Declares the current node to have the fields named by
dnl        var*.  Each argument should be a string matching the regex:
dnl        /[!*?][AJ] Name:Type/, where the first character indicates 
dnl        multiplicity, the second indicated whether the type is an AST class
dnl        ('A'), a Java class ('J'), or a primitive type (`P'), 
dnl        "Name" is the (unique) name of the field, and <Type> is the type 
dnl        of the field.
dnl
dnl    NO_CLASS: Suppresses generation of the standard class intro.
dnl    NO_CONSTRUCTOR: Suppresses generation of the standard constructor.
dnl    NO_SET: Suppresses generation of standard 'setFoo' methods. [Default for
dnl        immutable.]
dnl    NO_GET: Suppresses generation of standard 'getFoo' methods.
dnl    NO_ACCEPT: Suppresses generation of standard 'accept' method.
dnl    NO_VISITCHILDREN: Suppresses generation of standard 'visitChildren' 
dnl        method.
dnl    NO_COPY: Suppresses generation of standard 'copy' method.
dnl    NO_MERGE: Suppresses generation of standard 'merge' method.
dnl    NO_FIELDS: Suppresses generation of standard field declarations.
dnl
dnl    START_CLASS: Opens the class declaration, declaring some members.
dnl    END_CLASS: Closes the class declaration, declaring other members.
dnl    GENERATE: Calls both START_CLASS and END_CLASS.

dnl TODO: Handle primitives right.
dnl TODO: Elide nulls by default.

divert(-1)dnl Throw away all output until we divert again.

dnl =================================================================
dnl Utility macros (Functional programming lives.)
dnl =================================================================

dnl This macro applies its first argument to all the rest in order.
dnl MapEach(F, A, B, C...) = F(A)F(B)F(C)...
define(MapEach, `ifelse($#, 0, , $#, 1, , 
     $#, 2, 
     `ifelse(len($2), 0, , `indir(`$1', $2)')',
     `indir(`$1', $2) MapEach(`$1', ShiftN(2,$@))')')

dnl This macro applies its first argument to all the rest in order, separating
dnl with the second argument.
dnl MapEachSep(F, S, A, B, C...) = F(A)SF(B)SF(C)...
define(MapEachSep, `ifelse($#, 0, , $#, 1, , $#, 2, ,
    $#, 3, `ifelse(len($3), 0, , `indir(`$1', $3)')',
    `indir(`$1', $3)`'$2`'MapEachSep(`$1', `$2', ShiftN(3,$@))')')

dnl This macro treats its first argument as a predicate, the second as a value,
dnl and returns all of the subsequent arguments which cause the first to return
dnl the second.
dnl Filter(F, V, A1...) = {A | A in A1... AND F(A)=V}
define(Filter, `ifelse($#, 0, , $#, 1, , $#, 2, , $#, 3,
  `ifelse(len($3), 0, , `ifelse(indir(`$1', `$3'), `$2', `$3', `')')',
  `ifelse(indir(`$1', `$3'), `$2', 
       ``$3'FiltGotOne(`$1', `$2', ShiftN(3,$@))', 
       `Filter(`$1', `$2', ShiftN(3,$@))')')')

dnl Helper function for Filter.
define(FiltGotOne, `ifelse($#, 0, , $#, 1, , $#, 2, , $#, 3,
`ifelse(indir(`$1', `$3'), `$2', `, $3', `')',
`ifelse(indir(`$1', `$3'), `$2', `, `$3'FiltGotOne(`$1', `$2', ShiftN(3,$@))', 
 `FiltGotOne(`$1', `$2', ShiftN(3,$@))')')')

dnl ShiftN(N, A, B, C...) returns all but the first N elements of A,B,C...
define(ShiftN, `ifelse($#, 0, , $#, 1, , $1, 0, `shift($@)', 
          $1, 1, `shift(shift($@))',
          `ShiftN(decr($1), shift(shift($@)))')')

dnl UCFirst(W) returns W with its first character capitalized.
define(UCFirst, `translit(substr(`$1',0,1), `a-z', `A-Z')`'substr(`$1', 1)')

dnl =================================================================
dnl These functions help us access field information.
dnl =================================================================

dnl The general form for a field declaration is '[!*?][AJ] Name:Type', where 
dnl the first character indicates multiplicity and the second indicates whether
dnl it is an AST node ('A') or a Java node ('J').
define(NodeMult, `substr($1, 0, 1)')
define(NodeKind, `substr($1, 1, 1)')
define(NodeName, `substr($1, 3, eval(index($1, `:') -3))')
define(NodeType, `substr($1, incr(index($1,`:')))')
define(NodeStoreType, 
`ifelse(NodeMult(`$1'), `*', `TypedList', `NodeType($1)')')
define(NodeInType,
`ifelse(NodeMult(`$1'), `*', `List', `NodeType($1)')')

dnl This creates a declaration for a variable parameter, with no trailing 
dnl comma or semi.
define(VarParam, `NodeInType($1) NodeName($1)')

dnl This declares a field or local, with a trailing semi
define(VarField, `INDENT`'NodeStoreType($1) NodeName($1)`;'NEWLINE')

dnl =================================================================
dnl Typographic macros. (PROBABLY UNNECESSARY)
dnl =================================================================

define(NEWLINE, `
')

define(`INDENT', `  ')
define(`PUSHINDENT', `pushdef(`INDENT', `  'INDENT)')
define(`POPINDENT', `popdef(`INDENT')')

dnl =================================================================
dnl Macros for generating file data.
dnl =================================================================

dnl -----------------------------------------------------------------
dnl NodeFileIntro takes the name of the node as an argument, and the name of
dnl the package as a second argument. It generates a short header, along
dnl with the imports we know we'll need.
dnl
define(NodeFileIntro, ``/**
 * $1.java
 *
 * This is an autogenerated file: DO NOT EDIT.
 **/
package $2;

import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import java.util.*;'
ifelse($2, `jltools.ast',`',`import jltools.ast.*;')')

dnl -----------------------------------------------------------------
dnl NodeIntro takes a Node type, a parent type, and an optional argument,
dnl    which should either be 'abstract' or 'concrete'.  It generates the
dnl    opening of the class declaration.
dnl
dnl NodeIntro(`BlockStatement', `Statement', `concrete')
define(NodeIntro, `ifelse($3, `abstract', `public abstract class', 
                          `public class') dnl
$1 extends $2 {'`PUSHINDENT(2)'`NEWLINE')


dnl ------------------------------------------------------------
dnl These helper functions are used to generate statements and expressions,
dnl and probably should never get called directly.

dnl Helper function: takes a field f as an argument, and generates code
dnl equivalent (more or less) to "this.f = f;"
dnl
define(VarAssign, `dnl
ifelse(NodeKind(`$1'),`P',`INDENT`this.'NodeName($1)` = 'NodeName($1)',
NodeMult(`$1'),`?',`INDENT`this.'NodeName($1)` = NodeName($1);'NEWLINE',
NodeMult(`$1'), `!', 
INDENT`if (NodeName($1) == null)'
INDENT`  throw new NullPointerException("NodeName($1) may not be null.");'
INDENT`this.NodeName($1) = NodeName($1);'`NEWLINE',
INDENT`this.NodeName($1) = TypedList.copyAndCheck(NodeName($1),'
INDENT`                         NodeType($1),'
INDENT`                         IMMUTABLE);'`NEWLINE')')

dnl Helper function: Takes a field f as an argument, and generates code
dnl equivalent (more ore less) to "this.f != f"
define(VarHasChanged, `dnl
ifelse(NodeMult(`$1'), `*', 
``!NodeUtils.shallowEq('NodeName(`$1')`, this.'NodeName(`$1')`)'',
`NodeName(`$1')` != this.'NodeName(`$1')')')

dnl Helper function: Takes a field f, and generates code for 
dnl "deep ? f.copy(deep) : f"
define(CopyVar, `dnl
ifelse(NodeKind(`$1', `P', `NodeName(`$1'),
   NodeKind(`$1'), `J', 
     `ifelse(NodeMult(`$1'), `*', ``TypedList.copy('NodeName(`$1')`)'',
             `NodeName(`$1')')',
   NodeMult(`$1'), `!', 
     ``deep ? ('NodeType(`$1')`) 'NodeName(`$1')`.copy() : 'NodeName(`$1')',
   NodeMult(`$1'), `?', 
     `NodeName(`$1')`==null?null:`dnl
      'deep ? ('NodeType(`$1')`) 'NodeName(`$1')`.copy() : 'NodeName(`$1')',
   ``NodeUtils.copyNodeList('NodeName(`$1')`, deep)'')')

dnl -----------------------------------------------------------------
dnl Method declarations.

dnl This takes a node name, followed by an arbitrary number of variables.
define(ConstructorDecl, `dnl
INDENT`'public `$1'`('MapEachSep(`VarParam', `, ', shift($@))`) {'dnl
PUSHINDENT
MapEach(`VarAssign', shift($@))`'POPINDENT`'INDENT`'}')

dnl This takes a variable.
define(AccessorDecl, `dnl
INDENT`public 'NodeStoreType(`$1')` get'UCFirst(NodeName(`$1'))`() {'PUSHINDENT
INDENT`return 'NodeName(`$1')`;'POPINDENT
INDENT`}'')

define(MutatorDecl, `dnl
INDENT`public void set'UCFirst(NodeName(`$1'))`('dnl
NodeInType(`$1') NodeName(`$1')`) {'PUSHINDENT
INDENT`'VarAssign(`$1')`'POPINDENT
INDENT`}'')

dnl The first arg is a node name; the rest are variables.
define(MergeDecl, `dnl
INDENT`public $1 merge('MapEachSep(`VarParam', `, ', shift($@))`) {'PUSHINDENT
INDENT`if ('MapEachSep(`VarHasChanged', ` || ', shift($@))`) {'PUSHINDENT
INDENT`return new $1('MapEachSep(`NodeName', `, ', shift($@))`);'POPINDENT
INDENT`else'PUSHINDENT
INDENT`return this;'POPINDENT`'POPINDENT
INDENT`}'')

dnl Takes the name of the node.
define(AcceptDecl, `dnl
INDENT`public Node accept(NodeVisitor vis) {'PUSHINDENT
INDENT`  return vis.visit'$1`(this);'POPINDENT
INDENT`}'')

dnl Helper function: recurses `accept' on a variable.
define(VisitVarMutable, `dnl
ifelse(VarMult(`$1'), `!',
INDENT`'NodeName(`$1')` = ('NodeType(`$1')`) 'NodeName(`$1')`.accept(vis);'
, VarMult(`$1'), `?'
INDENT`if ('NodeName(`$1')` != null)'PUSHINDENT 
INDENT`'NodeName(`$1')` = ('NodeType(`$1')`) 'NodeName(`$1')`.accept(vis);'
POPINDENT,
INDENT`'NodeName(`$1')` = NodeUtils.visitMutableList(vis,'NodeName(`$1')`);'
)')

define(VisitVarImmutable, `dnl
ifelse(VarMult(`$1'), `!',
INDENT`'NodeName(`$1')` = ('NodeType(`$1')`) this.'NodeName(`$1')`.accept(vis);', 
VarMult(`$1'), `?', 
INDENT`if (this.'NodeName(`$1')` != null)'PUSHINDENT
INDENT`'NodeName(`$1')` = ('NodeType(`$1')`) this.'NodeName(`$1')`.accept(vis);'
POPINDENT, 
INDENT`'NodeName(`$1')` = NodeUtils.visitImmutableList(vis,this.'NodeName(`$1')`);'
)')

dnl Takes the name of the node and all variables.
define(VisitChildrenMutable, `dnl
INDENT`public void visitChildren(NodeVisitor vis) {'PUSHINDENT
MapEach(`VisitVarMutable', Filter(`NodeKind', `A', shift($@)))`'POPINDENT
`}'')

dnl Takes the name of the node and all variables.
define(VisitChildrenImmutable, `dnl
INDENT`public $1 visitChildren(NodeVisitor vis) {'PUSHINDENT
MapEach(`VarField', Filter(`NodeKind', `A', shift($@)))`'dnl
MapEach(`VisitVarImmutable', Filter(`NodeKind', `A', shift($@)))dnl
INDENT`return merge('MapEach(`NodeName',shift($@))`);'POPINDENT
INDENT`}'')

dnl Takes the name of the node and all variables.  (Mutable only.) !!!!
define(CopyDecl, `dnl
INDENT`public $1 copy(boolean deep) {'PUSHINDENT
INDENT`'PUSHINDENT`return new $1('MapEachSep(`CopyVar', `,NEWLINE`'INDENT',
                      shift($@))`);'POPINDENT`'POPINDENT
INDENT`}'')

dnl Takes a list of variables
define(FieldsDecl, `dnl
MapEach(`VarField', `$@')')

dnl =================================================================
dnl Initialize these just in case.
define(MUTABLE, `true')
define(IMMUTABLE, `false')
define(`_AST_PACKAGE_', `jltools.ast')
dnl =================================================================

dnl =================================================================
dnl ======== Here are the macros we should actually be using from the
dnl ======== outside.
dnl =================================================================

dnl NODES_ARE_(IM)MUTABLE
dnl AST_PACKAGE(PackageName)
dnl
dnl AST_NODE(NodeName, abstract|concrete, ParentName)
dnl AST_VARS(var*)
dnl NO_{CLASS|CONSTRUCTOR|SET|GET|ACCEPT|VISITCHILDREN|COPY|FIELDS}
dnl START_CLASS END_CLASS
dnl GENERATE

define(`NODES_ARE_MUTABLE', `define(`MUTABLE', `true')dnl
define(`IMMUTABLE', `false')')

define(`NODES_ARE_IMMUTABLE', `define(`MUTABLE', `false')dnl
define(`IMMUTABLE', `true')')

define(`AST_PACKAGE', `dnl
define(`_AST_PACKAGE_', $1)')

define(`AST_NODE', `dnl
define(`_AST_NAME_', `$1')dnl
define(`_AST_ABSTRACT_', `$2')dnl
define(`_AST_PARENT_', `$3')dnl
define(`_AST_CONSTRUCTOR_', 1)dnl
undefine(`_AST_IS_ABSTRACT_')dnl
define(`_AST_SET_', 1)dnl
define(`_AST_GET_', 1)dnl
define(`_AST_ACCEPT_', 1)dnl
define(`_AST_VISITCHILDREN_', 1)dnl
define(`_AST_FIELDS_', 1)dnl
define(`_AST_COPY_', 1)dnl
define(`_AST_MERGE_', 1)dnl
define(`_AST_CLASS_', 1)dnl
ifelse(`_AST_ABSTRACT_', `abstract', `define(_AST_IS_ABSTRACT_, 1)dnl
  NO_ACCEPT`'NO_VISITCHILDREN`'NO_COPY')dnl
ifelse(IMMUTABLE, `true', `NO_SET`'NO_COPY')dnl
define(`INDENT', `')dnl
define(`_AST_VARS_', )dnl
========== $1
NodeFileIntro(_AST_NAME_, _AST_PACKAGE_)')

define(`AST_VARS', `define(`_AST_VARS_', `$@')')
define(`NO_CONSTRUCTOR', `undefine(`_AST_CONSTRUCTOR_')')
define(`NO_SET', `undefine(`_AST_SET_')')
define(`NO_GET', `undefine(`_AST_GET_')')
define(`NO_ACCEPT', `undefine(`_AST_ACCEPT_')')
define(`NO_VISITCHILDREN', `undefine(`_AST_VISITCHILDREN_')')
define(`NO_FIELDS', `undefine(`_AST_FIELDS_')')
define(`NO_COPY', `undefine(`_AST_COPY_')')
define(`NO_MERGE', `undefine(`_AST_MERGE_')')
define(`NO_CLASS', `undefine(`_AST_CLASS_')')

define(`GENERATE', `START_CLASS`'END_CLASS')

define(`START_CLASS', `dnl
ifdef(`_AST_CLASS_', NodeIntro(_AST_NAME_, _AST_PARENT_, _AST_ABSTRACT_))
ifdef(`_AST_CONSTRUCTOR_', `ConstructorDecl(_AST_NAME_, _AST_VARS_)')
')

define(`END_CLASS', `dnl
ifdef(`_AST_GET_', `MapEach(`AccessorDecl', _AST_VARS_)')
ifdef(`_AST_SET_', `MapEach(`MutatorDecl', _AST_VARS_)')
ifdef(`_AST_MERGE_', `MergeDecl(_AST_NAME_, _AST_VARS_)')
ifdef(`_AST_ACCEPT_', `AcceptDecl(_AST_NAME_)')
ifdef(`_AST_VISIT_CHILDREN_', 
    `ifelse(`MUTABLE', `true', `VisitChildrenMutable(_AST_NAME_, _AST_VARS_)',
                       `VisitChildrenImmutable(_AST_NAME_, _AST_VARS_)')')
ifdef(`_AST_COPY_', `CopyDecl(_AST_NAME_, _AST_VARS_)')
ifdef(`_AST_FIELDS_', `FieldsDecl(_AST_VARS_)')`'POPINDENT
}')

divert(0)dnl