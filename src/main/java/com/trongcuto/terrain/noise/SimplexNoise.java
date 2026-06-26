package com.trongcuto.terrain.noise;

import java.util.Random;

/**
 * 3D Simplex noise.
 *
 * <p>Java port of Stefan Gustavson's public-domain reference implementation of
 * Ken Perlin's Simplex noise, with a seedable permutation table so each world
 * seed produces deterministic but distinct terrain.</p>
 *
 * <p>{@link #noise(double, double, double)} returns a value in the approximate
 * range [-1, 1]. The {@link #octaves} and {@link #ridged} helpers layer several
 * frequencies (fractal Brownian motion) to build natural-looking terrain.</p>
 */
public final class SimplexNoise {

    private static final Grad[] GRAD3 = {
            new Grad(1, 1, 0), new Grad(-1, 1, 0), new Grad(1, -1, 0), new Grad(-1, -1, 0),
            new Grad(1, 0, 1), new Grad(-1, 0, 1), new Grad(1, 0, -1), new Grad(-1, 0, -1),
            new Grad(0, 1, 1), new Grad(0, -1, 1), new Grad(0, 1, -1), new Grad(0, -1, -1)
    };

    private static final double F3 = 1.0 / 3.0;
    private static final double G3 = 1.0 / 6.0;

    /** Permutation table, doubled to avoid index wrapping in the hot loop. */
    private final short[] perm = new short[512];
    private final short[] permMod12 = new short[512];

    public SimplexNoise(long seed) {
        short[] p = new short[256];
        for (short i = 0; i < 256; i++) {
            p[i] = i;
        }
        // Fisher-Yates shuffle seeded by the world seed.
        Random random = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            short tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
            permMod12[i] = (short) (perm[i] % 12);
        }
    }

    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double dot(Grad g, double x, double y, double z) {
        return g.x * x + g.y * y + g.z * z;
    }

    /**
     * Raw 3D Simplex noise, output in the approximate range [-1, 1].
     */
    public double noise(double xin, double yin, double zin) {
        double n0;
        double n1;
        double n2;
        double n3;

        double s = (xin + yin + zin) * F3;
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);
        int k = fastFloor(zin + s);
        double t = (i + j + k) * G3;
        double x0 = xin - (i - t);
        double y0 = yin - (j - t);
        double z0 = zin - (k - t);

        int i1;
        int j1;
        int k1;
        int i2;
        int j2;
        int k2;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1;
            } else {
                i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1;
            } else if (x0 < z0) {
                i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1;
            } else {
                i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            }
        }

        double x1 = x0 - i1 + G3;
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + 2.0 * G3;
        double y2 = y0 - j2 + 2.0 * G3;
        double z2 = z0 - k2 + 2.0 * G3;
        double x3 = x0 - 1.0 + 3.0 * G3;
        double y3 = y0 - 1.0 + 3.0 * G3;
        double z3 = z0 - 1.0 + 3.0 * G3;

        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int gi0 = permMod12[ii + perm[jj + perm[kk]]];
        int gi1 = permMod12[ii + i1 + perm[jj + j1 + perm[kk + k1]]];
        int gi2 = permMod12[ii + i2 + perm[jj + j2 + perm[kk + k2]]];
        int gi3 = permMod12[ii + 1 + perm[jj + 1 + perm[kk + 1]]];

        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(GRAD3[gi0], x0, y0, z0);
        }

        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(GRAD3[gi1], x1, y1, z1);
        }

        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(GRAD3[gi2], x2, y2, z2);
        }

        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(GRAD3[gi3], x3, y3, z3);
        }

        return 32.0 * (n0 + n1 + n2 + n3);
    }

    /**
     * 2D convenience overload (samples the 3D field at z = 0).
     */
    public double noise(double x, double y) {
        return noise(x, y, 0.0);
    }

    /**
     * Fractal Brownian motion: sum several octaves of noise at increasing
     * frequency and decreasing amplitude. Output is normalised to ~[-1, 1].
     *
     * @param octaves     number of layers
     * @param persistence amplitude multiplier per octave (e.g. 0.5)
     * @param lacunarity  frequency multiplier per octave (e.g. 2.0)
     */
    public double octaves(double x, double y, double z, int octaves,
                          double persistence, double lacunarity) {
        double total = 0.0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxAmplitude = 0.0;
        for (int o = 0; o < octaves; o++) {
            total += noise(x * frequency, y * frequency, z * frequency) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return maxAmplitude == 0.0 ? 0.0 : total / maxAmplitude;
    }

    /**
     * Ridged multifractal noise, useful for sharp mountain ridges. Output is in
     * the approximate range [0, 1].
     */
    public double ridged(double x, double y, double z, int octaves,
                         double persistence, double lacunarity) {
        double total = 0.0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxAmplitude = 0.0;
        for (int o = 0; o < octaves; o++) {
            double n = 1.0 - Math.abs(noise(x * frequency, y * frequency, z * frequency));
            n *= n;
            total += n * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return maxAmplitude == 0.0 ? 0.0 : total / maxAmplitude;
    }

    private static final class Grad {
        final double x;
        final double y;
        final double z;

        Grad(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
