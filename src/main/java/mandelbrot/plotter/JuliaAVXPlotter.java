package mandelbrot.plotter;

import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.FMA;

public class JuliaAVXPlotter extends MandelbrotAVXPlotter {

    public JuliaAVXPlotter(int width, int height) {
        super(width, height);
    }

    @Override
    public void plot(double minx, double miny, double step, int sectionWidth, int sectionHeight, int offset) {
        int upperBoundW = DS.loopBound(sectionWidth);
        int pixel = offset;

        DoubleVector vzx = DoubleVector.broadcast(DS, this.zx);
        DoubleVector vzy = DoubleVector.broadcast(DS, this.zy);

        double y = miny;
        for (int i = 0; i < sectionHeight; ++i) {
            double x = minx;
            for (int j = 0; j < upperBoundW; j += DS.length()) {
                DoubleVector zx = DoubleVector.fromArray(DS, buildValues(x, step, DS.length()), 0);
                DoubleVector zy = DoubleVector.broadcast(DS, y);
                DoubleVector z2x = zx.mul(zx);
                DoubleVector z2y = zy.mul(zy);
                LongVector it = LongVector.zero(LS);

                VectorMask<Long> mask = z2x.add(z2y)
                        .compare(VectorOperators.LE, FOUR)
                        .cast(LS)
                        .and(it.lt(maxIterations));

                while (mask.anyTrue()) {
                    zy = zx.mul(TWO).lanewise(FMA, zy, vzy);
                    zx = z2x.sub(z2y).add(vzx);
                    z2x = zx.mul(zx);
                    z2y = zy.mul(zy);

                    it = it.add(ONE, mask);

                    mask = z2x.add(z2y)
                            .compare(VectorOperators.LE, FOUR)
                            .cast(LS)
                            .and(it.lt(maxIterations));
                }

                for (int idx = 0; idx < it.length(); ++idx) {
                    buffer[pixel + idx] = (int) it.lane(idx);
                }

                x += step * DS.length();
                pixel += DS.length();
            }
            y += step;
        }
    }
}
