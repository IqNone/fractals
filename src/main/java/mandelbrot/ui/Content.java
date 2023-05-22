package mandelbrot.ui;

import mandelbrot.plotter.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Content extends JPanel {

    private static final int[] PALETTE = {
            new Color(66, 30, 15).getRGB(),
            new Color(25, 7, 26).getRGB(),
            new Color(9, 1, 47).getRGB(),
            new Color(4, 4, 73).getRGB(),
            new Color(0, 7, 100).getRGB(),
            new Color(12, 44, 138).getRGB(),
            new Color(24, 82, 177).getRGB(),
            new Color(57, 125, 209).getRGB(),
            new Color(134, 181, 229).getRGB(),
            new Color(211, 236, 248).getRGB(),
            new Color(241, 233, 191).getRGB(),
            new Color(248, 201, 95).getRGB(),
            new Color(255, 170, 0).getRGB(),
            new Color(204, 128, 0).getRGB(),
            new Color(153, 87, 0).getRGB(),
            new Color(106, 52, 3).getRGB(),
    };

    private final int width;
    private final int height;

    private final int threads;

    private double centerX;
    private double centerY;
    private double zoom;

    private int maxIterations;
    private boolean avx;
    private String set = "Mandelbrot";

    private final BufferedImage buffer;
    private final int[] iterationsBuffer;
    private final ExecutorService executorService;

    private final Plotter mandelbrotPlotter;
    private final Plotter mandelbrotAVXPlotter;
    private final Plotter juliaPlotter;
    private final Plotter juliaAVXPlotter;

    public Content(int width, int height, int threads, double centerX, double centerY, double zoom, int maxIterations) {
        super();

        this.width = width;
        this.height = height;
        this.threads = threads;
        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
        this.maxIterations = maxIterations;

        setPreferredSize(new Dimension(width, height));

        buffer = new BufferedImage(width, height, TYPE_INT_RGB);
        iterationsBuffer = new int[width * height];
        executorService = Executors.newFixedThreadPool(threads);

        mandelbrotPlotter = new MandelbrotPlotter(width, height);
        mandelbrotPlotter.setMaxIterations(maxIterations);

        mandelbrotAVXPlotter = new MandelbrotAVXPlotter(width, height);
        mandelbrotAVXPlotter.setMaxIterations(maxIterations);

        juliaPlotter = new JuliaPlotter(width, height);
        juliaPlotter.setMaxIterations(maxIterations);

        juliaAVXPlotter = new JuliaAVXPlotter(width, height);
        juliaAVXPlotter.setMaxIterations(maxIterations);
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(buffer, 0, 0, null);
    }

    public void update() throws InterruptedException {
        double minx = centerX - width / 2.0 / zoom;
        double miny = centerY - height / 2.0 / zoom;
        double step = 1.0 / zoom;

        int section = iterationsBuffer.length / threads;
        int sectionHeight = height / threads;
        double dh = sectionHeight / zoom;

        Plotter plotter = getPlotter();

        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; ++t) {
            int thread = t;
            executorService.submit(() -> {
                plotter.plot(minx, miny + thread * dh, step,
                        width, sectionHeight,
                        section * thread);
                latch.countDown();
            });
        }

        latch.await();

        int[] iterations = plotter.getBuffer();
        int[] pixels = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < iterations.length; ++i) {
            pixels[i] = iterations[i] == maxIterations ? 0 : PALETTE[iterations[i] % 16];
        }

        repaint();
    }

    private Plotter getPlotter() {
        if (set.equalsIgnoreCase("mandelbrot")) {
            return avx ? mandelbrotAVXPlotter : mandelbrotPlotter;
        } else {
            return avx ? juliaAVXPlotter : juliaPlotter;
        }
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        this.juliaPlotter.setMaxIterations(maxIterations);
        this.juliaAVXPlotter.setMaxIterations(maxIterations);
        this.mandelbrotPlotter.setMaxIterations(maxIterations);
        this.mandelbrotAVXPlotter.setMaxIterations(maxIterations);
    }

    public void setStartZ(double x, double y) {
        this.juliaPlotter.setZ(x, y);
        this.juliaAVXPlotter.setZ(x, y);
        this.mandelbrotPlotter.setZ(x, y);
        this.mandelbrotAVXPlotter.setZ(x, y);
    }

    public void setCenter(double x, double y) {
        centerX = x;
        centerY = y;
    }

    public void setAVX(boolean avx) {
        this.avx = avx;
    }

    public void setSet(String set) {
        this.set = set;
    }
}
