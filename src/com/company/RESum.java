package com.company;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

class RESum
{
    ArrayList<RationalExpression> terms = new ArrayList<>();

    RESum copy()
    {
        RESum out = new RESum();
        out.terms.clear();

        for (RationalExpression term : terms)
            out.terms.add(term.copy());

        return out;
    }

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

    //looks good
    void multiply(RESum other)
    {
        ArrayList<RationalExpression> newTerms = new ArrayList<>();
        for (RationalExpression t1 : terms)
            for (RationalExpression t2 : other.terms)
            {
                RationalExpression temp = t1.copy();
                temp.multiply(t2);
                newTerms.add(temp);
            }

        terms = newTerms;
    }

    //looks good
    void add(RationalExpression newTerm)
    {
        if (newTerm.num.equals(Main.zero))
            return;

        for (RationalExpression term : terms)
            if (term.denom.equals(newTerm.denom))
            {
                term.num.add(newTerm.num);
                return;
            }

        terms.add(newTerm.copy());
    }

    //looks good
    void add(RESum newTerms)
    {
        for (RationalExpression term : newTerms.terms)
            add(term);
    }

    //looks good
    void multiply(Rational r)
    {
        for (RationalExpression term : terms)
            term.multiply(r);
    }

    void simplify()
    {
        ArrayList<RationalExpression> newTerms = new ArrayList<>();

        outer: for (RationalExpression term : terms)
        {
            for (RationalExpression newTerm : newTerms)
                if (term.denom.equals(newTerm.denom))
                {
                    newTerm.num.add(term.num);
                    continue outer;
                }

            newTerms.add(term);
        }

        terms = newTerms;
    }

//    RESum fix(RESum out)
//    {
//        for (int i = out.terms.size() - 1; i >= 0; i--)
//            if (out.terms.get(i).num.equals(Main.zero))
//                out.terms.remove(i);
//
//        boolean edited;
//        do
//        {
//            edited = false;
//            outer: for (int i = 0; i < out.terms.size(); i++)
//                for (int j = i + 1; j < out.terms.size(); j++)
//                    if (out.terms.get(i).denom.equals(out.terms.get(j).denom))
//                    {
//                        edited = true;
//                        //out.terms.get(i).num = out.terms.get(i).num.add(out.terms.get(j).num);
//                        out.terms.set(i, out.terms.get(i).add(out.terms.get(j)));
//                        out.terms.remove(j); //not suspicious!
//                        break outer;
//                    }
//        } while (edited);
//
//        return out;
//    }

    @SuppressWarnings("Duplicates")
    boolean geq(RESum other)
    {
        HashSet<Polynomial> denoms = new HashSet<>();

        for (RationalExpression term : terms)
            denoms.add(term.denom);

        for (RationalExpression term : other.terms)
            denoms.add(term.denom);

        Polynomial f = Main.zero.copy();

        for (RationalExpression term : terms)
        {
            Polynomial temp = term.num.copy();

            for (Polynomial denom : denoms)
                if (!denom.equals(term.denom))
                    temp.multiply(denom);

            f.add(temp);
        }

        for (RationalExpression term : other.terms)
        {
            Polynomial temp = term.num.copy();

            for (Polynomial denom : denoms)
                if (!denom.equals(term.denom))
                    temp.multiply(denom);

            temp.multiply(new Rational(-1, 1));
            f.add(temp);
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

        Polynomial f = Main.zero.copy();

        for (RationalExpression term : terms)
        {
            Polynomial temp = term.num;

            for (Polynomial denom : denoms)
                if (!denom.equals(term.denom))
                    temp.multiply(denom);

            f.add(temp);
        }

        for (RationalExpression term : other.terms)
        {
            Polynomial temp = term.num.copy();

            for (Polynomial denom : denoms)
                if (!denom.equals(term.denom))
                    temp.multiply(denom);

            temp.multiply(new Rational(-1, 1));
            f.add(temp);
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
        return evaluate(1.41421356237309504880168872420969) == ((RESum) other).evaluate(1.41421356237309504880168872420969);
//        int maxDegree = 50;
//        for (double lambda = 1.1; lambda < maxDegree; lambda *= 1.1)
//            if (evaluate(lambda) != ((RESum) other).evaluate(lambda))
//                return false;
//        return true;
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
