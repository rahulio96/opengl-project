package csc133;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class slCamera {
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private float f_left;
    private float f_right;
    private float f_bottom;
    private float f_top;
    private float f_near;
    private float f_far;
    public Vector3f defaultLookFrom = new Vector3f(0f, 0f, 10f);
    public Vector3f defaultLookAt = new Vector3f(0f, 0f, -1.0f);
    public Vector3f defaultUpVector = new Vector3f(0f, 1.0f, 0f);
    private Vector3f curLookFrom;
    private Vector3f curLookAt;
    private Vector3f curUpVector;

    private void setCamera() {
        this.viewMatrix = new Matrix4f().identity();
    }

    public slCamera(Vector3f camera_position) {
        setCamera();
        this.viewMatrix.lookAt(curLookFrom, curLookAt.add(camera_position), curUpVector);
        this.projectionMatrix = new Matrix4f().identity();
    }

    public slCamera() {
        setCamera();
        this.projectionMatrix = new Matrix4f().identity();
    }

    public void setProjectionOrtho() {
        this.projectionMatrix.setOrtho(f_left, f_right, f_bottom, f_top, f_near, f_far);
    }

    public void setProjectionOrtho(float left, float right, float bottom, float top, float near, float far) {
        this.projectionMatrix.setOrtho(left, right, bottom, top, near, far);
    }

    public Matrix4f getViewMatrix() {
        setCamera();
        return this.viewMatrix.lookAt(curLookFrom, curLookAt.add(defaultLookFrom), curUpVector); // return this
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }
}
