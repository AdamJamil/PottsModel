package com.company;

import java.math.BigDecimal;
import java.math.RoundingMode;

class Rational
{
    long p = 0, q = 1;

    Rational copy()
    {
        return new Rational(p, q);
    }

    Rational(){}

    Rational(long a, long b)
    {
        p = a;
        q = b;
    }

    static Rational pow(Rational base, int pow)
    {
        if (pow == 1)
            return base.copy();
        if (pow == 2)
        {
            Rational out = base.copy();
            out.multiply(base);
            return out;
        }

        Rational out = pow(base, pow / 2);
        out.multiply(out);

        if (pow % 2 == 1)
            out.multiply(base);

        return out;
    }

    void simplify()
    {
        long gcd = gcd(p, q);
        p /= gcd;
        q /= gcd;
    }

    double value()
    {
        return p / (double) q;
    }

    BigDecimal preciseValue()
    {
        return new BigDecimal(p).divide(new BigDecimal(q), 50, RoundingMode.CEILING);
    }

    void multiply(Rational other)
    {
        long gcd = gcd(other.p, other.q);
        p *= other.p / gcd;
        q *= other.q / gcd;
    }

    void add(Rational other)
    {
        p = (p * other.q) + (other.p * q);
        q *= other.q;
        long gcd = gcd(p, q);
        p /= gcd;
        q /= gcd;
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
        return (int) (p * q);
    }

    @Override
    public boolean equals(Object obj)
    {
        return p == ((Rational) obj).p && q == ((Rational) obj).q;
    }


    static long gcd(long a, long b)
    {
        return help(Math.abs(a), Math.abs(b));
    }

    static long help(long a, long b)
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
