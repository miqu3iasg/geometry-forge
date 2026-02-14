package core;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	// Spherical coordinates
	private float radius;
	private float theta; // Vertical angle (polar)
	private float phi; // Horizontal angle (azimuthal)

	private static final float MIN_RADIUS = 1.5f;
	private static final float MAX_RADIUS = 10.0f;
	private static final float MIN_THETA = 0.1f;
	private static final float MAX_THETA = (float) Math.PI - 0.1f;


	private final Vector3f target;
	private final Vector3f up;

	public Camera (float initialRadius, float initialTheta, float initialPhi) {
		this.radius = initialRadius;
		this.theta = initialTheta;
		this.phi = initialPhi;
		this.target = new Vector3f(0, 0, 0);
		this.up = new Vector3f(0, 1, 0);
	}

	/**
	 * Rotate camera by mouse delta.
	 *
	 * @param deltaX      Horizontal mouse movement
	 * @param deltaY      Vertical mouse movement
	 * @param sensitivity Rotation speed multiplier
	 */
	public void rotate (float deltaX, float deltaY, float sensitivity) {
		phi += deltaX * sensitivity;
		theta += deltaY * sensitivity;

		theta = Math.max(MIN_THETA, Math.min(MAX_THETA, theta));

		phi = phi % (float) (2 * Math.PI);
	}

	/**
	 * Zoom in/out by changing radius.
	 *
	 * @param delta Scroll amount (positive = zoom in)
	 */
	public void zoom (float delta) {
		radius -= delta;
		radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
	}

	public Vector3f getPosition () {
		float x = radius * (float) Math.sin(theta) * (float) Math.cos(phi);
		float y = radius * (float) Math.cos(theta);
		float z = radius * (float) Math.sin(theta) * (float) Math.sin(phi);

		return new Vector3f(x, y, z);
	}

	public Matrix4f getViewMatrix () {
		return new Matrix4f().lookAt(getPosition(), target, up);
	}

	public float getRadius () { return radius; }

	public float getTheta () { return theta; }

	public float getPhi () { return phi; }

}
