import core.Camera;
import core.ShaderProgram;
import core.Window;
import geometry.Cube;
import geometry.Icosphere;
import geometry.Mesh;
import geometry.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.*;

/**
 * Main application entry point for 3D shape rendering with interactive camera controls.
 * <p>
 * Supports toggling between cube and icosphere geometries with configurable lighting
 * and rotation behaviors. Camera can be controlled via mouse drag (rotation) and
 * scroll (zoom).
 */
public class Main {

	// Constants: Rendering Pipeline

	private static final float FIELD_OF_VIEW = 45.0f;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 100.0f;

	// Constants: Camera Configuration

	private static final float CAMERA_INITIAL_RADIUS = 2.5f;
	private static final float CAMERA_INITIAL_THETA = (float) Math.PI / 3;
	private static final float CAMERA_INITIAL_PHI = (float) Math.PI / 4;
	private static final float CAMERA_ROTATION_SENSITIVITY = 0.005f;
	private static final float CAMERA_ZOOM_SENSITIVITY = 0.5f;

	// Constants: Lighting & Materials

	private static final Vector3f FIXED_LIGHT_POSITION = new Vector3f(8.0f, 8.0f, 5.0f);
	private static final Vector3f LIGHT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);
	private static final Vector3f OBJECT_COLOR = new Vector3f(0.2f, 0.6f, 1.0f);
	private static final Vector3f WIREFRAME_COLOR = new Vector3f(0.1f, 0.1f, 0.15f);

	// Constants: Geometry & Animation

	private static final int ICOSPHERE_SUBDIVISIONS = 3;
	private static final float ROTATION_SPEED = 0.5f;

	private static final Vector3f CUBE_PRIMARY_ROTATION_AXIS = new Vector3f(1.0f, 0.5f, 0.0f).normalize();
	private static final Vector3f CUBE_SECONDARY_ROTATION_AXIS = new Vector3f(0.0f, 1.0f, 0.0f);
	private static final float CUBE_SECONDARY_ROTATION_MULTIPLIER = 0.5f;

	private static final Vector3f SPHERE_ROTATION_AXIS = new Vector3f(0.0f, 1.0f, 0.0f);

	// State

	private Shape currentShape = Shape.ICOSPHERE;
	private float rotation = 0.0f;

	// Resources

	private ShaderProgram shader;
	private Mesh cubeMesh;
	private Mesh sphereMesh;
	private Camera camera;
	private Matrix4f projectionMatrix;

	// Application Lifecycle

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
		Window window;

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
			window.setMouseMoveCallback(this::onMouseMove);
			window.setMouseScrollCallback(this::onMouseScroll);
			window.setKeyCallback(this::onKeyPress);

			System.out.println("Starting main loop");
			System.out.println("Controls: Left-click drag to rotate, scroll to zoom, SPACE to switch shapes");
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
			cubeMesh = Cube.createMesh();

			System.out.println("Creating icosphere mesh (subdivisions: " + ICOSPHERE_SUBDIVISIONS + ")");
			sphereMesh = Icosphere.createMesh(ICOSPHERE_SUBDIVISIONS);

			System.out.println("Setting up camera");
			camera = new Camera(CAMERA_INITIAL_RADIUS, CAMERA_INITIAL_THETA, CAMERA_INITIAL_PHI);

			System.out.println("Setting up projection matrix (FOV: " + FIELD_OF_VIEW + "°, near: " + NEAR_PLANE + ", far: " + FAR_PLANE + ")");
			projectionMatrix = createProjectionMatrix();

			System.out.println("Rendering resources initialized successfully");
			System.out.println("Current shape: " + currentShape);

		} catch (Exception e) {
			System.err.println("Failed to initialize rendering resources: " + e.getMessage());
			e.printStackTrace();
			cleanup();
			throw new RuntimeException("Initialization failed", e);
		}
	}

	private void cleanup () {
		try {
			if (cubeMesh != null) {
				System.out.println("Cleaning up cube mesh");
				cubeMesh.cleanup();
			}

			if (sphereMesh != null) {
				System.out.println("Cleaning up sphere mesh");
				sphereMesh.cleanup();
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

	private void render () {
		try {
			updateRotation();

			Matrix4f modelMatrix = createModelMatrix();
			Matrix4f viewMatrix = camera.getViewMatrix();
			Vector3f lightPos = getLightPosition();
			Vector3f viewPos = camera.getPosition();

			bindShaderAndSetUniforms(modelMatrix, viewMatrix, lightPos, viewPos);
			getCurrentMesh().render();
			renderWireframeOverlay();
			shader.unbind();

		} catch (Exception e) {
			System.err.println("Error during render frame: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void renderWireframeOverlay () {
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		shader.setVector3f("objectColor", WIREFRAME_COLOR);
		getCurrentMesh().render();
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}

	// Input Handlers

	private void onMouseMove (double deltaX, double deltaY) {
		camera.rotate((float) deltaX, (float) deltaY, CAMERA_ROTATION_SENSITIVITY);
	}

	private void onMouseScroll (double offsetY) {
		camera.zoom((float) offsetY * CAMERA_ZOOM_SENSITIVITY);
	}

	private void onKeyPress (int key, int action) {
		if (key == 32 && action == 1) {  // GLFW_KEY_SPACE = 32, GLFW_PRESS = 1
			switchShape();
		}
	}

	// Rendering Utilities

	private Matrix4f createProjectionMatrix () {
		float aspectRatio = (float) Window.getWidth() / Window.getHeight();
		float fovRadians = (float) Math.toRadians(FIELD_OF_VIEW);
		return new Matrix4f().perspective(fovRadians, aspectRatio, NEAR_PLANE, FAR_PLANE);
	}

	private Matrix4f createModelMatrix () {
		float rotationRad = (float) Math.toRadians(rotation);

		if (currentShape == Shape.CUBE) {
			return new Matrix4f()
				.rotate(rotationRad,
					CUBE_PRIMARY_ROTATION_AXIS.x,
					CUBE_PRIMARY_ROTATION_AXIS.y,
					CUBE_PRIMARY_ROTATION_AXIS.z)
				.rotate(rotationRad * CUBE_SECONDARY_ROTATION_MULTIPLIER,
					CUBE_SECONDARY_ROTATION_AXIS.x,
					CUBE_SECONDARY_ROTATION_AXIS.y,
					CUBE_SECONDARY_ROTATION_AXIS.z);
		}

		return new Matrix4f()
			.rotate(rotationRad,
				SPHERE_ROTATION_AXIS.x,
				SPHERE_ROTATION_AXIS.y,
				SPHERE_ROTATION_AXIS.z);
	}

	private void bindShaderAndSetUniforms (Matrix4f modelMatrix, Matrix4f viewMatrix,
	                                       Vector3f lightPos, Vector3f viewPos) {
		shader.bind();
		shader.setMatrix4f("model", modelMatrix);
		shader.setMatrix4f("view", viewMatrix);
		shader.setMatrix4f("projection", projectionMatrix);
		shader.setVector3f("objectColor", OBJECT_COLOR);
		shader.setVector3f("lightColor", LIGHT_COLOR);
		shader.setVector3f("lightPos", lightPos);
		shader.setVector3f("viewPos", viewPos);
	}

	private Vector3f getLightPosition () {
		return currentShape == Shape.CUBE ? camera.getPosition() : FIXED_LIGHT_POSITION;
	}

	private Mesh getCurrentMesh () {
		return currentShape == Shape.CUBE ? cubeMesh : sphereMesh;
	}

	private void updateRotation () {
		rotation += ROTATION_SPEED;
		if (rotation >= 360.0f) {
			rotation -= 360.0f;
		}
	}

	private void switchShape () {
		currentShape = (currentShape == Shape.CUBE) ? Shape.ICOSPHERE : Shape.CUBE;
		System.out.println("Switched to: " + currentShape);
	}
}
