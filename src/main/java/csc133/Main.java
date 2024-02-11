package csc133;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Main {
    static int WIN_WIDTH = 900, WIN_HEIGHT = 900;
    static long window = csc133.slWindow.getWindow(WIN_WIDTH, WIN_HEIGHT);
    private static final int OGL_MATRIX_SIZE = 16;
    // call glCreateProgram() here - we have no gl-context here
    int shader_program;
    Matrix4f viewProjMatrix = new Matrix4f();
    FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);
    int vpMatLocation = 0, renderColorLocation = 0;
    public static void main(String[] args) {
        new Main().render();
    } // public static void main(String[] args)

    void render() {
        try {
            csc133.slWindow.initGLFWindow(window);
            renderLoop();
            csc133.slWindow.destroyWindow(window);
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    } // void render()

    void renderLoop() {
        glfwPollEvents();
        initOpenGL();
        renderObjects();
        /* Process window messages in the main thread */
        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
        }
    } // void renderLoop()

    void initOpenGL() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);
        glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        this.shader_program = glCreateProgram();
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "void main(void) {" +
                        " gl_Position = viewProjMatrix * gl_Vertex;" +
                        "}");
        glCompileShader(vs);
        glAttachShader(shader_program, vs);
        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
                "uniform vec3 color;" +
                        "void main(void) {" +
                        " gl_FragColor = vec4(0.7f, 0.5f, 0.1f, 1.0f);" +
                        "}");
        glCompileShader(fs);
        glAttachShader(shader_program, fs);
        glLinkProgram(shader_program);
        glUseProgram(shader_program);
        vpMatLocation = glGetUniformLocation(shader_program, "viewProjMatrix");
        return;
    } // void initOpenGL()

    int vps = 4; // vertices per square
    int fpv = 2; // float per vertices
    int ips = 6; // indices per square
    int MAX_ROWS = 7; // rows for the square matrix
    int MAX_COLS = 5; // cols for the square matrix
    int offset = 10;
    int padding = 5;
    int length = 10;

    float[] getVertices(int MAX_ROWS, int MAX_COLS, int vps, int fpv, int offset, int length, int padding) {
        float[] vertices = new float[MAX_ROWS * MAX_COLS * vps * fpv];

        int xmin = offset;
        int xmax = xmin + length;
        int ymax = 200 - offset;
        int ymin = ymax - length;
        int index = 0;

        for (int row = 0; row < MAX_ROWS; row++) {
            for (int col = 0; col < MAX_COLS; col++) {
                vertices[index++] = xmin;
                vertices[index++] = ymin;
                vertices[index++] = xmax;
                vertices[index++] = ymin;
                vertices[index++] = xmax;
                vertices[index++] = ymax;
                vertices[index++] = xmin;
                vertices[index++] = ymax;

                xmin = xmax + padding;
                xmax = xmin + length;
            }
            xmin = offset;
            xmax = xmin + length;
            ymax = ymin - padding;
            ymin = ymax - length;
        }
        return vertices;
    }

    int[] getIndices(int MAX_ROWS, int MAX_COLS, int ips, int vps) {
        int[] indices =  new int[MAX_ROWS * MAX_COLS * ips];

        int index = 0;
        int v_index = 0;

        while (index < indices.length) {
            indices[index++] = v_index;
            indices[index++] = v_index + 1;
            indices[index++] = v_index + 2;
            indices[index++] = v_index;
            indices[index++] = v_index + 2;
            indices[index++] = v_index + 3;

            v_index += vps;
        }

        return indices;
    }

    void renderObjects() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            int vbo = glGenBuffers();
            int ibo = glGenBuffers();

            float[] vertices = getVertices(MAX_ROWS, MAX_COLS, vps, fpv, offset, length, padding);
            int[] indices = getIndices(MAX_ROWS, MAX_COLS, ips, vps);

            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.
                    createFloatBuffer(vertices.length).
                    put(vertices).flip(), GL_STATIC_DRAW);

            glEnableClientState(GL_VERTEX_ARRAY);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils.
                    createIntBuffer(indices.length).
                    put(indices).flip(), GL_STATIC_DRAW);

            glVertexPointer(2, GL_FLOAT, 0, 0L);

            viewProjMatrix.setOrtho(0, 200, 0, 200, 0, 10);

            glUniformMatrix4fv(vpMatLocation, false,
                    viewProjMatrix.get(myFloatBuffer));

            glUniform3f(renderColorLocation, 1.0f, 0.498f, 0.153f);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

            int VTD = 6; // need to process 6 Vertices To Draw 2 triangles

            glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0L);
            glfwSwapBuffers(window);

        }
    } // renderObjects
}
