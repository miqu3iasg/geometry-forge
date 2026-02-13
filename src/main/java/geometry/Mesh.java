package geometry;

import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Mesh {
	private final int vboId;
	private final int vaoId;
	private final int nboId;
	private final int eboId;
	private final int indexCount;

	public Mesh (float[] vertices,float[] normals, int[] indices) {
		indexCount = indices.length;

		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		// VBO - vertex positions
		vboId = glGenBuffers();
		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
		vertexBuffer.put(vertices).flip();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(0);
		MemoryUtil.memFree(vertexBuffer);

		// NBO - normals
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

	public void render () {
		glBindVertexArray(vaoId);
		glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);
	}

	public void cleanup () {
		glDeleteBuffers(vboId);
		glDeleteBuffers(eboId);
		glDeleteBuffers(nboId);
		glDeleteVertexArrays(vaoId);
	}
}
