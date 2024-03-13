package SlRenderer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static csc133.spot.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform3f;

public class slSingleBatchRenderer {
    private static long window;
    private static final int OGL_MATRIX_SIZE = 16;
    // call glCreateProgram() here - we have no gl-context here
    private static int shader_program;
    private static Matrix4f viewProjMatrix = new Matrix4f();
    private static FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);
    private static int vpMatLocation = 0, renderColorLocation = 1;
    private static int coordinatesPerVertex = 2;

    public void render() {
        window = slWindow.getWindow();
        try {
            renderLoop();
            slWindow.destroyWindow(window);
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
        glClearColor(red, green, blue, alpha);
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
                "uniform vec3 renderColorLocation;" +
                        "void main(void) {" +
                        " gl_FragColor = vec4(renderColorLocation, 1.0);" +
                        "}");
        glCompileShader(fs);
        glAttachShader(shader_program, fs);
        glLinkProgram(shader_program);
        glUseProgram(shader_program);
        vpMatLocation = glGetUniformLocation(shader_program, "viewProjMatrix");
        return;
    } // void initOpenGL()

    float[] getVertices(int MAX_ROWS, int MAX_COLS, int vps, int fpv, int offset, int length, int padding) {
        float[] vertices = new float[MAX_ROWS * MAX_COLS * vps * fpv];


        int xmin = offset;
        int xmax = xmin + length;
        int ymax = WIN_HEIGHT - offset;
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

    // generate all the indices for the matrix of squares
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

            glVertexPointer(coordinatesPerVertex, GL_FLOAT, 0, 0L);

            slCamera my_cam = new slCamera();
            my_cam.setProjectionOrtho();
            viewProjMatrix = my_cam.getProjectionMatrix();

            glUniformMatrix4fv(vpMatLocation, false,
                    viewProjMatrix.get(myFloatBuffer));

            for (int i = 0; i < vertices.length; i+=vps) {
                Random rand = new Random();
                int randInt = rand.nextInt(100);
                if (randInt < 50) {
                    glUniform3f(renderColorLocation, VEC_RC.x, VEC_RC.y, VEC_RC.z);
                } else {
                    glUniform3f(renderColorLocation, 1f, 0f, 0f);
                }
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                glDrawElements(GL_TRIANGLES, ips, GL_UNSIGNED_INT, (long) i * ips);
            }
            glfwSwapBuffers(window);
        }
    } // renderObjects
}
