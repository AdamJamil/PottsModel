package com.company;

import java.text.DecimalFormat;

class Complex
{
    double r, theta;

    Complex inverse()
    {
        return new Complex(1 / r, (2 * Math.PI) - theta);
    }

    Complex multiply(Complex other)
    {
        return new Complex(r * other.r, (theta + other.theta) % (2 * Math.PI));
    }

    Complex subtract(Complex other)
    {
        double x1 = r * Math.cos(theta), x2 = other.r * Math.cos(other.theta);
        double y1 = r * Math.sin(theta), y2 = other.r * Math.sin(other.theta);
        double angle = Math.atan2(y1 - y2, x1 - x2);
        if (angle < 0)
            angle += 2 * Math.PI;

        return new Complex(Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2))), angle);
    }

    Complex add(Complex other)
    {
        double x1 = r * Math.cos(theta), x2 = other.r * Math.cos(other.theta);
        double y1 = r * Math.sin(theta), y2 = other.r * Math.sin(other.theta);
        double angle = Math.atan2(y1 + y2, x1 + x2);
        if (angle < 0)
            angle += 2 * Math.PI;

        return new Complex(Math.sqrt(((x1 + x2) * (x1 + x2)) + ((y1 + y2) * (y1 + y2))), angle);
    }

    Complex(double a, double b)
    {
        r = a;
        theta = b;
    }

    @Override
    public String toString()
    {
        return "(" + new DecimalFormat("#.000").format(r) + ", " + new DecimalFormat("#.000").format(theta / Math.PI) + " pi)";
    }
}