package com.company;

import java.util.ArrayList;

class Polynomial
{
    ArrayList<Rational> coefficients = new ArrayList<>();
    private static final double err = 0.000000001;

    boolean geqZero()
    {
        if (coefficients.size() == 1)
            return coefficients.get(0).value() >= 0;

        if (coefficients.get(coefficients.size() - 1).value() < -err || evaluateOne() < -err)
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

        if (evaluate(1.6) < -err || evaluate(2) < -err)
            return false;

        while (coefficients.get(0).p == 0)
            coefficients.remove(0);

        double[] taylor = new double[coefficients.size()], arr = new double[coefficients.size()];
        int w = coefficients.size();

        for (int i = 0; i < w; i++)
            arr[i] = coefficients.get(i).value();

        for (int i = 0; i < w; i++)
            for (int j = i; j < w; j++)
            {
                taylor[i] += arr[j];
                arr[j] *= (j - i) / (double)(i + 1);
            }

        domCoeff = 0;
        dominated = true;
        for (int i = taylor.length - 1; i >= 0; i--)
        {
            domCoeff += taylor[i];
            if (domCoeff < err)
            {
                dominated = false;
                break;
            }
        }

        if (dominated)
            return true;

        System.out.println(this); //yikes !

        return false;
    }

    double evaluate(double lambda)
    {
        double result = coefficients.get(coefficients.size() - 1).value();

        for (int i = coefficients.size() - 2; i >= 0; i--)
            result = (result * lambda) + coefficients.get(i).value();

        return result;
    }

    private double evaluateOne()
    {
        double sum = 0;
        for (Rational coefficient : coefficients)
            sum += coefficient.value();
        return sum;
    }

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

    void multiply(Polynomial other)
    {
        ArrayList<Rational> newCoefficients = new ArrayList<>();

        for (int outDegree = 0; outDegree <= coefficients.size() + other.coefficients.size() - 2; outDegree++)
        {
            Rational current = new Rational();
            for (int firstDegree = Math.max(0, outDegree - other.coefficients.size() + 1); firstDegree <= Math.min(outDegree, coefficients.size() - 1); firstDegree++)
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
                continue;
            sb.append(coefficients.get(i)).append("x");
            if (i != 1)
                sb.append(pow(i));
            sb.append("+");
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

    private String pow(int i)
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
                continue;

            sb.append("x");
            if (i != 1)
                sb.append("^").append(i);
            sb.append("+");
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
        return (int) (sum.p - sum.q);
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
}