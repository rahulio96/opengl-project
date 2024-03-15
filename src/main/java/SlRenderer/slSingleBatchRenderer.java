package SlRenderer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static SlRenderer.slKeyListener.isKeyPressed;
import static SlRenderer.slKeyListener.resetKeypressEvent;
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
    private static slGoLBoard GoLBoard = new slGoLBoardLive(MAX_ROWS, MAX_COLS);

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
        boolean delayFrame = false;
        boolean haltRendering = false;
        glfwSetKeyCallback(window, slKeyListener::keyCallback);
        while (!glfwWindowShouldClose(window)) {
            if (delayFrame) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            glfwPollEvents();
            // Delay framerate
            if (isKeyPressed(GLFW_KEY_D)) {
                delayFrame = !delayFrame;
                resetKeypressEvent(GLFW_KEY_D);
            }
            // Stop rendering
            if (isKeyPressed(GLFW_KEY_H)) {
                haltRendering = true;
                resetKeypressEvent(GLFW_KEY_H);
            }
            // Resume Rendering
            if (isKeyPressed(GLFW_KEY_SPACE)) {
                haltRendering = false;
                resetKeypressEvent(GLFW_KEY_SPACE);
            }
            // Usage (Help)
            if (isKeyPressed(GLFW_KEY_SLASH) && isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
                System.out.println("Print this text --> ?");
                System.out.println("Toggle 500 ms frame delay --> d");
                System.out.println("Toggle frame rate display --> f");
                System.out.println("Halt the engine --> h");
                System.out.println("Reset board --> r");
                System.out.println("Resume engine --> SPACE");
                System.out.println("Save engine state to a file --> s");
                System.out.println("Load engine state from a file --> l");
                System.out.println("Exit application --> ESC");
                resetKeypressEvent(GLFW_KEY_SLASH);
                resetKeypressEvent(GLFW_KEY_LEFT_SHIFT);
            }
            // Close window
            if (isKeyPressed(GLFW_KEY_ESCAPE)) {
                glfwSetWindowShouldClose(window, true);
                resetKeypressEvent(GLFW_KEY_ESCAPE);
            }
            // Reset the board randomly
            if (isKeyPressed(GLFW_KEY_R)) {
                GoLBoard = new slGoLBoardLive(MAX_ROWS, MAX_COLS);
                resetKeypressEvent(GLFW_KEY_R);
            }
            // Load board file
            if (isKeyPressed(GLFW_KEY_L)) {
                haltRendering = true;
                JFileChooser fc = new JFileChooser();

                resetKeypressEvent(GLFW_KEY_L);
                haltRendering = false;
            }

            int vertexCount = 0;
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
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            for (int row = 0; row < MAX_ROWS; row++) {
                for (int col = 0; col < MAX_COLS; col++) {
                    if (GoLBoard.getLiveCellArray()[row][col]) {
                        glUniform3f(renderColorLocation, ALIVE.x, ALIVE.y, ALIVE.z);
                    } else {
                        glUniform3f(renderColorLocation, DEAD.x, DEAD.y, DEAD.z);
                    }
                    glDrawElements(GL_TRIANGLES, ips, GL_UNSIGNED_INT, (long) vertexCount * ips);
                    vertexCount+=vps;
                }
            }
            // Save here before so current frame is saved!
            // Safe board to file
            if (isKeyPressed(GLFW_KEY_S)) {
                haltRendering = true;
                boolean[][] curCellArray = GoLBoard.getLiveCellArray();
                String fileName = JOptionPane.showInputDialog("Please type the file's name");
                if (!fileName.contains(".ca")) {
                    fileName += ".ca";
                }
                try (FileWriter writer = new FileWriter(fileName)) {
                    writer.write(MAX_ROWS+"\n");
                    writer.write(MAX_COLS+"\n");
                    for (int r = 0; r < MAX_ROWS; r++) {
                        String rowString = "";
                        for (int c = 0; c < MAX_COLS; c++) {
                            if (curCellArray[r][c]) {
                                rowString += "1 ";
                            } else {
                                rowString += "0 ";
                            }
                        }
                        writer.write(rowString+"\n");
                    }
                    JOptionPane.showMessageDialog(null, "GoL Board saved to: " + fileName);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error saving string to file: " + e.getMessage());
                }
                resetKeypressEvent(GLFW_KEY_S);
                haltRendering = false;
            }
            if (!haltRendering) {
                GoLBoard.updateNextCellArray();
                GoLBoard.copyLiveToNext();
                glfwSwapBuffers(window);
            }
        }
    } // renderObjects
}
