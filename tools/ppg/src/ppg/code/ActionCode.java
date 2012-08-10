package ppg.code;

public class ActionCode extends Code {
    public ActionCode(String actionCode) {
        value = actionCode;
    }

    @Override
    public Object clone() {
        return new ActionCode(value.toString());
    }

    @Override
    public String toString() {
        return "action code {:\n" + value + "\n:}\n";
    }
}
