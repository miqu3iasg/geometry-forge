package core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

/**
 * Manages a linked GLSL shader program with uniform setters.
 * <p>
 * Loads vertex and fragment shaders from fixed paths, compiles and links them on construction.
 * Shader objects are deleted after linking; program must be explicitly cleaned up via {@link #cleanup()}.
 * </p>
 */
public class ShaderProgram {
	private static final String VERTEX_SHADER_PATH = "src/main/resources/shaders/vertex.glsl";
	private static final String FRAGMENT_SHADER_PATH = "src/main/resources/shaders/fragment.glsl";

	private final int programId;

	/**
	 * Compiles and links vertex/fragment shaders from default paths.
	 *
	 * @throws IOException      if shader files cannot be read
	 * @throws RuntimeException if compilation or linking fails
	 */
	public ShaderProgram () throws IOException {
		String vertexSrc = Files.readString(Paths.get(VERTEX_SHADER_PATH));
		String fragmentSrc = Files.readString(Paths.get(FRAGMENT_SHADER_PATH));

		int vertexId = compile(vertexSrc, GL_VERTEX_SHADER);
		int fragmentId = compile(fragmentSrc, GL_FRAGMENT_SHADER);

		programId = glCreateProgram();
		glAttachShader(programId, vertexId);
		glAttachShader(programId, fragmentId);
		glLinkProgram(programId);

		if (glGetProgrami(programId, GL_LINK_STATUS) == 0)
			throw new RuntimeException("Shader link error: " + glGetProgramInfoLog(programId));

		glDeleteShader(vertexId);
		glDeleteShader(fragmentId);
	}

	// Compiles shader source, throws on error
	private int compile (String source, int type) {
		int id = glCreateShader(type);
		glShaderSource(id, source);
		glCompileShader(id);

		if (glGetShaderi(id, GL_COMPILE_STATUS) == 0)
			throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(id));

		return id;
	}

	/**
	 * Binds this program for subsequent draw calls.
	 */
	public void bind () {
		glUseProgram(programId);
	}

	/**
	 * Unbinds any active shader program.
	 */
	public void unbind () {
		glUseProgram(0);
	}

	/**
	 * Uploads a 4x4 matrix to the named uniform.
	 */
	public void setMatrix4f (String name, Matrix4f matrix) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(
				glGetUniformLocation(programId, name),
				false,
				matrix.get(stack.mallocFloat(16))
			);
		}
	}

	/**
	 * Uploads a vec3 to the named uniform.
	 */
	public void setVector3f (String name, Vector3f value) {
		glUniform3f(
			glGetUniformLocation(programId, name),
			value.x,
			value.y,
			value.z
		);
	}

	/**
	 * Deletes the shader program. Instance becomes unusable afterward.
	 */
	public void cleanup () {
		glDeleteProgram(programId);
	}
}
