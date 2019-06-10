package com.company;

import java.math.BigDecimal;
import java.util.ArrayList;

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

    void add(RationalExpression newTerm)
    {
        terms.add(newTerm);

        boolean edited;
        do
        {
            edited = false;
            outer: for (int i = 0; i < terms.size(); i++)
                for (int j = i + 1; j < terms.size(); j++)
                    if (terms.get(i).denom.equals(terms.get(j).denom))
                    {
                        edited = true;
                        terms.get(i).num = terms.get(i).num.add(terms.get(j).num);
                        terms.remove(j); //not suspicious!
                        break outer;
                    }
        } while (edited);
    }
}
