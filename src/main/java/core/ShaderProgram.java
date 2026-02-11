package core;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
	private static final String VERTEX_SHADER_PATH   = "src/main/resources/shaders/vertex.glsl";
	private static final String FRAGMENT_SHADER_PATH = "src/main/resources/shaders/fragment.glsl";

	private final int programId;

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

	private int compile (String source, int type) {
		int id = glCreateShader(type); // The error points to this line

		glShaderSource(id, source);
		glCompileShader(id);

		if (glGetShaderi(id, GL_COMPILE_STATUS) == 0)
			throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(id));

		return id;

	}

	public void bind () { glUseProgram(programId); }

	public void unbind () { glUseProgram(0); }

	public void setMatrix4f (String name, Matrix4f matrix) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(
				glGetUniformLocation(programId, name),
				false,
				matrix.get(stack.mallocFloat(16))
			);
		}
	}

	public void setVector3f (String name, Vector3f value) {
		glUniform3f(
			glGetUniformLocation(programId, name),
			value.x,
			value.y,
			value.z
		);
	}

	public void cleanup () { glDeleteProgram(programId); }
}
