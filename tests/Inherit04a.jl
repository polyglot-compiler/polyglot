interface T8111da12b {
    int m();
}
abstract class T8111da12c extends p1.T8111da12a implements T8111da12b {
    // c is legal: since a.m is not inherited, there are no conflicting
    // signatures. However, no concrete subclass of c can exist (either it
    // would be out of p1, and not implement a.m(), or it would be in p1, and
    // cannot override a.m and b.m simultaneously)
}