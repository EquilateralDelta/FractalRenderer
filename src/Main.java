import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    double zoom = 3;
    Vector2f rotation = new Vector2f(0, 0);

    public Main()
    {
        try {
            Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(800, 600));
            Display.create(new PixelFormat(0, 8, 0, 4));
            Display.setVSyncEnabled(true);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        GL11.glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        double scale = Display.getDisplayMode().getWidth() / 2;
        GL11.glOrtho(-scale, scale, scale * (6f / 8), -scale * (6f / 8), .1f, 500);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GLU.gluLookAt(0, 0, 50, 0, 0, 0, 0, 1, 0);

        int shaderId = ShaderLoader.LoadShaders();
        Mouse.setGrabbed(true);

        while(!Display.isCloseRequested()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            HandleInput();

            Vector3f eyePosition = new Vector3f();
            eyePosition.y = (float)(zoom * Math.sin(rotation.x));
            float xPluszDist = (float)(zoom * Math.cos(rotation.x));

            eyePosition.x = (float)(xPluszDist * Math.cos(rotation.y));
            eyePosition.z = (float)(xPluszDist * Math.sin(rotation.y));
            GL20.glUniform3f(GL20.glGetUniformLocation(shaderId, "cameraPosition"), eyePosition.x, eyePosition.y, eyePosition.z);

            GL20.glUniform2f(GL20.glGetUniformLocation(shaderId, "resolution"), 780, 580);
            GL20.glUseProgram(shaderId);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-390, -290);
            GL11.glVertex2f(-390, 290);
            GL11.glVertex2f(390, 290);
            GL11.glVertex2f(390, -290);
            GL11.glEnd();

            Display.update();
            Display.sync(100);
        }
    }

    double zoomSpeed = .05;
    double rotationSpeed = Math.PI / 180;
    double threshold = 10e-5;
    double mouseSensitivity = .15;
    double mouseZoomSensitivity = .1;
    void HandleInput(){
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Display.destroy();
            System.exit(1);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_UP))
            zoom -= zoomSpeed;
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
            zoom += zoomSpeed;

        if (Keyboard.isKeyDown(Keyboard.KEY_W))
            rotation.x += rotationSpeed;
        if (Keyboard.isKeyDown(Keyboard.KEY_S))
            rotation.x -= rotationSpeed;
        if (Keyboard.isKeyDown(Keyboard.KEY_A))
            rotation.y += rotationSpeed;
        if (Keyboard.isKeyDown(Keyboard.KEY_D))
            rotation.y -= rotationSpeed;

        float mouseDifX = Mouse.getX() - Display.getWidth() / 2,
                mouseDifY = Mouse.getY() - Display.getHeight() / 2;
        rotation.x += mouseDifY * mouseSensitivity * rotationSpeed;
        rotation.y -= mouseDifX * mouseSensitivity * rotationSpeed;
        Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);

        zoom -= Mouse.getDWheel() * zoomSpeed * mouseZoomSensitivity;

        if (zoom <= .01)
            zoom = 1;
        if (rotation.x > Math.PI / 2 - threshold)
            rotation.x = (float)(Math.PI / 2 - threshold);
        if (rotation.x < -(Math.PI / 2 - threshold))
            rotation.x = -(float)(Math.PI / 2 - threshold);
    }

    public static void main(String[] args){
        new Main();
    }
}
