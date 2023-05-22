package mandelbrot.plotter;

import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.FMA;

//JEP 338: Vector API (Incubator)
public class MandelbrotAVXPlotter extends Plotter {

    protected static final VectorSpecies<Double> DS = DoubleVector.SPECIES_PREFERRED;
    protected static final VectorSpecies<Long> LS = LongVector.SPECIES_PREFERRED;
    protected static final LongVector ONE = LongVector.broadcast(LS, 1);
    protected static final DoubleVector TWO = DoubleVector.broadcast(DS, 2);
    protected static final DoubleVector FOUR = DoubleVector.broadcast(DS, 4);

    public MandelbrotAVXPlotter(int width, int height) {
        super(width, height);
    }

    @Override
    public void plot(double minx, double miny, double step, int sectionWidth, int sectionHeight, int offset) {
        int upperBoundW = DS.loopBound(sectionWidth);
        int pixel = offset;

        double y = miny;
        for (int i = 0; i < sectionHeight; ++i) {
            double x = minx;
            DoubleVector vy = DoubleVector.broadcast(DS, y);
            for (int j = 0; j < upperBoundW; j += DS.length()) {
                DoubleVector vx = DoubleVector.fromArray(DS, buildValues(x, step, DS.length()), 0);
                DoubleVector zx = DoubleVector.broadcast(DS, this.zx);
                DoubleVector zy = DoubleVector.broadcast(DS, this.zy);
                DoubleVector z2x = zx.mul(zx);
                DoubleVector z2y = zy.mul(zy);
                LongVector it = LongVector.zero(LS);

                VectorMask<Long> mask = z2x.add(z2y)
                        .compare(VectorOperators.LE, FOUR)
                        .cast(LS)
                        .and(it.lt(maxIterations));

                while (mask.anyTrue()) {
                    zy = zx.mul(TWO).lanewise(FMA, zy, vy);//zx.mul(2).mul(zy).add(vy);
                    zx = z2x.sub(z2y).add(vx);
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

    protected double[] buildValues(double start, double step, int length) {
        double[] result = new double[length];
        double current = start;
        for (int i = 0; i < length; ++i) {
            result[i] = current;
            current += step;
        }
        return result;
    }
}
