package geometry;

import java.util.Random;

public class BasicGeometry {
    private static final double[] levelSettings = new double[20];
    private static Float32Array[] lastData = null;
    private static final Random rand = new Random();

    public static Float32Array[] build(int id) {
        return myBuild(id);
    }

    private static Float32Array[] myBuild(int id) {
        int numSubsets = (int) Math.floor(levelSettings[0]);

        if (lastData == null) {
            lastData = new Float32Array[numSubsets];
        }

        return ShapeGeometry(id, lastData);
    }

    private static Float32Array[] ShapeGeometry(int levelId, Float32Array[] reuse) {
        if (reuse == null) {
            return null;
        }

        double startScale = 0.6 + rand.nextDouble() * 0.8;
        double endScale = startScale - rand.nextDouble() * 0.5 * (rand.nextBoolean() ? -1 : 1);
        double stepSize = (startScale - endScale) / levelSettings[1];

        for (int s = 0; s < (int) levelSettings[1]; s++) {
            double tscale = startScale + stepSize * s;
            reuse[s] = MakeLevelData(levelId, tscale, reuse[s]);
        }

        return reuse;
    }

    private static Float32Array MakeLevelData(int levelId, double scale, Float32Array arr) {
        switch (levelId % 4) {
            case 1:
                return Circle(scale, arr);
            case 2:
                return Rektangle(scale, arr);
            case 3:
                return Cross(scale, arr);
            default:
                return Triangle(scale, arr);
        }
    }

    private static Float32Array Triangle(double scale, Float32Array arr) {
        int numVert = (int) Math.floor(levelSettings[2] / 100);
        int rVert = numVert - (numVert % 3);
        return arr != null ? arr : new Float32Array(rVert);
    }

    private static Float32Array Circle(double scale, Float32Array arr) {
        int numVert = (int) Math.floor(levelSettings[2] / 100);
        return arr != null ? arr : new Float32Array(numVert);
    }

    private static Float32Array Rektangle(double scale, Float32Array arr) {
        int numVert = (int) Math.floor(levelSettings[2] / 100);
        int rVert = numVert - (numVert % 4);
        return arr != null ? arr : new Float32Array(rVert);
    }

    private static Float32Array Cross(double scale, Float32Array arr) {
        int numVert = (int) Math.floor(levelSettings[2] / 100);
        int rVert = numVert - (numVert % 12);
        return arr != null ? arr : new Float32Array(rVert);
    }

    // Mocked Float32Array to resemble JavaScript Typed Array
    static class Float32Array {
        private final float[] data;

        public Float32Array(int size) {
            this.data = new float[size];
        }

        public float[] getData() {
            return data;
        }
    }
}

