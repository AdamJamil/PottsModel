package com.company;

import java.math.BigDecimal;
import java.util.ArrayList;

class Polynomial
{
    int degree = 0;
    ArrayList<Rational> coefficients = new ArrayList<>();
    ArrayList<Polynomial> factors = new ArrayList<>();

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

    double evaluate(double lambda)
    {
        double sum = 0;

        for (int i = 0; i < coefficients.size(); i++)
            sum += Math.pow(lambda, i) * coefficients.get(i).p / (double) coefficients.get(i).q;

        return sum;
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
            return sb.toString() + coefficients.get(0).p;
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
}
