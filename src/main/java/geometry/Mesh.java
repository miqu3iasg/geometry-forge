package geometry;

import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Represents a renderable 3D mesh composed of vertices, normals, and indexed triangles.
 * <p>
 * This class encapsulates OpenGL buffer objects (VBO, NBO, EBO) and vertex array configuration
 * for efficient GPU-based rendering. Mesh data is uploaded once during construction and remains
 * immutable throughout the object's lifetime.
 * </p>
 * <p>
 * Resource management follows RAII principles: buffers must be explicitly released via
 * {@link #cleanup()} when the mesh is no longer needed.
 * </p>
 */
public class Mesh {
	private final int vboId;  // Vertex buffer object
	private final int vaoId;  // Vertex array object
	private final int nboId;  // Normal buffer object
	private final int eboId;  // Element buffer object (indices)
	private final int indexCount;

	/**
	 * Constructs a mesh from vertex positions, normals, and triangle indices.
	 * <p>
	 * Allocates GPU buffers and configures vertex attribute pointers. The VAO is left unbound
	 * after construction to prevent accidental state mutation.
	 * </p>
	 *
	 * @param vertices flat array of vertex positions (x, y, z) in model space
	 * @param normals  flat array of vertex normals (nx, ny, nz), must match vertex count
	 * @param indices  triangle indices referencing vertex positions (each triplet defines one triangle)
	 * @throws IllegalArgumentException if array lengths are inconsistent or zero
	 */
	public Mesh (float[] vertices, float[] normals, int[] indices) {
		indexCount = indices.length;
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		// VBO - vertex positions (location 0)
		vboId = glGenBuffers();
		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
		vertexBuffer.put(vertices).flip();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(0);
		MemoryUtil.memFree(vertexBuffer);

		// NBO - normals (location 1)
		nboId = glGenBuffers();
		FloatBuffer normalBuffer = MemoryUtil.memAllocFloat(normals.length);
		normalBuffer.put(normals).flip();
		glBindBuffer(GL_ARRAY_BUFFER, nboId);
		glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(1);
		MemoryUtil.memFree(normalBuffer);

		// EBO - indices
		eboId = glGenBuffers();
		IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
		indexBuffer.put(indices).flip();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
		MemoryUtil.memFree(indexBuffer);

		glBindVertexArray(0);
	}

	/**
	 * Submits the mesh geometry for rendering as indexed triangles.
	 * <p>
	 * Binds the internal VAO and issues a draw call using the configured element buffer.
	 * Assumes an appropriate shader program is already bound before invocation.
	 * </p>
	 */
	public void render () {
		glBindVertexArray(vaoId);
		glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);
	}

	/**
	 * Releases all GPU resources associated with this mesh.
	 * <p>
	 * Must be called explicitly to prevent memory leaks. After cleanup, this mesh instance
	 * becomes unusable and should be discarded.
	 * </p>
	 */
	public void cleanup () {
		glDeleteBuffers(vboId);
		glDeleteBuffers(eboId);
		glDeleteBuffers(nboId);
		glDeleteVertexArrays(vaoId);
	}
}
