package utility;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Simple timer utility for checking elapsed time.
 */
public class Timer {
    private double startTime;
    private final double duration;
    private boolean triggered;

    /**
     * Creates a timer with a given duration.
     *
     * @param durationInSeconds The duration in seconds.
     */
    public Timer(double durationInSeconds) {
        this.duration = durationInSeconds;
        this.startTime = glfwGetTime();
        this.triggered = false;
    }

    /**
     * Checks if the timer has elapsed.
     *
     * @return True if the duration has passed and it hasn't been marked as triggered yet.
     */
    public boolean isElapsed() {
        return !triggered && glfwGetTime() - startTime >= duration;
    }

    /**
     * Resets the timer to start from the current time again.
     */
    public void reset() {
        this.startTime = glfwGetTime();
        this.triggered = false;
    }

    /**
     * Marks the timer as triggered so `isElapsed` won't return true again.
     */
    public void markTriggered() {
        this.triggered = true;
    }
}
