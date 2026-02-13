import core.ShaderProgram;
import core.Window;
import geometry.Cube;
import geometry.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Main {

	private static final float FIELD_OF_VIEW = 45.0f;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 100.0f;
	private static final float ROTATION_SPEED = 0.5f;
	private static final float SECONDARY_ROTATION_MULTIPLIER = 0.5f;

	private static final Vector3f CAMERA_POSITION = new Vector3f(0.0f, 0.0f, 3.0f);
	private static final Vector3f CAMERA_TARGET = new Vector3f(0.0f, 0.0f, 0.0f);
	private static final Vector3f CAMERA_UP = new Vector3f(0.0f, 1.0f, 0.0f);

	private static final Vector3f CUBE_COLOR = new Vector3f(0.2f, 0.6f, 1.0f);

	private float rotation = 0.0f;
	private ShaderProgram shader;
	private Mesh cube;
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;

	private final Vector3f lightPos = new Vector3f(2.0f, 2.0f, 2.0f);
	private final Vector3f viewPos = new Vector3f(0, 0, 3);
	private final Vector3f lightColor = new Vector3f(1.0f, 1.0f, 1.0f); // white light
	private final Vector3f objectColor = new Vector3f(0.2f, 0.6f, 1.0f); // blue

	public static void main (String[] args) {
		System.out.println("Starting application...");
		try {
			new Main().run();
		} catch (Exception e) {
			System.err.println("Fatal error during application execution: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run () {
		Window window = null;

		try {
			System.out.println("Initializing window and rendering context");
			window = new Window();

			window.setInitCallback(() -> {
				try {
					System.out.println("Initializing rendering resources");

					init();
				} catch (Exception e) {
					throw new RuntimeException("Failed to initialize OpenGL resources", e);
				}
			});

			window.setRenderCallback(this::render);

			System.out.println("Starting main loop");
			window.run();
		} catch (Exception e) {
			System.err.println("Error during application runtime: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Application failed to run", e);
		} finally {
			System.out.println("Cleaning up resources");
			cleanup();
		}
	}

	private void init () throws Exception {
		try {
			System.out.println("Creating shader program");
			shader = new ShaderProgram();

			System.out.println("Creating cube mesh");
			cube = Cube.createMesh();

			System.out.println(
				"Setting up projection matrix (FOV: " + FIELD_OF_VIEW + "°, near: " + NEAR_PLANE + ", far: " + FAR_PLANE + ")"
			);

			projectionMatrix = createProjectionMatrix();

			System.out.println("Setting up view matrix (camera position: " + CAMERA_POSITION + ")");

			viewMatrix = createViewMatrix();

			System.out.println("Rendering resources initialized successfully");

		} catch (Exception e) {
			System.err.println("Failed to initialize rendering resources: " + e.getMessage());
			e.printStackTrace();
			cleanup(); // Ensure partial cleanup on failure
			throw new RuntimeException("Initialization failed", e);
		}
	}

	private Matrix4f createProjectionMatrix () {
		float aspectRatio = (float) Window.getWidth() / Window.getHeight();
		float fovRadians = (float) Math.toRadians(FIELD_OF_VIEW);

		return new Matrix4f().perspective(fovRadians, aspectRatio, NEAR_PLANE, FAR_PLANE);
	}

	private Matrix4f createViewMatrix () {
		return new Matrix4f().lookAt(CAMERA_POSITION, CAMERA_TARGET, CAMERA_UP);
	}

	private void render () {
		try {
			updateRotation();

			// Debug: print every 60 frames (roughly once per second at 60fps)
			if ((int)rotation % 60 == 0) {
				System.out.println("Render called - rotation: " + rotation);
			}

			Matrix4f modelMatrix = createModelMatrix();

			bindShaderAndSetUniforms(modelMatrix);

			cube.render();
			shader.unbind();
		} catch (Exception e) {
			System.err.println("Error during render frame: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void updateRotation () {
		rotation += ROTATION_SPEED;

		// Prevent float overflow by wrapping at 360 degrees
		if (rotation >= 360.0f) {
			rotation -= 360.0f;
		}
	}

	private Matrix4f createModelMatrix () {
		float primaryRotationRad = (float) Math.toRadians(rotation);
		float secondaryRotationRad = (float) Math.toRadians(rotation * SECONDARY_ROTATION_MULTIPLIER);

		return new Matrix4f()
			.rotate(primaryRotationRad, 1.0f, 0.5f, 0.0f)
			.rotate(secondaryRotationRad, 0.0f, 1.0f, 0.0f);
	}

	private void bindShaderAndSetUniforms (Matrix4f modelMatrix) {
		shader.bind();
		shader.setMatrix4f("model", modelMatrix);
		shader.setMatrix4f("view", viewMatrix);
		shader.setMatrix4f("projection", projectionMatrix);
		shader.setVector3f("color", CUBE_COLOR);

		shader.setVector3f("objectColor", objectColor);
		shader.setVector3f("lightColor", lightColor);
		shader.setVector3f("lightPos", lightPos);
		shader.setVector3f("viewPos", viewPos);
	}

	private void cleanup () {
		try {
			if (cube != null) {
				System.out.println("Cleaning up cube mesh");
				cube.cleanup();
			}

			if (shader != null) {
				System.out.println("Cleaning up shader program");
				shader.cleanup();
			}

			System.out.println("Resource cleanup completed");

		} catch (Exception e) {
			System.err.println("Error during cleanup: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
