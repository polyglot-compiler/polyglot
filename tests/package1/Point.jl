package package1;

public class Point {
	protected int x, y;
	void warp(package2.Point3d a) {
		if (a.z > 0)          // compile-time error: cannot access a.z
			a.delta(this);
	}
}
