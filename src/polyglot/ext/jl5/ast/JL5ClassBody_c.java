package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.ClassBody_c;
import polyglot.ast.ClassMember;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;

public class JL5ClassBody_c extends ClassBody_c {

    public JL5ClassBody_c(Position pos, List<ClassMember> members) {
        super(pos, members);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        // check if we have any EnumConstantDecl
        List<EnumConstantDecl> ecds = enumConstantDecls();
        if (ecds.isEmpty()) {
            super.prettyPrint(w, tr);
            return;
        }

        if (!members.isEmpty()) {
            w.newline(4);
            w.begin(0);
            ClassMember prev = null;

            for (Iterator<EnumConstantDecl> i = ecds.iterator(); i.hasNext();) {
                EnumConstantDecl ecd = i.next();
                prev = ecd;
                print(ecd, w, tr);
                w.write(i.hasNext() ? "," : ";");
                w.allowBreak(1);
            }
            if (!ecds.isEmpty()) {
                w.newline(0);
            }

            for (Iterator<ClassMember> i = members.iterator(); i.hasNext();) {
                ClassMember member = i.next();
                if (member instanceof EnumConstantDecl) {
                    // already printed it
                    continue;
                }

                if ((member instanceof polyglot.ast.CodeDecl)
                        || (prev instanceof polyglot.ast.CodeDecl)) {
                    w.newline(0);
                }
                prev = member;
                printBlock(member, w, tr);
                if (i.hasNext()) {
                    w.newline(0);
                }
            }

            w.end();
            w.newline(0);
        }
    }

    protected List<EnumConstantDecl> enumConstantDecls() {
        List<EnumConstantDecl> ecds = new ArrayList<EnumConstantDecl>();
        for (ClassMember m : this.members) {
            if (m instanceof EnumConstantDecl) {
                ecds.add((EnumConstantDecl) m);
            }
        }
        return ecds;
    }

}
