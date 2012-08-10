/*
 * Author : Stephen Chong
 * Created: Nov 24, 2003
 */
package polyglot.pth;

import polyglot.util.ErrorInfo;

/**
 * 
 */
public class AnyExpectedFailure extends ExpectedFailure {
    public AnyExpectedFailure() {
        super(-1);
    }

    @Override
    public boolean matches(ErrorInfo e) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AnyExpectedFailure) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (74298);
    }

    @Override
    public String toString() {
        return "(any remaining failures)";
    }
}
