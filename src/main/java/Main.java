import core.ShaderProgram;
import core.Window;
import geometry.Cube;
import geometry.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

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

	public static void main (String[] args) {
		logger.info("Starting application...");
		try {
			new Main().run();
		} catch (Exception e) {
			logger.error("Fatal error during application execution", e);
			System.exit(1);
		}
	}

	public void run () {
		Window window = null;

		try {
			logger.info("Initializing window and rendering context");
			window = new Window();

			window.setInitCallback(() -> {
				try {
					logger.info("Initializing rendering resources");

					init();
				} catch (Exception e) {
					throw new RuntimeException("Failed to initialize OpenGL resources", e);
				}
			});

			window.setRenderCallback(this::render);

			logger.info("Starting main loop");
			window.run();
		} catch (Exception e) {
			logger.error("Error during application runtime", e);
			throw new RuntimeException("Application failed to run", e);
		} finally {
			logger.info("Cleaning up resources");
			cleanup();
		}
	}

	private void init () throws Exception {
		try {
			logger.debug("Creating shader program");
			shader = new ShaderProgram();

			logger.debug("Creating cube mesh");
			cube = Cube.createMesh();

			logger.debug(
				"Setting up projection matrix (FOV: {}°, near: {}, far: {})",
				FIELD_OF_VIEW,
				NEAR_PLANE,
				FAR_PLANE
			);

			projectionMatrix = createProjectionMatrix();

			logger.debug("Setting up view matrix (camera position: {})", CAMERA_POSITION);

			viewMatrix = createViewMatrix();

			logger.info("Rendering resources initialized successfully");

		} catch (Exception e) {
			logger.error("Failed to initialize rendering resources", e);
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

			Matrix4f modelMatrix = createModelMatrix();

			bindShaderAndSetUniforms(modelMatrix);

			cube.render();
			shader.unbind();
		} catch (Exception e) {
			logger.error("Error during render frame", e);
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
	}

	private void cleanup () {
		try {
			if (cube != null) {
				logger.debug("Cleaning up cube mesh");
				cube.cleanup();
			}

			if (shader != null) {
				logger.debug("Cleaning up shader program");
				shader.cleanup();
			}

			logger.info("Resource cleanup completed");

		} catch (Exception e) {
			logger.error("Error during cleanup", e);
		}
	}
}
