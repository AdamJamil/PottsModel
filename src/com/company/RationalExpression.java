package com.company;

class RationalExpression
{
    Polynomial num, denom;

    double evaluate(double lambda)
    {
        return num.evaluate(lambda) / denom.evaluate(lambda);
    }

    RationalExpression copy()
    {
        RationalExpression out = new RationalExpression();
        out.num = num.copy();
        out.denom = denom.copy();

        return out;
    }

    RationalExpression()
    {
        denom = new Polynomial();
        denom.coefficients.add(new Rational(1, 1));
        num = new Polynomial();
        num.coefficients.add(new Rational());
    }

    RationalExpression(Polynomial p)
    {
        num = p.copy();
        denom = new Polynomial();
        denom.coefficients.add(new Rational(1, 1));
    }

    void divide(Polynomial p)
    {
        denom.multiply(p);
    }

    @Override
    public String toString()
    {
        return "[" + num + "]/[" + denom + "]";
    }
}