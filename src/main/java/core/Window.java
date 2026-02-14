package core;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 600;
	private static final String WINDOW_TITLE = "Geometry Forge";
	private static final int OPENGL_VERSION_MAJOR = 3;
	private static final int OPENGL_VERSION_MINOR = 3;

	private static final float CLEAR_COLOR_R = 0.1f;
	private static final float CLEAR_COLOR_G = 0.1f;
	private static final float CLEAR_COLOR_B = 0.12f;
	private static final float CLEAR_COLOR_A = 1.0f;

	@FunctionalInterface
	public interface MouseMoveCallback {
		void onMouseMove (double deltaX, double deltaY);
	}

	@FunctionalInterface
	public interface MouseScrollCallback {
		void onMouseScroll (double offsetY);
	}

	@FunctionalInterface
	public interface KeyCallback {
		void onKeyPress (int key, int action);
	}

	private long handle;

	private Runnable renderCallback;
	private Runnable initCallback;
	private MouseMoveCallback mouseMoveCallback;
	private MouseScrollCallback mouseScrollCallback;
	private KeyCallback keyCallback;

	private double lastMouseX;
	private double lastMouseY;
	private boolean isDragging;


	public void run () {
		init();
		loop();
		cleanup();
	}

	// Callback setters

	public void setMouseMoveCallback (MouseMoveCallback callback) {
		this.mouseMoveCallback = callback;
	}

	public void setMouseScrollCallback (MouseScrollCallback callback) {
		this.mouseScrollCallback = callback;
	}

	public void setRenderCallback (Runnable callback) {
		this.renderCallback = callback;
	}

	public void setInitCallback (Runnable callback) {
		this.initCallback = callback;
	}

	public void setKeyCallback (KeyCallback callback) {
		this.keyCallback = callback;
	}

	// Static accessors

	public static int getWidth () {
		return WINDOW_WIDTH;
	}

	public static int getHeight () {
		return WINDOW_HEIGHT;
	}

	public long getHandle () {
		return handle;
	}

	// Initialization

	private void init () {
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		configureWindowHints();

		handle = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, NULL, NULL);

		if (handle == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		registerInputCallbacks();

		try (MemoryStack stack = stackPush()) {
			centerWindow(stack);

			glfwMakeContextCurrent(handle);

			glfwSwapInterval(1); // Enable v-sync

			glfwShowWindow(handle);
		}
	}

	private void configureWindowHints () {
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, OPENGL_VERSION_MAJOR);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, OPENGL_VERSION_MINOR);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
	}

	private void registerInputCallbacks () {
		glfwSetKeyCallback(handle, this::onKeyPress);

		glfwSetMouseButtonCallback(handle, this::onMouseButton);

		glfwSetCursorPosCallback(handle, this::onCursorMove);

		glfwSetScrollCallback(handle, (window, xoffset, yoffset) -> {
			if (mouseScrollCallback != null) {
				mouseScrollCallback.onMouseScroll(yoffset);
			}
		});
	}

	private void onMouseButton (long window, int button, int action, int mods) {
		if (button != GLFW_MOUSE_BUTTON_LEFT) return;

		if (action == GLFW_PRESS) {
			isDragging = true;
			double[] pos = new double[1];
			glfwGetCursorPos(window, pos, new double[1]);
			lastMouseX = lastMouseY = pos[0];
		} else if (action == GLFW_RELEASE) {
			isDragging = false;
		}
	}

	private void onCursorMove (long window, double xpos, double ypos) {
		if (isDragging && mouseMoveCallback != null) {
			mouseMoveCallback.onMouseMove(xpos - lastMouseX, ypos - lastMouseY);
			lastMouseX = xpos;
			lastMouseY = ypos;
		}
	}

	private void onKeyPress (long window, int key, int scancode, int action, int mods) {
		if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
			glfwSetWindowShouldClose(window, true);
		}

		if (keyCallback != null) {
			keyCallback.onKeyPress(key, action);
		}
	}

	private void centerWindow (MemoryStack stack) {
		IntBuffer pWidth = stack.mallocInt(1);
		IntBuffer pHeight = stack.mallocInt(1);
		glfwGetWindowSize(handle, pWidth, pHeight);

		GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		if (vidMode != null) {
			int centerX = (vidMode.width() - pWidth.get(0)) / 2;
			int centerY = (vidMode.height() - pHeight.get(0)) / 2;
			glfwSetWindowPos(handle, centerX, centerY);
		}
	}

	private void loop () {
		GL.createCapabilities();

		glClearColor(CLEAR_COLOR_R, CLEAR_COLOR_G, CLEAR_COLOR_B, CLEAR_COLOR_A);
		glEnable(GL_DEPTH_TEST);

		if (initCallback != null) {
			initCallback.run();
		}

		while (!glfwWindowShouldClose(handle)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			if (renderCallback != null) {
				renderCallback.run();
			}

			glfwSwapBuffers(handle);

			glfwPollEvents();
		}
	}

	private void cleanup () {
		glfwFreeCallbacks(handle);
		glfwDestroyWindow(handle);
		glfwTerminate();

		GLFWErrorCallback errorCallback = glfwSetErrorCallback(null);
		if (errorCallback != null) {
			errorCallback.free();
		}
	}
}
