package com.company;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

class RationalExpression
{
    Polynomial num, denom;
    //Rational coeff = new Rational(1, 1);

    BigDecimal evaluatePrecise(BigDecimal lambda)
    {
        if (denom.evaluatePrecise(lambda).compareTo(new BigDecimal(0)) == 0)
            System.out.println(denom);
        return num.evaluatePrecise(lambda).divide(denom.evaluatePrecise(lambda), 50, RoundingMode.CEILING);
    }

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
        denom.degree = 0;
        denom.coefficients.add(new Rational(1, 1));
        num = new Polynomial();
        num.degree = 0;
        num.coefficients.add(new Rational());
    }

    RationalExpression(Polynomial p)
    {
        num = p.copy();
        denom = new Polynomial();
        denom.degree = 0;
        denom.coefficients.add(new Rational(1, 1));
    }

    //looks good
    void add(RationalExpression other)
    {
        //assumes that they have the same denom!
        if (!this.denom.equals(other.denom))
        {
            System.out.println("Error in addition; RationalExpression.java, add()");
            return;
        }

        num.add(other.num);
    }

    //looks good
    void divide(Polynomial p)
    {
        denom.multiply(p);
    }

    //looks good
    void multiply(Rational r)
    {
        num.multiply(r);
    }

    //looks good
    void multiply(RationalExpression r)
    {
        num.multiply(r.num);
        denom.multiply(r.denom);
    }

    @Override
    public String toString()
    {
        return "[" + num + "]/[" + denom + "]";
    }
}