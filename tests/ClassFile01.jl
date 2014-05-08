interface T131co3a {
    T131co3a m(); // change return type to a instead of b
}
// interface T131co3b extends T131co3a {
//     T131co3b m(); // now covariant!
// }
abstract class T131co3c implements T131co3b {
    // if b were uncommented and compiled by gj, this class would also have
    // /*synthetic*/ T131co3a m() { return /*T131co3b*/m(); }
    // Note that calling a.m() may fail when this class is not compiled by
    // gj, as the bridge method is missing; but that is a different problem.
    { m(); }
}
