package mandelbrot;

import mandelbrot.ui.Content;
import mandelbrot.ui.Header;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Mandelbrot extends JFrame implements MouseListener, MouseMotionListener {

    private static final float DEFAULT_CENTER_X = -0.5F;
    private static final float DEFAULT_CENTER_Y = 0.0F;
    private static final float DEFAULT_WIDTH = 4.0F;
    private static final int DEFAULT_MAX_ITERATIONS = 100;

    private final Header header;
    private final Content content;

    private final int width;
    private final int height;

    private double zoom;
    private double centerX = DEFAULT_CENTER_X;
    private double centerY = DEFAULT_CENTER_Y;

    private int dragStartX;
    private int dragStartY;

    public Mandelbrot(int width, int height, int threads) {
        super("Mandelbrot");

        this.width = width;
        this.height = height;

        zoom = (double) width / DEFAULT_WIDTH;

        this.header = new Header(width, centerX, centerY, zoom, DEFAULT_MAX_ITERATIONS);
        this.content = new Content(width, height, threads, centerX, centerY, zoom, DEFAULT_MAX_ITERATIONS);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(content, BorderLayout.CENTER);
        setResizable(false);
        pack();
        setVisible(true);

        this.header.onMaxIterationsChanged(content::setMaxIterations);
        this.header.onStartZChanged(content::setStartZ);
        this.header.onAVXChanged(content::setAVX);
        this.header.onSetChanged(content::setSet);

        this.content.addMouseListener(this);
        this.content.addMouseMotionListener(this);
    }

    private void zoom(double rate, int sx, int sy) {
        centerX += (1  - 1 / rate) / zoom * (sx - width / 2.0);
        centerY += (1  - 1 / rate) / zoom * (sy - height / 2.0);
        header.setCenter(centerX, centerY);
        content.setCenter(centerX, centerY);

        zoom *= rate;
        header.setZoom(zoom);
        content.setZoom(zoom);
    }

    public void update() throws InterruptedException {
        long frameStart = System.nanoTime();
        content.update();
        long frameEnd = System.nanoTime();
        float delta = (float) ((frameEnd - frameStart)) / 1_000_000_000;
        header.update(delta);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (header.zoomIn()) {
            zoom(1.2, e.getX(), e.getY());
        } else if (header.zoomOut()) {
            zoom(0.8, e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragStartX = e.getX();
        dragStartY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = dragStartX - e.getX();
        int dy = dragStartY - e.getY();

        centerX += dx / zoom;
        centerY += dy / zoom;

        header.setCenter(centerX, centerY);
        content.setCenter(centerX, centerY);

        dragStartX = e.getX();
        dragStartY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public static void main(String[] args) throws InterruptedException {
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Detected " + processors + ". Using 4x (" + 4 * processors + ") threads.");

        Mandelbrot mandelbrot = new Mandelbrot(1536, 864, 4 * processors);

        while (true) {
            mandelbrot.update();
            Thread.sleep(100);
        }
    }
}
