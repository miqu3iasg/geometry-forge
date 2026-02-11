package geometry;

public class Cube {

	// 8 corners of a unit cube centered at origin
	private static final float[] VERTICES = {
		-0.5f, -0.5f, 0.5f,  // 0 front-bottom-left
		0.5f, -0.5f, 0.5f,  // 1 front-bottom-right
		0.5f, 0.5f, 0.5f,  // 2 front-top-right
		-0.5f, 0.5f, 0.5f,  // 3 front-top-left
		-0.5f, -0.5f, -0.5f,  // 4 back-bottom-left
		0.5f, -0.5f, -0.5f,  // 5 back-bottom-right
		0.5f, 0.5f, -0.5f,  // 6 back-top-right
		-0.5f, 0.5f, -0.5f   // 7 back-top-left
	};

	// 6 faces × 2 triangles × 3 indices = 36 indices
	private static final int[] INDICES = {
		0, 1, 2, 2, 3, 0,  // front
		5, 4, 7, 7, 6, 5,  // back
		4, 0, 3, 3, 7, 4,  // left
		1, 5, 6, 6, 2, 1,  // right
		3, 2, 6, 6, 7, 3,  // top
		4, 5, 1, 1, 0, 4   // bottom
	};

	public static Mesh createMesh () {
		return new Mesh(VERTICES, INDICES);
	}
}
