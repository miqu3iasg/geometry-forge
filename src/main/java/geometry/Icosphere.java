package geometry;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Icosphere {

	/*The Golden Ratio (φ) is central to icosahedron construction*/
	private static final float PHI = (1.0f + (float) Math.sqrt(5.0f)) / 2.0f;

	/**
	 * Generate an icosphere with the specified subdivision level.
	 *
	 * @param subdivisions Number of times to subdivide (0 = icosahedron, 1-4 typical)
	 * @return Mesh representing the icosphere
	 */


	public static Mesh createMesh(int subdivisions) {
		List<Vector3f> vertices = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		createIcosahedron(vertices, indices);

		// subdivide
		for (int i = 0; i < subdivisions; i++) {
			subdivide(vertices, indices);
		}


		float[] vertexArray = new float[vertices.size() * 3];
		float[] normalArray = new float[vertices.size() * 3];

		for (int i = 0; i < vertices.size(); i++) {
			Vector3f v = vertices.get(i);
			vertexArray[i * 3]     = v.x;
			vertexArray[i * 3 + 1] = v.y;
			vertexArray[i * 3 + 2] = v.z;

			// For a sphere, normal = normalized position
			normalArray[i * 3]     = v.x;
			normalArray[i * 3 + 1] = v.y;
			normalArray[i * 3 + 2] = v.z;
		}

		int[] indexArray = indices.stream().mapToInt(Integer::intValue).toArray();

		return new Mesh(vertexArray, normalArray, indexArray);
	}

	private static void createIcosahedron (List<Vector3f> vertices, List<Integer> indices) {
		float t = PHI;
		// 12 vertices of icosahedron (inscribed in unit sphere, normalized)
		addVertex(vertices, -1, t, 0);  // 0
		addVertex(vertices, 1, t, 0);   // 1
		addVertex(vertices, -1, -t, 0); // 2
		addVertex(vertices, 1, -t, 0);  // 3

		addVertex(vertices, 0, -1, t);  // 4
		addVertex(vertices, 0, 1, t);   // 5
		addVertex(vertices, 0, -1, -t); // 6
		addVertex(vertices, 0, 1, -t);  // 7

		addVertex(vertices, t, 0, -1);  // 8
		addVertex(vertices, t, 0, 1);   // 9
		addVertex(vertices, -t, 0, -1); // 10
		addVertex(vertices, -t, 0, 1);  // 11

		// 20 triangular faces
		// 5 faces around point 0
		addTriangle(indices, 0, 11, 5);
		addTriangle(indices, 0, 5, 1);
		addTriangle(indices, 0, 1, 7);
		addTriangle(indices, 0, 7, 10);
		addTriangle(indices, 0, 10, 11);

		// 5 adjacent faces
		addTriangle(indices, 1, 5, 9);
		addTriangle(indices, 5, 11, 4);
		addTriangle(indices, 11, 10, 2);
		addTriangle(indices, 10, 7, 6);
		addTriangle(indices, 7, 1, 8);

		// 5 faces around point 3
		addTriangle(indices, 3, 9, 4);
		addTriangle(indices, 3, 4, 2);
		addTriangle(indices, 3, 2, 6);
		addTriangle(indices, 3, 6, 8);
		addTriangle(indices, 3, 8, 9);

		// 5 adjacent faces
		addTriangle(indices, 4, 9, 5);
		addTriangle(indices, 2, 4, 11);
		addTriangle(indices, 6, 2, 10);
		addTriangle(indices, 8, 6, 7);
		addTriangle(indices, 9, 8, 1);
	}

	/**
	 * Add a normalized vertex to the list.
	 */
	private static void addVertex (List<Vector3f> vertices, float x, float y, float z) {
		Vector3f v = new Vector3f(x, y, z).normalize();
		vertices.add(v);
	}

	/**
	 * Add a triangle (3 indices).
	 */
	private static void addTriangle (List<Integer> indices, int a, int b, int c) {
		indices.add(a);
		indices.add(b);
		indices.add(c);
	}

	/**
	 * Subdivide all triangles: each triangle becomes 4 triangles.
	 * Uses a midpoint cache to avoid duplicate vertices.
	 */
	private static void subdivide (List<Vector3f> vertices, List<Integer> indices) {
		Map<Long, Integer> midpointCache = new HashMap<>();
		List<Integer> newIndices = new ArrayList<>();

		for (int i = 0; i < indices.size(); i += 3) {
			int v1 = indices.get(i);
			int v2 = indices.get(i + 1);
			int v3 = indices.get(i + 2);

			int m12 = getMiddlePoint(v1, v2, vertices, midpointCache);
			int m23 = getMiddlePoint(v2, v3, vertices, midpointCache);
			int m31 = getMiddlePoint(v3, v1, vertices, midpointCache);

			addTriangle(newIndices, v1, m12, m31);
			addTriangle(newIndices, v2, m23, m12);
			addTriangle(newIndices, v3, m31, m23);
			addTriangle(newIndices, m12, m23, m31);
		}

		indices.clear();
		indices.addAll(newIndices);
	}

	/**
	 * Get or create a midpoint between two vertices.
	 * Uses caching to avoid duplicate vertices.
	 */
	private static int getMiddlePoint (int p1, int p2, List<Vector3f> vertices, Map<Long, Integer> cache) {
		// Ensure consistent ordering for cache key
		boolean firstIsSmaller = p1 < p2;
		long smallerIndex = firstIsSmaller ? p1 : p2;
		long greaterIndex = firstIsSmaller ? p2 : p1;
		long key = (smallerIndex << 32) + greaterIndex;

		if (cache.containsKey(key)) {
			return cache.get(key);
		}

		// Calculate midpoint and normalize (project onto sphere)
		Vector3f v1 = vertices.get(p1);
		Vector3f v2 = vertices.get(p2);
		Vector3f middle = new Vector3f(
			(v1.x + v2.x) / 2.0f,
			(v1.y + v2.y) / 2.0f,
			(v1.z + v2.z) / 2.0f
		).normalize();

		int index = vertices.size();
		vertices.add(middle);
		cache.put(key, index);

		return index;
	}

}
