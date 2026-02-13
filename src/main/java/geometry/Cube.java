package geometry;

public class Cube {

	// 24 vertices (4 per face × 6 faces) for proper per-face normals
	private static final float[] VERTICES = {
		// Front face (z = 0.5)
		-0.5f, -0.5f, 0.5f,  // 0
		0.5f, -0.5f, 0.5f,  // 1
		0.5f, 0.5f, 0.5f,  // 2
		-0.5f, 0.5f, 0.5f,  // 3

		// Back face (z = -0.5)
		0.5f, -0.5f, -0.5f,  // 4
		-0.5f, -0.5f, -0.5f,  // 5
		-0.5f, 0.5f, -0.5f,  // 6
		0.5f, 0.5f, -0.5f,  // 7

		// Left face (x = -0.5)
		-0.5f, -0.5f, -0.5f,  // 8
		-0.5f, -0.5f, 0.5f,  // 9
		-0.5f, 0.5f, 0.5f,  // 10
		-0.5f, 0.5f, -0.5f,  // 11

		// Right face (x = 0.5)
		0.5f, -0.5f, 0.5f,  // 12
		0.5f, -0.5f, -0.5f,  // 13
		0.5f, 0.5f, -0.5f,  // 14
		0.5f, 0.5f, 0.5f,  // 15

		// Top face (y = 0.5)
		-0.5f, 0.5f, 0.5f,  // 16
		0.5f, 0.5f, 0.5f,  // 17
		0.5f, 0.5f, -0.5f,  // 18
		-0.5f, 0.5f, -0.5f,  // 19

		// Bottom face (y = -0.5)
		-0.5f, -0.5f, -0.5f,  // 20
		0.5f, -0.5f, -0.5f,  // 21
		0.5f, -0.5f, 0.5f,  // 22
		-0.5f, -0.5f, 0.5f   // 23
	};

	private static final float[] NORMALS = {
		// Front face
		0.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f,

		// Back face
		0.0f, 0.0f, -1.0f,
		0.0f, 0.0f, -1.0f,
		0.0f, 0.0f, -1.0f,
		0.0f, 0.0f, -1.0f,

		// Left face
		-1.0f, 0.0f, 0.0f,
		-1.0f, 0.0f, 0.0f,
		-1.0f, 0.0f, 0.0f,
		-1.0f, 0.0f, 0.0f,

		// Right face
		1.0f, 0.0f, 0.0f,
		1.0f, 0.0f, 0.0f,
		1.0f, 0.0f, 0.0f,
		1.0f, 0.0f, 0.0f,

		// Top face
		0.0f, 1.0f, 0.0f,
		0.0f, 1.0f, 0.0f,
		0.0f, 1.0f, 0.0f,
		0.0f, 1.0f, 0.0f,

		// Bottom face
		0.0f, -1.0f, 0.0f,
		0.0f, -1.0f, 0.0f,
		0.0f, -1.0f, 0.0f,
		0.0f, -1.0f, 0.0f
	};

	private static final int[] INDICES = {
		0, 1, 2, 2, 3, 0,   // front
		4, 5, 6, 6, 7, 4,   // back
		8, 9, 10, 10, 11, 8,   // left
		12, 13, 14, 14, 15, 12,  // right
		16, 17, 18, 18, 19, 16,  // top
		20, 21, 22, 22, 23, 20   // bottom
	};

	public static Mesh createMesh () {
		return new Mesh(VERTICES, NORMALS, INDICES);
	}
}
