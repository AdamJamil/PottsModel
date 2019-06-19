package com.company;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

class RationalExpression
{
    Polynomial num, denom;
    Rational coeff = new Rational(1, 1);

    BigDecimal evaluatePrecise(BigDecimal lambda)
    {
        if (denom.evaluatePrecise(lambda).compareTo(new BigDecimal(0)) == 0)
            System.out.println(denom);
        return coeff.preciseValue().multiply(num.evaluatePrecise(lambda).divide(denom.evaluatePrecise(lambda), 50, RoundingMode.CEILING));
    }

    double evaluate(double lambda)
    {
        return coeff.p * num.evaluate(lambda) / (coeff.q * denom.evaluate(lambda));
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

        Polynomial num1 = this.num.multiply(this.coeff);
        Polynomial num2 = other.num.multiply(other.coeff);
        RationalExpression out = new RationalExpression();
        out.num = num1.add(num2);
        out.denom = denom.multiply(Main.one);
        out.setCoefficient();

        return out;
    }

    RationalExpression divide(Polynomial p)
    {
        RationalExpression rationalExpression = new RationalExpression(num.multiply(Main.one));
        rationalExpression.denom = denom.multiply(p);
        rationalExpression.coeff = coeff.multiply(new Rational(1, 1));

        return rationalExpression;
    }

    RationalExpression multiply(Rational r)
    {
        Polynomial newNum = num.multiply(new Rational(r.p * coeff.p, 1));
        Polynomial newDenom = denom.multiply(new Rational(r.q * coeff.q, 1));
        RationalExpression out = new RationalExpression();
        out.num = newNum;
        out.denom = newDenom;
        out.setCoefficient();

        return out;
    }

    RationalExpression multiply(RationalExpression r)
    {
        RationalExpression out = this.multiply(new Rational(1, 1));
        out.num = out.num.multiply(r.num).multiply(new Rational(r.coeff.p, 1));
        out.denom = out.denom.multiply(r.denom).multiply(new Rational(r.coeff.q, 1));
        out.setCoefficient();

        return out;
    }

    void setCoefficient()
    {
        if (num.equals(Main.zero))
        {
            coeff = new Rational(0, 1);
            return;
        }

        num = num.multiply(new Rational(coeff.p, 1));
        denom = denom.multiply(new Rational(coeff.q, 1));
        coeff = new Rational(1, 1);

        ArrayList<Integer> numList = new ArrayList<>();
        for (Rational rational : num.coefficients)
            numList.add(rational.q);
        int numLcm = lcm(numList, 1, 0);

        num = num.multiply(new Rational(numLcm, 1));

        ArrayList<Integer> denomList = new ArrayList<>();
        for (Rational rational : denom.coefficients)
            denomList.add(rational.q);
        int denomLcm = lcm(denomList, 1, 0);

        denom = denom.multiply(new Rational(denomLcm, 1));

        coeff = new Rational(denomLcm, numLcm);

        setCoefficient2();
    }

    void setCoefficient2()
    {
        ArrayList<Integer> numList = new ArrayList<>();
        for (Rational rational : num.coefficients)
            numList.add(rational.p);
        int numGcd = gcd(numList, numList.get(0), 0);

        num = num.multiply(new Rational(1, numGcd));

        ArrayList<Integer> denomList = new ArrayList<>();
        for (Rational rational : denom.coefficients)
            denomList.add(rational.p);
        int denomGcd = gcd(denomList, denomList.get(0), 0);

        denom = denom.multiply(new Rational(1, denomGcd));

        coeff = coeff.multiply(new Rational(numGcd, denomGcd));

        setCoefficient3();
    }

    void setCoefficient3()
    {
        int nonzeroTerms = 0;
        for (Rational coefficient : num.coefficients)
            if (coefficient.p != 0)
                nonzeroTerms++;

        if (nonzeroTerms == 1)
        {
            num = num.multiply(new Rational(coeff.p, 1));
            coeff.p = 1;
        }

        nonzeroTerms = 0;
        for (Rational coefficient : denom.coefficients)
            if (coefficient.p != 0)
                nonzeroTerms++;

        if (nonzeroTerms == 1)
        {
            denom = denom.multiply(new Rational(coeff.q, 1));
            coeff.q = 1;
        }
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
        setCoefficient();
        return "" + coeff + "[" + num + "]/[" + denom + "]";
    }
}