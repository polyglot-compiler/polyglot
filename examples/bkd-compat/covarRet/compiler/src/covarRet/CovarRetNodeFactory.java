package covarRet;

import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassMember;
import polyglot.ast.NodeFactory_c;
import polyglot.util.Position;

public class CovarRetNodeFactory extends NodeFactory_c {
    @Override
    public ClassBody ClassBody(Position pos, List<ClassMember> members) {
        return new CovarRetClassBody_c(pos, members);
    }
}
