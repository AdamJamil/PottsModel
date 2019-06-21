package com.company;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

class RESum
{
    ArrayList<RationalExpression> terms = new ArrayList<>();

    RESum()
    {
        terms.add(new RationalExpression());
    }

    BigDecimal evaluatePrecise(BigDecimal lambda)
    {
        BigDecimal out = new BigDecimal(0);
        for (RationalExpression rE : terms)
            out = out.add(rE.evaluatePrecise(lambda));

        return out;
    }

    double evaluate(double lambda)
    {
        double out = 0;
        for (RationalExpression term : terms)
            out += term.evaluate(lambda);

        return out;
    }

    RESum multiply(RESum other)
    {
        RESum out = new RESum();

        for (RationalExpression t1 : terms)
            for (RationalExpression t2 : other.terms)
                out.terms.add(t1.multiply(t2));

        return fix(out);
    }

    RESum add(RationalExpression newTerm)
    {
        RESum out = new RESum();

        for (RationalExpression term : terms)
        {
            if (term.num.coefficients.size() == 1 && term.num.coefficients.get(0).p == 0)
                continue;
            out.terms.add(term.multiply(new Rational(1, 1)));
        }

        if (newTerm.num.equals(Main.zero))
            return out;

        out.terms.add(newTerm.multiply(new Rational(1, 1)));

        return fix(out);
    }

    RESum add(RESum newTerms)
    {
        RESum out = null;

        for (RationalExpression newTerm : newTerms.terms)
            if (out == null)
                out = this.add(newTerm);
            else
                out = out.add(newTerm);

        return out;
    }

    RESum multiply(Rational r)
    {
        RESum out = new RESum();
        for (RationalExpression term : terms)
            out.terms.add(term.multiply(r));

        for (int i = out.terms.size() - 1; i >= 0; i--)
            if (out.terms.get(i).num.equals(Main.zero))
                out.terms.remove(i);

        if (out.terms.size() == 0)
            out.terms.add(new RationalExpression());

        return out;
    }

    RESum fix(RESum out)
    {
        for (int i = out.terms.size() - 1; i >= 0; i--)
            if (out.terms.get(i).num.equals(Main.zero))
                out.terms.remove(i);

        boolean edited;
        do
        {
            edited = false;
            outer: for (int i = 0; i < out.terms.size(); i++)
                for (int j = i + 1; j < out.terms.size(); j++)
                    if (out.terms.get(i).denom.equals(out.terms.get(j).denom))
                    {
                        edited = true;
                        //out.terms.get(i).num = out.terms.get(i).num.add(out.terms.get(j).num);
                        out.terms.set(i, out.terms.get(i).add(out.terms.get(j)));
                        out.terms.remove(j); //not suspicious!
                        break outer;
                    }
        } while (edited);

        return out;
    }

    @SuppressWarnings("Duplicates")
    boolean geq(RESum other)
    {
        for (double lambda = 1.05; lambda <= 5; lambda *= 1.05)
            if (evaluate(lambda) < other.evaluate(lambda) - 0.000001)
                return false;

        HashSet<Polynomial> denoms = new HashSet<>();

        for (RationalExpression term : terms)
            denoms.add(term.denom);

        for (RationalExpression term : other.terms)
            denoms.add(term.denom);

        Polynomial f = Main.zero;

        for (RationalExpression term : terms)
        {
            Polynomial temp = term.num;

            for (Polynomial denom : denoms)
                if (!denom.equals(term.denom))
                    temp = temp.multiply(denom);

            f = f.add(temp);
        }

        for (RationalExpression term : other.terms)
        {
            Polynomial temp = term.num;

            for (Polynomial denom : denoms)
                if (!denom.equals(term.denom))
                    temp = temp.multiply(denom);

            f = f.add(temp.multiply(new Rational(-1, 1)));
        }

        boolean fast = true;

        for (Rational coefficient : f.coefficients)
            fast &= coefficient.value() >= 0;

        if (fast)
            return true;

        return f.geqZero();
    }

    @SuppressWarnings("Duplicates")
    int compare(RESum other)
    {
        //construct a polynomial to compare to zero

        //find all denoms
        HashSet<Polynomial> denoms = new HashSet<>();

        for (RationalExpression term : terms)
            denoms.add(term.denom);

        for (RationalExpression term : other.terms)
            denoms.add(term.denom);

        Polynomial f = Main.zero.multiply(Main.one);

        for (RationalExpression term : terms)
        {
            Polynomial temp = term.num;

            for (Polynomial denom : denoms)
                if (!denom.equals(term.denom))
                    temp = temp.multiply(denom);

            f = f.add(temp);
        }

        for (RationalExpression term : other.terms)
        {
            Polynomial temp = term.num;

            for (Polynomial denom : denoms)
                if (!denom.equals(term.denom))
                    temp = temp.multiply(denom);

            f = f.add(temp.multiply(new Rational(-1, 1)));
        }

        return f.compare();
    }

    @Override
    public int hashCode()
    {
        int sum = 0;
        for (RationalExpression term : terms)
            sum += term.num.hashCode();
        return sum;
    }

    @Override
    public boolean equals(Object other)
    {
        int maxDegree = 1000; //TODO: change me!
        for (double lambda = 1.1; lambda < maxDegree; lambda *= 1.1)
            if (evaluate(lambda) != ((RESum) other).evaluate(lambda))
                return false;
        return true;
    }

    String LaTeX()
    {
        String temp = "";
        for (RationalExpression rE : terms)
        {
            if (rE.num.equals(Main.zero))
                continue;
            temp += "\\frac{" + rE.num.LaTeX() + "}{" + rE.denom.LaTeX() + "} + ";
        }
        if (temp.equals(""))
            temp = "0000";

        return temp.substring(0, Math.max(0, temp.length() - 3));
    }

    @Override
    public String toString()
    {
        for (int i = terms.size() - 1; i >= 0; i--)
            if (terms.get(i).num.equals(Main.zero))
                terms.remove(i);

        if (terms.size() == 0)
            terms.add(new RationalExpression());

        String temp = terms.get(0).toString();

        for (int i = 1; i < terms.size(); i++)
            temp += " + " + terms.get(i);

        return temp;
    }
}
