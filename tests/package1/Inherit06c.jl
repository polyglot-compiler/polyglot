package package1;
public abstract class Inherit06c extends package2.Inherit06b {
    void m() {} // overrides a.m(), even though a.m() is not inherited!
}
