package utility;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Timer {
    private double startTime;
    private final double duration;
    private boolean triggered;

    public Timer(double durationInSeconds) {
        this.duration = durationInSeconds;
        this.startTime = glfwGetTime();
        this.triggered = false;
    }

    public boolean isElapsed() {
        return !triggered && glfwGetTime() - startTime >= duration;
    }

    public void reset() {
        this.startTime = glfwGetTime();
        this.triggered = false;
    }

    public void markTriggered() {
        this.triggered = true;
    }
}
