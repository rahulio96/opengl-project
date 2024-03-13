package csc133;
import org.joml.Vector3f;

public class spot {
    public static int vps = 4, fpv = 2, ips = 6; // vertices per square, float per vertices, indices per square
    public static int MAX_ROWS = 20, MAX_COLS = 18; // rows and cols for the square matrix
    public static int offset = 20, length = 30, padding = 10;
    public static int WIN_WIDTH = offset * 2 + MAX_COLS * (length + padding) - padding,
            WIN_HEIGHT = offset * 2 + MAX_ROWS * (length + padding) - padding;
    public static float red = 0.0f, green = 0.0f, blue = 1.0f, alpha = 1.0f; // background color
    public static final Vector3f ALIVE = new Vector3f(0.0f, 1.0f, 0.0f);
    public static final Vector3f DEAD = new Vector3f(1.0f, 0.0f, 0.0f); // "vector render color" for square
    public static final float FRUSTUM_LEFT = 0.0f,   FRUSTUM_RIGHT = (float)WIN_WIDTH,
            FRUSTUM_BOTTOM = 0.0f, FRUSTUM_TOP = (float)WIN_HEIGHT, Z_NEAR = 0.0f, Z_FAR = 10.0f;

}
