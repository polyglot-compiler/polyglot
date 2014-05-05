package p1;
public class T8464m3a {
    int m() { return 1; }
}
class T8464m3c extends p2.T8464m3b {
    // inherited static b.m() does not clash with accessible instance a.m()
    int i = m();
}
