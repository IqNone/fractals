package mandelbrot.plotter;

public class JuliaPlotter extends MandelbrotPlotter {
    public JuliaPlotter(int width, int height) {
        super(width, height);
    }

    protected int iterations(double x, double y) {
        double zx = x;
        double zy = y;
        double z2x = zx * zx;
        double z2y = zy * zy;
        int iterations = 0;

        while (z2x + z2y <= 4 && iterations < maxIterations) {
            zy = 2 * zx * zy + this.zy;
            zx = z2x - z2y + this.zx;
            z2x = zx * zx;
            z2y = zy * zy;

            iterations++;
        }

        return iterations;
    }
}
