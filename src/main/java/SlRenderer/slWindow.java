
package SlRenderer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import static csc133.spot.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class slWindow {
    private static GLFWKeyCallback keyCallback;
    private static GLFWFramebufferSizeCallback fbCallback;

    private static long window = -1;
    private static final int WIN_POS_X = 30, WIN_POS_Y = 90;

    // Creates and returns the glfw window
    public static long getWindow() {
        if (window == -1) {
            GLFWErrorCallback errorCallback;
            glfwSetErrorCallback(errorCallback =
                    GLFWErrorCallback.createPrint(System.err));
            if (!glfwInit())
                throw new IllegalStateException("Unable to initialize GLFW");
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
            glfwWindowHint(GLFW_SAMPLES, 8);
            window = glfwCreateWindow(WIN_WIDTH, WIN_HEIGHT, "CSC 133", NULL, NULL);

            if (window == NULL)
                throw new RuntimeException("Failed to create the GLFW window");
            glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
                @Override
                public void invoke(long window, int key, int scancode, int action, int
                        mods) {
                    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                        glfwSetWindowShouldClose(window, true);
                }
            });
            glfwSetFramebufferSizeCallback(window, fbCallback = new
                    GLFWFramebufferSizeCallback() {
                        @Override
                        public void invoke(long window, int w, int h) {
                            if (w > 0 && h > 0) {
                                WIN_WIDTH = w;
                                WIN_HEIGHT = h;
                            }
                        }
                    });
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, WIN_POS_X, WIN_POS_Y);
            glfwMakeContextCurrent(window);
            int VSYNC_INTERVAL = 1;
            glfwSwapInterval(VSYNC_INTERVAL);
            glfwShowWindow(window);
        }
        return window;
    }

    // Overloading function
    public static long getWindow(int w, int h) {
        WIN_WIDTH = w;
        WIN_HEIGHT = h;
        return getWindow();
    }

    static void destroyWindow(long window) {
        glfwDestroyWindow(window);
        keyCallback.free();
        fbCallback.free();
    }

}
