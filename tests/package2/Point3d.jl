package package2;

import package1.Point;
public class Point3d extends Point {
	protected int z;
	public void delta(Point p) {
		p.x += this.x;          // compile-time error: cannot access p.x
		p.y += this.y;          // compile-time error: cannot access p.y

	}
	public void delta3d(Point3d q) {
		q.x += this.x;
		q.y += this.y;
		q.z += this.z;
	}
}