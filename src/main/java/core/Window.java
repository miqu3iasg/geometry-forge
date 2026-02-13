package core;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 600;
	private static final String WINDOW_TITLE = "Geometry Forge";

	private long handle;
	private Runnable renderCallback;
	private Runnable initCallback;

	public void setRenderCallback (Runnable callback) {
		this.renderCallback = callback;
	}

	public void setInitCallback (Runnable callback) {
		this.initCallback = callback;
	}

	public void run () {
		init();
		loop();
		cleanup();
	}

	private void init () {
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		handle = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, NULL, NULL);

		if (handle == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true);
		});

		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			glfwGetWindowSize(handle, pWidth, pHeight);

			GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			glfwSetWindowPos(handle, (vidMode.width() - pWidth.get(0) / 2), (vidMode.height() - pHeight.get(0) / 2));

			glfwMakeContextCurrent(handle);

			glfwSwapInterval(1); // Enable v-sync

			glfwShowWindow(handle);
		}
	}

	private void loop () {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		glClearColor(0.1f, 0.1f, 0.12f, 1.0f);
		glEnable(GL_DEPTH_TEST);

		if (initCallback != null) {
			initCallback.run();
		}

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (!glfwWindowShouldClose(handle)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

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
		glfwSetErrorCallback(null).free();
	}

	public static int getWidth () {
		return WINDOW_WIDTH;
	}

	public static int getHeight () {
		return WINDOW_HEIGHT;
	}
}
