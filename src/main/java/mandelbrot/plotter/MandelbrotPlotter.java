package mandelbrot.plotter;

public class MandelbrotPlotter extends Plotter {

    public MandelbrotPlotter(int width, int height) {
        super(width, height);
    }

    @Override
    public void plot(double minx, double miny, double step, int sectionWidth, int sectionHeight, int offset) {
        double y = miny;

        int iteration = offset;
        for (int i = 0; i < sectionHeight; ++i) {
            double x = minx;
            for (int j = 0; j < sectionWidth; ++j) {
                buffer[iteration++] = iterations(x, y);

                x += step;
            }
            y += step;
        }
    }

    protected int iterations(double x, double y) {
        double zx = this.zx;
        double zy = this.zy;
        double z2x = zx * zx;
        double z2y = zy * zy;
        int iterations = 0;

        while (z2x + z2y <= 4 && iterations < maxIterations) {
            zy = 2 * zx * zy + y;
            zx = z2x - z2y + x;
            z2x = zx * zx;
            z2y = zy * zy;

            iterations++;
        }

        return iterations;
    }
}
