package covarRet;

import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassMember;
import polyglot.ast.JLang_c;
import polyglot.ast.NodeFactory_c;
import polyglot.util.Position;

public class CovarRetNodeFactory extends NodeFactory_c {
    public CovarRetNodeFactory() {
        super(JLang_c.instance);
    }

    @Override
    public ClassBody ClassBody(Position pos, List<ClassMember> members) {
        return new CovarRetClassBody_c(pos, members);
    }
}
