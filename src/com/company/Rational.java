package com.company;

import java.math.BigDecimal;
import java.math.RoundingMode;

class Rational
{
    int p = 0, q = 1;

    Rational()
    {
    }

    Rational(int a, int b)
    {
        p = a;
        q = b;
    }

    double value()
    {
        return p / (double) q;
    }

    BigDecimal preciseValue()
    {
        return new BigDecimal(p).divide(new BigDecimal(q), 50, RoundingMode.CEILING);
    }

    Rational multiply(Rational other)
    {
        int outP = p * other.p, outQ = q * other.q;
        int gcd = gcd(outP, outQ);
        outP /= gcd;
        outQ /= gcd;

        return new Rational(outP, outQ);
    }

    Rational add(Rational other)
    {
        int outP = (p * other.q) + (other.p * q), outQ = q * other.q;
        int gcd = gcd(outP, outQ);
        outP /= gcd;
        outQ /= gcd;

        return new Rational(outP, outQ);
    }

    @Override
    public String toString()
    {
        if (p == 1 && q == 1) return "";
        if (q == 1) return "" + p;
        return "(" + p + "/" + q + ")";
    }

    @Override
    public int hashCode()
    {
        return p * q;
    }

    @Override
    public boolean equals(Object obj)
    {
        return p == ((Rational) obj).p && q == ((Rational) obj).q;
    }

    static int depth = 0, c, d;

    static int gcd(int a, int b)
    {
        depth = 0;
        c = a;
        d = b;
        return help(Math.abs(a), Math.abs(b));
    }

    static int help(int a, int b)
    {
        while (true)
        {
            if (a == 0)
                return b;
            if (b == 0)
                return a;

            if (a == b)
                return a;

            if (a > b)
                a = a - b;
            else
                b = b - a;
        }
    }
}
