import java.util.List;
public class Call02D {
    public <E extends Object> void f(List<E> l1, List l2) {}
    public <E extends Object> void g(List<E> l1, List<E> l2) {}
    public void h(List l1, List l2) {}
}
