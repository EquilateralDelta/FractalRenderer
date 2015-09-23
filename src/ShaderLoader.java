import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ShaderLoader {
    static int LoadShaders()
    {
        int programId = GL20.glCreateProgram();

        int vertexId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexId, ReadFile("screen.vert"));
        GL20.glCompileShader(vertexId);
        if (GL20.glGetShaderi(vertexId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
            System.out.println(GL20.glGetShaderInfoLog(vertexId, GL20.GL_INFO_LOG_LENGTH));
        GL20.glAttachShader(programId, vertexId);

        int fragmentId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentId, ReadFile("screen.frag"));
        GL20.glCompileShader(fragmentId);
        if (GL20.glGetShaderi(fragmentId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
            System.out.println(GL20.glGetShaderInfoLog(fragmentId, GL20.GL_INFO_LOG_LENGTH));
        GL20.glAttachShader(programId, fragmentId);

        GL20.glLinkProgram(programId);
        GL20.glValidateProgram(programId);

        return programId;
    }

    static String ReadFile(String fileName)
    {
        StringBuilder result = new StringBuilder("");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null)
                result.append(line).append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
