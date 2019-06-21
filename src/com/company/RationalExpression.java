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
        num = p.multiply(Main.one);
        denom = new Polynomial();
        denom.degree = 0;
        denom.coefficients.add(new Rational(1, 1));
    }

    RationalExpression add(RationalExpression other)
    {
        //assumes that they have the same denom!
        if (!this.denom.equals(other.denom))
        {
            System.out.println("Error in addition; RationalExpression.java, add()");
            return null;
        }

        Polynomial num1 = this.num.multiply(new Rational(1, 1));
        Polynomial num2 = other.num.multiply(new Rational(1, 1));
        RationalExpression out = new RationalExpression();
        out.num = num1.add(num2);
        out.denom = denom.multiply(Main.one);

        return out;
    }

    RationalExpression divide(Polynomial p)
    {
        RationalExpression rationalExpression = new RationalExpression(num.multiply(new Rational(1, 1)));
        rationalExpression.denom = denom.multiply(p);

        return rationalExpression;
    }

    RationalExpression multiply(Rational r)
    {
        Polynomial newNum = num.multiply(new Rational(r.p, 1));
        Polynomial newDenom = denom.multiply(new Rational(r.q, 1));
        RationalExpression out = new RationalExpression();
        out.num = newNum;
        out.denom = newDenom;

        return out;
    }

    RationalExpression multiply(RationalExpression r)
    {
        RationalExpression out = this.multiply(new Rational(1, 1));
        out.num = out.num.multiply(r.num);
        out.denom = out.denom.multiply(r.denom);

        return out;
    }

    static int lcm(ArrayList<Integer> list, int n, int pos)
    {
        if (pos == list.size())
            return n;
        return lcm(list, list.get(pos) * n / Rational.gcd(list.get(pos), n), pos + 1);
    }

    static int gcd(ArrayList<Integer> list, int n, int pos)
    {
        if (pos == list.size())
            return n;
        return gcd(list, Rational.gcd(list.get(pos), n), pos + 1);
    }

    @Override
    public String toString()
    {
        return "[" + num + "]/[" + denom + "]";
    }
}