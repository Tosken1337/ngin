package com.tosken.photoviewer.rendering.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.camera.Vector3Mover;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public class MovingCamera {
    private Vector3Mover lookAtMover = new Vector3Mover();

    private Vector3Mover positionMover = new Vector3Mover();

    public MovingCamera(final Vector3f position, final Vector3f lookAt) {
        lookAtMover.maxDirectAcceleration = 5.0f;
        lookAtMover.maxDirectDeceleration = 5.0f;
        positionMover.maxDirectAcceleration = 5.0f;
        positionMover.maxDirectDeceleration = 5.0f;

        lookAtMover.current.set(lookAt);
        lookAtMover.target.set(lookAt);
        positionMover.current.set(position);
        positionMover.target.set(position);
    }

    /**
     * Apply the camera's view transformation to the given matrix by post-multiplying it.
     *
     * @param mat
     *          the matrix which gets post-multiplied by the camera's view transformation matrix
     * @return the supplied matrix
     */
    public Matrix4f viewMatrix(Matrix4f mat) {
        /*
         * Explanation:
         * - First, translate the center position back to the origin, so that we can rotate about it
         * - Then, rotate first about Y and then about X (this will ensure that "right" is always parallel to the world's XZ-plane)
         * - Next, translate the camera back by its distance to the center (the radius of the arcball)
         */
        /*return mat.translate(0, 0, (float) -zoomMover.current)
                .rotateX((float) betaMover.current)
                .rotateY((float) alphaMover.current)
                .translate(-centerMover.current.x, -centerMover.current.y, -centerMover.current.z);*/

        return new Matrix4f().lookAt(positionMover.current, lookAtMover.current, new Vector3f(0f, 1f, 0f)).mul(mat);
    }

    public void setLookAt(float x, float y, float z) {
        lookAtMover.target.set(x, y, z);
    }

    public void update(float elapsedTimeInSeconds) {
        lookAtMover.update(elapsedTimeInSeconds);
        positionMover.update(elapsedTimeInSeconds);
    }
}
