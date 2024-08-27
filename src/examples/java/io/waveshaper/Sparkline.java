package io.waveshaper;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;

public class Sparkline {
    // ASCII characters for the chart, from low to high
    private static final char[] LEVELS = {'▁', '▂', '▄', '▆', '█'};

    private final int maxLength; // Maximum number of data points to retain
    private final double minValue; // Minimum value for scaling
    private final double maxValue; // Maximum value for scaling
    private final Deque<Double> dataPoints; // Deque to store data points

    // Constructor to initialize the sparkline with a fixed length, min, and max
    public Sparkline(int maxLength, double minValue, double maxValue) {
        this.maxLength = maxLength;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.dataPoints = new ArrayDeque<>(maxLength); // Initialize with ArrayDeque for efficient access
    }

    // Method to add a data point and maintain the fixed length
    public void addDataPoint(double value) {
        if (dataPoints.size() >= maxLength) {
            dataPoints.pollFirst(); // Remove the oldest data point to maintain fixed length
        }
        dataPoints.addLast(value); // Add new data point to the end
    }

    // Method to generate the ASCII sparkline
    public String render() {
        StringBuilder sparkline = new StringBuilder();
        for (double value : dataPoints) {
            // Scale the value to the LEVELS array index range
            int index = (int) ((value - minValue) / (maxValue - minValue) * (LEVELS.length - 1));
            index = Math.max(0, Math.min(index, LEVELS.length - 1)); // Ensure index is within bounds
            sparkline.append(LEVELS[index]);
        }
        return sparkline.toString();
    }

    // Method to clear the terminal (using ANSI escape codes)
    public static void clearTerminal() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Method to print the sparkline with terminal clearing
    public void print() {
        clearTerminal();
        System.out.println(this.render());
    }

    // Getters for the current state of the sparkline (optional)
    public int getMaxLength() {
        return maxLength;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public List<Double> getDataPoints() {
        return new ArrayList<>(dataPoints); // Convert to List and return a copy to maintain encapsulation
    }
}
