package div;

import java.util.Random;

public class BasicGeometry {
    public static final double[] levelSettings = new double[20];
    private static Float32Array[] lastData = null;

    public static class Float32Array {
        private final float[] data;

        public Float32Array(int size) {
            this.data = new float[size];
        }

        public Float32Array(float[] initialData) {
            this.data = initialData;
        }

        public float[] getData() {
            return data;
        }

        public int length() {
            return data.length;
        }
    }

    public static Float32Array[] build(int id) {
        return myBuild(id);
    }

    private static Float32Array[] myBuild(int id) {
        int numSubsets = (int) levelSettings[0];
        if (lastData == null || lastData.length != numSubsets) {
            lastData = new Float32Array[numSubsets];
        }
        return lastData = ShapeGeometry(id, lastData);
    }

    private static Float32Array[] ShapeGeometry(int levelId, Float32Array[] reuse) {
        if (reuse == null) return null;

        Random rand = new Random();
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

    private static Float32Array Triangle(double lscale, Float32Array arr) {
        int numVert = (int) (levelSettings[2] / 100);
        int rVert = numVert - (numVert % 3);
        if (arr == null || arr.length() != rVert) {
            return new Float32Array(rVert);
        }
        return arr;
    }

    private static Float32Array Circle(double lscale, Float32Array arr) {
        int numVert = (int) (levelSettings[2] / 100);
        if (arr == null || arr.length() != numVert) {
            return new Float32Array(numVert);
        }
        return arr;
    }

    private static Float32Array Rektangle(double lscale, Float32Array arr) {
        int numVert = (int) (levelSettings[2] / 100);
        int rVert = numVert - (numVert % 4);
        if (arr == null || arr.length() != rVert) {
            return new Float32Array(rVert);
        }
        return arr;
    }

    private static Float32Array Cross(double lscale, Float32Array arr) {
        int numVert = (int) (levelSettings[2] / 100);
        int rVert = numVert - (numVert % 12);
        if (arr == null || arr.length() != rVert) {
            return new Float32Array(rVert);
        }
        return arr;
    }
} 