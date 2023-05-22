package mandelbrot.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Header extends JPanel {
    private double centerX;
    private double centerY;

    private double zoom;

    private final Stats stats;
    private final MaxIterationsInput maxIterationsInput;
    private final StartInput startInput;
    private final ZoomInput zoomInput;
    private final SetInput setInput;

    private final AVXSelector avxSelector;

    public Header(int contentWidth, double centerX, double centerY, double zoom, int maxIterations) {
        super();

        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;

        this.stats = new Stats(350, 40);
        this.maxIterationsInput = new MaxIterationsInput(215, 30, maxIterations);
        this.startInput = new StartInput(300, 40, 0, 0);
        this.zoomInput = new ZoomInput(80, 40);
        this.avxSelector = new AVXSelector(80, 40);
        this.setInput = new SetInput(150, 40);

        setPreferredSize(new Dimension(contentWidth, 40));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBackground(Color.BLACK);

        add(stats);
        add(createSeparator());
        add(zoomInput);
        add(createSeparator());
        add(maxIterationsInput);
        add(createSeparator());
        add(startInput);
        add(createSeparator());
        add(setInput);
        add(createSeparator());
        add(avxSelector);
    }

    private Component createSeparator() {
        JSeparator separator = new JSeparator();

        separator.setPreferredSize(new Dimension(20, 0));
        separator.setSize(new Dimension(20, 0));
        separator.setMaximumSize(new Dimension(20, 0));
        separator.setMinimumSize(new Dimension(20, 0));
        return separator;
    }

    public void setCenter(double x, double y) {
        centerX = x;
        centerY = y;
    }

    public void setZoom(double z) {
        zoom = z;
    }

    public void update(float delta) {
        stats.update(delta, centerX, centerY, zoom);
    }

    public void onMaxIterationsChanged(Consumer<Integer> callback) {
        maxIterationsInput.onValueChanged(callback);
    }

    public void onStartZChanged(BiConsumer<Double, Double> callback) {
        startInput.onValueChanged(callback);
    }

    public void onAVXChanged(Consumer<Boolean> callback) {
        avxSelector.onChanged(callback);
    }

    public void onSetChanged(Consumer<String> callback) {
        setInput.onChange(callback);
    }

    public boolean zoomIn() {
        return zoomInput.zoomIn();
    }

    public boolean zoomOut() {
        return zoomInput.zoomOut();
    }

    private static class Stats extends JPanel {
        private final int height;
        private final int fontSize = 14;

        private final BufferedImage buffer;

        private Stats(int width, int height) {
            super();

            this.height = height;
            setPreferredSize(new Dimension(width, height));
            setSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));

            buffer = new BufferedImage(width, height, TYPE_INT_RGB);
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(buffer, 0, 0, null);
        }

        public void update(float delta, double centerX, double centerY, double zoom) {
            Graphics2D g2d = buffer.createGraphics();
            g2d.setBackground(Color.BLACK);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.clearRect(0, 0, getWidth(), getHeight());

            Font font = g2d.getFont().deriveFont(Font.BOLD, fontSize);
            g2d.setFont(font);

            g2d.drawString(String.format("%6.4f (s) \t C: %,.2f, %,.2f \t Zoom: %6.3ex",
                            delta, centerX, centerY, zoom),
                    5, height - fontSize);

            repaint();
        }
    }

    private static class MaxIterationsInput extends JPanel {
        private final JSpinner maxIterations;

        private MaxIterationsInput(int width, int height, int maxIterations) {
            this.maxIterations = new JSpinner(new SpinnerNumberModel(100, 10, 100000, 10));
            this.maxIterations.setBackground(Color.BLACK);
            this.maxIterations.setValue(maxIterations);

            JLabel label = new JLabel("Max Iterations");
            label.setForeground(Color.LIGHT_GRAY);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14));

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            add(label);
            add(this.maxIterations);

            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(width, height));
            setSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));
        }

        public void onValueChanged(Consumer<Integer> callback) {
            maxIterations.addChangeListener(e -> callback.accept((Integer) maxIterations.getValue()));
        }
    }

    private static class StartInput extends JPanel {

        public static final int MIN = -2;
        public static final int MAX = 2;
        public static final int SIZE = 200;
        public static final int STEP = 10;

        private final JSlider x;
        private final JSlider y;

        public StartInput(int width, int height, int x, int y) {
            this.x = createSlider(x);
            this.y = createSlider(y);

            JLabel lx = new JLabel("Zx");
            lx.setForeground(Color.DARK_GRAY);
            lx.setFont(lx.getFont().deriveFont(Font.BOLD, 14));

            JLabel ly = new JLabel("Zy");
            ly.setForeground(Color.DARK_GRAY);
            ly.setFont(lx.getFont().deriveFont(Font.BOLD, 14));

            add(lx);
            add(this.x);
            add(ly);
            add(this.y);

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setPreferredSize(new Dimension(width, height));
            setSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));
        }

        private JSlider createSlider(int value) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, SIZE, value * SIZE / 2 + SIZE / 2);
            slider.setMajorTickSpacing(SIZE / 2);
            slider.setMinorTickSpacing(STEP);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setLabelTable(getLabels());
            return slider;
        }

        private Hashtable<Integer, JLabel> getLabels() {
            Hashtable<Integer, JLabel> hashtable = new Hashtable<>();
            for (int i = 0; i <= SIZE; i += SIZE / 2) {
                hashtable.put(i, new JLabel(String.valueOf(v(i))));
            }
            return hashtable;
        }

        public void onValueChanged(BiConsumer<Double, Double> callback) {
            x.addChangeListener(e -> callback.accept(v(x.getValue()), v(y.getValue())));
            y.addChangeListener(e -> callback.accept(v(x.getValue()), v(y.getValue())));
        }

        private double v(int x) {
            return ((double) x) / SIZE * (MAX - MIN) + MIN;
        }
    }

    static class ZoomInput extends JPanel {

        private final JToggleButton zoomIn;
        private final JToggleButton zoomOut;

        public ZoomInput(int width, int height) {
            zoomIn = new JToggleButton("+");
            zoomOut = new JToggleButton("-");

            ButtonGroup buttonGroup = new ButtonGroup();

            zoomIn.setSize(new Dimension(height - 4, height - 4));
            zoomIn.setPreferredSize(new Dimension(height - 4, height - 4));
            zoomIn.setMaximumSize(new Dimension(height - 4, height - 4));
            zoomIn.setMinimumSize(new Dimension(height - 4, height - 4));

            zoomOut.setSize(new Dimension(height - 4, height - 4));
            zoomOut.setPreferredSize(new Dimension(height - 4, height - 4));
            zoomOut.setMaximumSize(new Dimension(height - 4, height - 4));
            zoomOut.setMinimumSize(new Dimension(height - 4, height - 4));

            setBackground(Color.BLACK);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setPreferredSize(new Dimension(width, height));
            setSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));

            add(zoomIn);
            add(zoomOut);

            buttonGroup.add(zoomIn);
            buttonGroup.add(zoomOut);
        }

        public boolean zoomIn() {
            return zoomIn.isSelected();
        }

        public boolean zoomOut() {
            return zoomOut.isSelected();
        }
    }

    static class AVXSelector extends JPanel {

        private final JCheckBox checkBox;

        public AVXSelector(int width, int height) {
            checkBox = new JCheckBox("AVX", false);

            JLabel label = new JLabel("AVX");
            label.setForeground(Color.LIGHT_GRAY);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14));

            setBackground(Color.BLACK);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setPreferredSize(new Dimension(width, height));
            setSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));

            add(label);
            add(checkBox);
        }

        public void onChanged(Consumer<Boolean> callback) {
            checkBox.addActionListener(e -> callback.accept(checkBox.isSelected()));
        }
    }

    static class SetInput extends JPanel {
        private final JComboBox<String> comboBox;

        public SetInput(int width, int height) {
            comboBox = new JComboBox<>(new String[]{"Mandelbrot", "Julia"});
            comboBox.setSelectedIndex(0);

            JLabel label = new JLabel("Set");
            label.setForeground(Color.LIGHT_GRAY);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14));

            setBackground(Color.BLACK);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setPreferredSize(new Dimension(width, height));
            setSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));

            add(label);
            add(comboBox);
        }

        public void onChange(Consumer<String> callback) {
            this.comboBox.addActionListener(e -> callback.accept((String) this.comboBox.getSelectedItem()));
        }
    }
}
