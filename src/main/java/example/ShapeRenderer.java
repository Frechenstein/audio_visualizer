package example;

import javax.swing.*;

import example.BasicGeometry.Float32Array;

import java.awt.*;
import java.util.Random;

public class ShapeRenderer extends JPanel {

    private final Float32Array[] shapeData;

    public ShapeRenderer() {
        // Level Settings setzen
        BasicGeometry.levelSettings[0] = 1;    // Nur eine Form
        BasicGeometry.levelSettings[1] = 1;    // Nur eine Iteration
        BasicGeometry.levelSettings[2] = 300;  // Komplexität / Punktezahl

        // Zufällige Form-ID wählen
        int shapeId = new Random().nextInt(4); // 0 = Triangle, 1 = Circle, 2 = Rektangle, 3 = Cross

        // Formdaten generieren
        shapeData = BasicGeometry.build(shapeId);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (shapeData == null || shapeData.length == 0 || shapeData[0] == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.CYAN);
        g2.setStroke(new BasicStroke(2));

        float[] vertices = shapeData[0].getData();

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int scale = 200;

        // Verbinde die Punkte (immer paarweise als Linien: x0,y0 -> x1,y1 -> ...)
        for (int i = 0; i < vertices.length - 2; i += 2) {
            int x1 = centerX + (int)(vertices[i] * scale);
            int y1 = centerY + (int)(vertices[i + 1] * scale);
            int x2 = centerX + (int)(vertices[i + 2] * scale);
            int y2 = centerY + (int)(vertices[i + 3] * scale);

            g2.drawLine(x1, y1, x2, y2);
        }

        // Verbinde letzten Punkt mit erstem für geschlossene Form
        if (vertices.length >= 4) {
            int x1 = centerX + (int)(vertices[vertices.length - 2] * scale);
            int y1 = centerY + (int)(vertices[vertices.length - 1] * scale);
            int x2 = centerX + (int)(vertices[0] * scale);
            int y2 = centerY + (int)(vertices[1] * scale);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Zufällige Geometrische Form");
        ShapeRenderer panel = new ShapeRenderer();
        frame.add(panel);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
