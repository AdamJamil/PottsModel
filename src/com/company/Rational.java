package com.company;

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

    double value()
    {
        return p / (double) q;
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

    private static long gcd(long a, long b)
    {
        return help(Math.abs(a), Math.abs(b));
    }

    private static long help(long a, long b)
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
