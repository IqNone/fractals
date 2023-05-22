package mandelbrot.plotter;

public abstract class Plotter {

    protected final int[] buffer;

    protected int maxIterations;

    protected double zx;
    protected double zy;

    protected Plotter(int width, int height) {
        this.buffer = new int[width * height];
    }

    public abstract void plot(double minx, double miny, double step,
                               int sectionWidth, int sectionHeight,
                               int offset);

    public int[] getBuffer() {
        return buffer;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setZ(double x, double y) {
        this.zx = x;
        this.zy = y;
    }
}
