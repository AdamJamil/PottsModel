package com.company;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

class Polynomial
{
    int degree = 0;
    ArrayList<Rational> coefficients = new ArrayList<>();
    ArrayList<Polynomial> factors = new ArrayList<>();
    private static final double err = 0.000001;

    int compare()
    {
        if (degree == 0)
        {
            if (coefficients.get(0).value() == 0)
                return 3;
            if (coefficients.get(0).value() > 0)
                return 0;
            if (coefficients.get(0).value() < 0)
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

    Polynomial derivative()
    {
        Polynomial derivative = new Polynomial();
        if (degree == 0)
        {
            derivative.coefficients.add(new Rational());
            return derivative;
        }
        derivative.degree = degree - 1;
        for (int i = 1; i < coefficients.size(); i++)
            derivative.coefficients.add(coefficients.get(i).multiply(new Rational(i, 1)));
        return derivative;
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
        double result = coefficients.get(0).value();

        for (int i = 1; i < coefficients.size(); i++)
            result = (result * lambda) + coefficients.get(i).value();

        return result;
    }

    Polynomial add(Polynomial other)
    {
        Polynomial out = new Polynomial();

        for (int i = 0; i < Math.max(coefficients.size(), other.coefficients.size()); i++)
        {
            Rational temp = new Rational();
            if (i < coefficients.size())
                temp = temp.add(coefficients.get(i));
            if (i < other.coefficients.size())
                temp = temp.add(other.coefficients.get(i));
            out.coefficients.add(temp);
        }

        for (int i = out.coefficients.size() - 1; i > 0; i--)
            if (out.coefficients.get(i).p == 0)
                out.coefficients.remove(i);
            else
                break;

        out.degree = out.coefficients.size() - 1;

        if (out.degree == 1 && out.coefficients.get(0).p == 0 && out.coefficients.size() == 1)
            degree = 0;

        return out;
    }

    Polynomial multiply(Polynomial other)
    {
        return new Polynomial(this, other);
    }

    Polynomial multiply(Rational rat)
    {
        Polynomial out = new Polynomial();

        if (rat.p == 0)
        {
            out.coefficients.add(new Rational(0, 1));
            return out;
        }

        for (int i = 0; i < coefficients.size(); i++)
            out.coefficients.add(coefficients.get(i).multiply(rat));
        out.degree = out.coefficients.size() - 1;

        return out;
    }

    Polynomial()
    {
        factors.add(this);
    }

    Polynomial(Polynomial f, Polynomial g)
    {
        degree = f.degree + g.degree;

        for (int outDegree = 0; outDegree <= degree; outDegree++)
        {
            Rational current = new Rational();
            for (int firstDegree = Math.max(0, outDegree - g.degree); firstDegree <= Math.min(outDegree, f.degree); firstDegree++)
                current = current.add(f.coefficients.get(firstDegree).multiply(g.coefficients.get(outDegree - firstDegree)));
            coefficients.add(current);
        }

        for (int i = coefficients.size() - 1; i > 0; i--)
            if (coefficients.get(i).p == 0)
                coefficients.remove(i);
            else
                break;

        degree = coefficients.size() - 1;

        if (degree == 1 && coefficients.get(0).p == 0 && coefficients.size() == 1)
            degree = 0;
    }

    String LaTeX()
    {
        StringBuilder sb = new StringBuilder();

        if (degree == 0)
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

        if (degree == 0)
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
            sum = sum.add(coefficient);
        return sum.p - sum.q;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (degree != ((Polynomial) obj).degree)
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
            if (degree == 0)
                return new Complex[0];

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
