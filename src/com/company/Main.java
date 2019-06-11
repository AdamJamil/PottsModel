package com.company;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public class Main
{
    static int n = 2;
    static Polynomial zero, one, x;
    static RESum sumZero;

    Main() throws Exception
    {
        TransitionMatrix.arrows.add(new Arrow("↑", new int[]{1, -1, 0}));
        TransitionMatrix.arrows.add(new Arrow("→", new int[]{0, -1, 1}));
        TransitionMatrix.arrows.add(new Arrow("↘", new int[]{-1, 0, 1}));
        TransitionMatrix.arrows.add(new Arrow("↓", new int[]{-1, 1, 0}));
        TransitionMatrix.arrows.add(new Arrow("←", new int[]{0, 1, -1}));
        TransitionMatrix.arrows.add(new Arrow("↖", new int[]{1, 0, -1}));
        TransitionMatrix.arrows.add(new Arrow("-", new int[]{0, 0, 0}));

        TransitionMatrix tm = new TransitionMatrix();
        tm.findCoupling();

//        outer: for (n = 8; n < 30; n++)
//        {
//            System.out.println(n);
//            BigDecimal lambda = new BigDecimal(100);
//            for (int p = 0; p < 1; p++)
//            {
//                TransitionMatrix tm = new TransitionMatrix();
//                BigDecimal[][] arr = tm.evaluatePrecise(lambda), temp = new BigDecimal[arr.length][arr.length];
//
//                for (int i = 0; i < arr.length; i++)
//                    for (int j = 0; j < arr.length; j++)
//                        temp[i][j] = arr[i][j].multiply(new BigDecimal(1));
//
//                for (int i = 0; i < 10; i++)
//                {
//                    boolean bad = false;
//                    for (int j = 0; j < arr.length - 1; j++)
//                        if (arr[j][0].compareTo(arr[j + 1][0]) < 0 && arr[j + 1][0].subtract(arr[j][0]).compareTo(new BigDecimal(0.001)) > 0)
//                            bad = true;
//                    if (bad)
//                    {
//                        for (BigDecimal[] bigDecimals : arr)
//                        {
//                            BigDecimal sum = new BigDecimal(0);
//                            for (BigDecimal bigDecimal : bigDecimals)
//                                sum = sum.add(bigDecimal);
//                            System.out.println(sum);
//                        }
//                        for (int w = 0; w < arr.length; w++)
//                        {
//                            BigDecimal[] bigDecimals = arr[w];
//                            System.out.print(tm.states.get(w) + "  ");
//                            for (BigDecimal bigDecimal : bigDecimals)
//                                System.out.print(new DecimalFormat("#.000000000000").format(bigDecimal) + " ");
//                            System.out.println();
//                        }
//                        System.out.println(n);
//                        System.out.println(lambda);
//                        System.out.println(i);
//                        break outer;
//                    }
//                    arr = multiply(arr, temp);
//                }
//                lambda = lambda.multiply(new BigDecimal(1.01));
//            }
//        }
    }

    BigDecimal[][] multiply(BigDecimal[][] arr1, BigDecimal[][] arr2)
    {
        BigDecimal[][] out = new BigDecimal[arr1.length][arr2[0].length];

        for (int i = 0; i < out.length; i++)
            for (int j = 0; j < out[0].length; j++)
            {
                out[i][j] = new BigDecimal(0);
                for (int k = 0; k < arr1.length; k++)
                    out[i][j] = out[i][j].add(arr1[i][k].multiply(arr2[k][j]));
            }

        return out;
    }

    double[][] multiply(double[][] arr1, double[][] arr2)
    {
        double[][] out = new double[arr1.length][arr2[0].length];

        for (int i = 0; i < out.length; i++)
            for (int j = 0; j < out[0].length; j++)
                for (int k = 0; k < arr1.length; k++)
                    out[i][j] += arr1[i][k] * arr2[k][j];

        return out;
    }

    void printBS(ArrayList<State> states, ArrayList<RationalExpression>[][] arr)
    {
        ArrayList<String> lines = new ArrayList<>();
        int stateMax = 0;

        for (State state : states)
            stateMax = Math.max(stateMax, state.toString().length());

        String firstLine = " ";
        for (int i = 0; i < stateMax; i++)
            firstLine += " ";
        lines.add(firstLine);

        for (int i = 0; i < states.size(); i++)
        {
            String temp = states.get(i).toString();
            while (temp.length() < stateMax + 1)
                temp += " ";
            lines.add(temp);
        }

        for (int stateIndex = 0; stateIndex < states.size(); stateIndex++)
        {
            State state = states.get(stateIndex);
            int max = state.toString().length();

            for (ArrayList<RationalExpression> list : arr[stateIndex])
            {
                int temp = -1; //only spaces in the middle
                for (RationalExpression rationalExpression : list)
                    temp += rationalExpression.toString().length() + 1;
                max = Math.max(max, temp);
            }

            String stateString = state.toString();
            while (max - stateString.length() > 1)
                stateString = " " + stateString + " ";
            if (max - stateString.length() == 1)
                stateString = " " + stateString;
            lines.set(0, lines.get(0) + stateString + " ");

            for (int i = 0; i < states.size(); i++)
            {
                String temp = arr[stateIndex][i].toString();
                while (max - temp.length() > 1)
                    temp = " " + temp + " ";
                if (max - temp.length() == 1)
                    temp = " " + temp;
                lines.set(i + 1, lines.get(i + 1) + temp + " ");
            }
        }

        for (String line : lines)
            System.out.println(line);
    }

    public static void main(String[] args) throws Exception
    {
        new Main();
    }

    static ArrayList<int[]> partitions()
    {
        ArrayList<int[]> start = new ArrayList<>(), good = new ArrayList<>();
        for (int i = 0; i <= Main.n; i++)
        {
            int[] temp = new int[3];
            temp[0]= i;
            start.add(temp);
        }

        start = Main.partitions(start, 1);

        for (int[] ints : start)
        {
            int a = ints[0], b = ints[1], c = ints[2];
            if (a + b + c == n && a >= b && b >= c)
                good.add(ints);
        }

        return good;
    }

    static ArrayList<int[]> partitions(ArrayList<int[]> list, int pos)
    {
        if (pos == 3)
            return list;

        ArrayList<int[]> out = new ArrayList<>();

        for (int[] ints : list)
        {
            for (int i = 0; i <= n; i++)
            {
                int[] temp = new int[3];
                for (int j = 0; j < pos; j++)
                    temp[j] = ints[j];
                temp[pos] = i;
                out.add(temp);
            }
        }

        return partitions(out, pos + 1);
    }

    void printDenominators()
    {
        for (n = 2; n < 20; n++)
        {
            ArrayList<int[]> start = new ArrayList<>(), good = new ArrayList<>();

            for (int i = 0; i <= n; i++)
            {
                int[] temp = new int[3];
                temp[0] = i;
                start.add(temp);
            }

            start = partitions(start, 1);

            for (int[] ints : start)
            {
                int a = ints[0], b = ints[1], c = ints[2];
                if (a + b + c == n - 1 && a >= b && b >= c)
                    good.add(ints);
            }

            Polynomial one = new Polynomial();
            one.degree = 0;
            one.coefficients.add(new Rational(1, 1));

            Polynomial x = new Polynomial();
            x.degree = 1;
            x.coefficients.add(new Rational());
            x.coefficients.add(new Rational(1, 1));

            Polynomial ans = one.multiply(one);
            for (int[] ints : good)
            {
                Polynomial temp = new Polynomial();
                temp.coefficients.add(new Rational());
                for (int i = 0; i < 3; i++)
                {
                    if (ints[i] == 0)
                        temp = temp.add(one);
                    else
                    {
                        Polynomial pow = one.multiply(one);
                        for (int j = 0; j < ints[i]; j++)
                            pow = pow.multiply(x);
                        temp = temp.add(pow);
                    }
                }
                //System.out.println(temp);
                //ans = ans.multiply(temp);
                //System.out.println();
                System.out.print("(" + temp + ")");
            }
            System.out.println();
        }
    }
}
