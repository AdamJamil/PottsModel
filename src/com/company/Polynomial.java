package com.company;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

class Polynomial
{
    ArrayList<Rational> coefficients = new ArrayList<>();
    private static final double err = 0.0001;
    static ArrayList<Polynomial> pow;
    static Rational minusOne = new Rational(-1, 1);

    Polynomial quot(Polynomial divisor)
    {
        Polynomial quot = new Polynomial();
        Polynomial rem = this.copy();
        int d = divisor.coefficients.size() - 1;
        Rational c = divisor.coefficients.get(d);

        while (rem.coefficients.size() - 1 >= d)
        {
            Polynomial temp = pow.get(rem.coefficients.size() - 1 - d);
            temp.multiply(new Rational(rem.coefficients.get(rem.coefficients.size() - 1).p * c.q,
                    rem.coefficients.get(rem.coefficients.size() - 1).q * c.p));
            quot.add(temp);
            temp.multiply(divisor);
            temp.multiply(minusOne);
            rem.add(temp);
        }

        return quot;
    }

    Polynomial rem(Polynomial divisor)
    {
        Polynomial rem = this.copy();
        int d = divisor.coefficients.size() - 1;
        Rational c = divisor.coefficients.get(d);

        while (rem.coefficients.size() - 1 >= d)
        {
            Polynomial temp = pow.get(rem.coefficients.size() - 1 - d);
            temp.multiply(new Rational(rem.coefficients.get(rem.coefficients.size() - 1).p * c.q,
                    rem.coefficients.get(rem.coefficients.size() - 1).q * c.p));
            temp.multiply(divisor);
            temp.multiply(minusOne);
            rem.add(temp);
        }

        return rem;
    }

    boolean geqZero()
    {
        if (coefficients.size() == 1)
            return coefficients.get(0).value() >= 0;

        if (evaluate(1) < 0 || coefficients.get(coefficients.size() - 1).value() < 0)
            return false;

        double domCoeff = 0;
        boolean dominated = true;
        for (int i = coefficients.size() - 1; i >= 0; i--)
        {
            domCoeff += coefficients.get(i).value();
            if (domCoeff < err)
            {
                dominated = false;
                break;
            }
        }

        if (dominated)
            return true;

        ArrayList<Polynomial> P = new ArrayList<>();
    }

    Polynomial derivative()
    {
        Polynomial out = new Polynomial();
        for (int i = 1; i < coefficients.size(); i++)
        {
            Rational temp = coefficients.get(i).copy();
            temp.p *= i;
            out.coefficients.add(temp);
        }

        return out;
    }

    int compare()
    {
        if (coefficients.size() == 1)
        {
            if (coefficients.get(0).value() == 0)
                return 3;
            if (coefficients.get(0).value() > 0)
                return 0;
            return 1;
        }

        Complex[] roots = this.findRoots();
        ArrayList<Double> realRoots = new ArrayList<>();

        for (Complex root : roots)
            if ((root.theta < 0.0001 || (2 * Math.PI - root.theta) < 0.0001) && root.r > 1) //if real root > 1
                realRoots.add(root.r);

        Collections.sort(realRoots);

        boolean allPos = true, allNeg = true;

        if (realRoots.size() == 0)
        {
            if (evaluate(2) > err)
                allNeg = false;
            if (evaluate(2) < -err)
                allPos = false;
        }
        else
        {
            if (evaluate((1 + realRoots.get(0)) / 2) > err)
                allNeg = false;
            if (evaluate((1 + realRoots.get(0)) / 2) < -err)
                allPos = false;

            for (int i = 1; i < realRoots.size() - 1; i++)
            {
                if (Math.abs(realRoots.get(i + 1) - realRoots.get(i)) < err)
                    continue;
                if (evaluate((realRoots.get(i) + realRoots.get(i + 1)) / 2) > err)
                    allNeg = false;
                if (evaluate((realRoots.get(i) + realRoots.get(i + 1)) / 2) < -err)
                    allPos = false;
            }

            if (evaluate(2 * realRoots.get(realRoots.size() - 1)) > err)
                allNeg = false;
            if (evaluate(2 * realRoots.get(realRoots.size() - 1)) < -err)
                allPos = false;
        }

        if (allPos && allNeg)
            return 3;
        if (allPos)
            return 0;
        if (allNeg)
            return 1;
        return 2;
    }

    BigDecimal evaluatePrecise(BigDecimal lambda)
    {
        BigDecimal sum = new BigDecimal(0), pow = new BigDecimal(1);

        for (Rational coefficient : coefficients)
        {
            sum = sum.add(pow.multiply(coefficient.preciseValue()));
            pow = pow.multiply(lambda);
        }

        return sum;
    }

    Complex evaluate(Complex x)
    {
        Complex sum = new Complex(coefficients.get(0).p / (double) coefficients.get(0).q, 0);
        Complex pow = x.multiply(new Complex(1, 0));

        for (int i = 1; i < coefficients.size(); i++)
        {
            sum = sum.add(pow.multiply(new Complex(coefficients.get(i).p / (double) coefficients.get(i).q, 0)));
            pow = pow.multiply(x);
        }

        return sum;
    }

    double evaluate(double lambda)
    {
        double result = coefficients.get(coefficients.size() - 1).value();

        for (int i = coefficients.size() - 2; i >= 0; i--)
            result = (result * lambda) + coefficients.get(i).value();

        return result;
    }

    //looks good
    void add(Polynomial other)
    {
        for (int i = 0; i < Math.max(coefficients.size(), other.coefficients.size()); i++)
        {
            Rational temp = new Rational();
            if (i < coefficients.size())
                temp.add(coefficients.get(i));
            if (i < other.coefficients.size())
                temp.add(other.coefficients.get(i));
            if (coefficients.size() > i)
                coefficients.set(i, temp);
            else
                coefficients.add(temp);
        }

        for (int i = coefficients.size() - 1; i > 0; i--)
            if (coefficients.get(i).p == 0)
                coefficients.remove(i);
            else
                break;
    }

    //looks BAD
    void multiply(Polynomial other)
    {
        ArrayList<Rational> newCoefficients = new ArrayList<>();

        for (int outDegree = 0; outDegree <= coefficients.size() + coefficients.size() - 2; outDegree++)
        {
            Rational current = new Rational();
            for (int firstDegree = Math.max(0, outDegree - other.coefficients.size() - 1); firstDegree <= Math.min(outDegree, coefficients.size() - 1); firstDegree++)
            {
                Rational temp = coefficients.get(firstDegree).copy();
                temp.multiply(other.coefficients.get(outDegree - firstDegree));
                current.add(temp);
            }
            newCoefficients.add(current);
        }

        coefficients = newCoefficients;

        for (int i = coefficients.size() - 1; i > 0; i--)
            if (coefficients.get(i).p == 0)
                coefficients.remove(i);
            else
                break;
    }

    //looks good
    void multiply(Rational rat)
    {
        if (rat.p == 0)
        {
            coefficients.clear();
            coefficients.add(new Rational(0, 1));
            return;
        }

        for (Rational coefficient : coefficients)
            coefficient.multiply(rat);
    }

    Polynomial copy()
    {
        Polynomial out = new Polynomial();
        for (Rational rational : coefficients)
            out.coefficients.add(new Rational(rational.p, rational.q));

        return out;
    }

    Polynomial(){}

    String LaTeX()
    {
        StringBuilder sb = new StringBuilder();

        if (coefficients.size() == 1)
        {
            if (coefficients.get(0).q == 1)
                return "" + coefficients.get(0).p;
            return "(" + coefficients.get(0).p + "/" + coefficients.get(0).q + ")";
        }

        for (int i = coefficients.size() - 1; i > 0; i--)
        {
            if (coefficients.get(i).p == 0)
            {
                continue;
            }
            if (i == 1)
                sb.append(coefficients.get(i) + "x+"); //λ
            else
                sb.append(coefficients.get(i) + "x" + pow(i) + "+"); //λ
        }

        if (coefficients.get(0).p != 0)
            return sb.toString() + coefficients.get(0).p;
        else
            return sb.toString().substring(0, sb.toString().length() - 1);

    }

    String pow(int i)
    {
        return "^{" + i + "}";
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if (coefficients.size() == 1)
        {
            if (coefficients.get(0).q == 1)
                return "" + coefficients.get(0).p;
            return "(" + coefficients.get(0).p + "/" + coefficients.get(0).q + ")";
        }

        for (int i = coefficients.size() - 1; i > 0; i--)
        {
            if (coefficients.get(i).p == 0)
            {
                continue;
            }
            if (i == 1)
                sb.append(coefficients.get(i) + "x+"); //λ
            else
                sb.append(coefficients.get(i) + "x" + superscript(i) + "+"); //λ
        }

        if (coefficients.get(0).p != 0)
        {
            if (coefficients.get(0).q == 1)
                return sb.toString() + coefficients.get(0).p;
            else
                return sb.toString() + "(" + coefficients.get(0).p + "/" + coefficients.get(0).q + ")";
        }
        else
            return sb.toString().substring(0, sb.toString().length() - 1);
    }

    @Override
    public int hashCode()
    {
        Rational sum = new Rational();
        for (Rational coefficient : coefficients)
            sum.add(coefficient);
        return sum.p - sum.q;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (coefficients.size() != ((Polynomial) obj).coefficients.size())
            return false;

        for (int i = 0; i < coefficients.size(); i++)
            if (!coefficients.get(i).equals(((Polynomial) obj).coefficients.get(i)))
                return false;

        return true;
    }

    String[] stringArr = new String[]{"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};

    String superscript(int num)
    {
        if (num == 0)
            return "⁰";
        StringBuilder out = new StringBuilder();
        while (num != 0)
        {
            out.append(stringArr[num % 10]);
            num /= 10;
        }

        return out.reverse().toString();
    }

    Complex[] findRoots()
    {
        int degree = coefficients.size() - 1;
        Complex[] roots = new Complex[degree];

        Complex mult = new Complex(0.984886, 1.152571805);
        roots[0] = new Complex(1, 0);

        for (int i = 1; i < degree; i++)
            roots[i] = roots[i - 1].multiply(mult);

        for (int n = 0; n < 30; n++)
        {
            Complex[] next = new Complex[degree];

            for (int i = 0; i < degree; i++)
            {
                Complex temp = this.evaluate(roots[i]);
                for (int j = 0; j < degree; j++)
                {
                    if (i == j)
                        continue;
                    Complex divisor = roots[i].subtract(roots[j]);
                    temp = temp.multiply(divisor.inverse());
                }
                next[i] = roots[i].subtract(temp);
            }

            System.arraycopy(next, 0, roots, 0, degree);
        }

        return roots;
    }
}
