package com.company;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.company.Main.sumZero;
import static com.company.Main.n;

class Driver
{
    TransitionMatrix tm;

    static ArrayList<State> states;
    static ArrayList<Arrow> arrows = new ArrayList<>();

    //stores all the information regarding "bad cases" (ie the upset criteria doesn't work)
    ArrayList<State> bs1 = new ArrayList<>(), bs2 = new ArrayList<>();
    ArrayList<RESum> p1 = new ArrayList<>(), p2 = new ArrayList<>();
    ArrayList<HashSet<State>> bu = new ArrayList<>();

    ArrayList<HashSet<State>> upsets;
    HashMap<HashSet<State>, HashSet<State>> generators = new HashMap<>();

    HashMap<State, HashSet<State>> partialOrder, temp;
    ArrayList<HashMap<State, HashSet<State>>> partialOrders = new ArrayList<>();

    Driver()
    {
        arrows.add(new Arrow(" does (*→B)", new int[]{1, -1, 0}));
        arrows.add(new Arrow(" does (*→†)", new int[]{0, -1, 1}));
        arrows.add(new Arrow(" does (B→†)", new int[]{-1, 0, 1}));
        arrows.add(new Arrow(" does (B→*)", new int[]{-1, 1, 0}));
        arrows.add(new Arrow(" does (†→*)", new int[]{0, 1, -1}));
        arrows.add(new Arrow(" does (†→B)", new int[]{1, 0, -1}));
        arrows.add(new Arrow(" does (nothing)", new int[]{0, 0, 0}));

        tm = new TransitionMatrix();

        partialOrder = guessAndInitPartialOrder();
        boolean good = false;

        while (!good)
        {
            partialOrders.add(copy(partialOrder));
            initializeMinUpset(partialOrder);
            findUpsets(partialOrder);
            oneStepCoupling(partialOrder);

            good = true;

            for (State s1 : State.blacklist.keySet())
                if (State.blacklist.get(s1).size() > 0)
                {
                    if (!s1.equals(states.get(0)))
                    {
                        partialOrder.get(s1).removeAll(State.blacklist.get(s1));
                        good = false;
                    }
                }

            if (good)
                if (State.blacklist.get(states.get(0)).size() > 0)
                    System.out.println("shippaishita :(");
                else
                {
                    System.out.println("sakusen kanryo!!!");
//                    for (State state : partialOrder.keySet())
//                        System.out.println(state + ": " + partialOrder.get(state));
//                    System.out.println(partialOrder.get(states.get(0)).size());
                }
        }

        partialOrders.add(partialOrder);

        for (State s1 : states)
            for (State s2 : states)
                for (State s3 : states)
                    if (partialOrder.get(s1).contains(s2) && partialOrder.get(s2).contains(s3))
                        if (!partialOrder.get(s1).contains(s3))
                            System.out.println(":((((((((((((");

        //printMinPartialOrder(partialOrder);
    }

    //this will completely mess up the partialOrder!
    void printMinPartialOrder(HashMap<State, HashSet<State>> partialOrder)
    {
        for (State state : partialOrder.keySet())
        {
            ArrayList<State> set = new ArrayList<>(state.minUpset);
            set.remove(state);
            outer:
            for (int i = set.size() - 1; i >= 0; i--)
                for (State otherState : set)
                {
                    if (set.get(i).equals(otherState))
                        continue;
                    if (partialOrder.get(set.get(i)).contains(otherState))
                    {
                        set.remove(i);
                        continue outer;
                    }
                }
            System.out.println(state + ": " + set);
        }
    }

    HashMap<State, HashSet<State>> copy(HashMap<State, HashSet<State>> map)
    {
        HashMap<State, HashSet<State>> out = new HashMap<>();
        for (State state : map.keySet())
            out.put(state, new HashSet<>(map.get(state)));

        return out;
    }

    HashMap<State, HashSet<State>> guessAndInitPartialOrder()
    {
        HashMap<State, HashSet<State>> partialOrder = new HashMap<>();

        boolean[][] geq = new boolean[states.size()][states.size()];

        for (int i = 0; i < geq.length; i++)
            for (int j = 0; j < geq.length; j++)
                geq[i][j] = true; //tm.states.get(i).geq(tm.states.get(j));

        for (double lambda = 1.01; lambda < 100; lambda *= 1.01)
        {
            double[][] arr = tm.evaluate(lambda), temp = new double[arr.length][arr.length];
            for (int i = 0; i < arr.length; i++)
                System.arraycopy(arr[i], 0, temp[i], 0, arr.length);
            for (int pow = 0; pow < 100; pow++)
            {
                arr = multiply(arr, temp);

                for (int i = 0; i < geq.length; i++)
                    for (int j = 0; j < geq.length; j++)
                        geq[i][j] &= arr[i][0] >= arr[j][0];
            }
        }

        for (int i = 0; i < geq.length; i++)
        {
            partialOrder.put(states.get(i), new HashSet<>());
            for (int j = 0; j < geq.length; j++)
                if (geq[i][j])
                    partialOrder.get(states.get(i)).add(states.get(j));
        }

        return partialOrder;
    }

    void findUpsets(HashMap<State, HashSet<State>> partialOrder)
    {
        upsets = new ArrayList<>();
        temp = partialOrder;

        generateAntichains(new HashSet<>(), new HashSet<>(states));

        System.out.println(upsets.size());
    }

    void generateAntichains(HashSet<State> curr, HashSet<State> allowed)
    {
        if (allowed.isEmpty())
        {
            HashSet<State> upset = new HashSet<>();
            for (State state : curr)
                upset.addAll(state.minUpset);
            upsets.add(upset);
            return;
        }

        State next = allowed.iterator().next();
        allowed.remove(next);
        HashSet<State> other = new HashSet<>(curr), otherAllowed = new HashSet<>(allowed);
        generateAntichains(curr, allowed);

        other.add(next);
        otherAllowed.removeAll(next.minUpset);
        otherAllowed.removeAll(temp.get(next));
        generateAntichains(other, otherAllowed);
    }

    HashMap<RESum, HashMap<RESum, Integer>> map = new HashMap<>();

    void oneStepCoupling(HashMap<State, HashSet<State>> partialOrder)
    {
        for (State s1 : states)
            State.blacklist.put(s1, new HashSet<>());

        long time = System.nanoTime();
        System.out.println("sakusen kaishi!");

        for (State s1 : states)
            for (State s2 : states)
                if (partialOrder.get(s1).contains(s2) && !s1.equals(s2))
                {
                    for (HashSet<State> upset : upsets)
                    {
                        RESum p1 = sumZero.multiply(new Rational(1, 1));
                        RESum p2 = sumZero.multiply(new Rational(1, 1));

                        for (Arrow a : arrows)
                        {
                            if (upset.contains(a.map(s1)))
                                p1 = p1.add(tm.map.get(s1).get(a.map(s1)));
                            if (upset.contains(a.map(s2)))
                                p2 = p2.add(tm.map.get(s2).get(a.map(s2)));
                        }

                        int temp;

                        if (map.containsKey(p1) && map.get(p1).containsKey(p2))
                            temp = map.get(p1).get(p2);
                        else
                        {
                            //System.out.println(p1 + "  " + p2);
                            temp = p1.compare(p2);
                            if (!map.containsKey(p1))
                                map.put(p1, new HashMap<>());
                            map.get(p1).put(p2, temp);
                        }

                        if (temp == 2 || temp == 1)
                        {
                            State.blacklist.get(s1).add(s2);
//                            tm.bs1.add(s1);
//                            tm.bs2.add(s2);
//                            tm.bu.add(upset);
//                            tm.p1.add(p1);
//                            tm.p2.add(p2);
//                            System.out.println("s1 = " + s1 + ", s2 = " + s2);
//                            System.out.println("U = " + upset);
//                            System.out.println(p1.LaTeX());
//                            System.out.println(p2.LaTeX());
//                            System.out.println();
                        }
                    }
                }

        System.out.println(((double) (System.nanoTime() - time)) / 1000000000 + "s");
    }

    void twoStepCoupling()
    {
        HashMap<RESum, HashMap<RESum, Integer>> map = new HashMap<>();

        long time = System.nanoTime();
        System.out.println("sakusen kaishi! dainimaku!!!");

        for (int i = 0; i < bs1.size(); i++)
        {
            State s1 = bs1.get(i), s2 = bs2.get(i);
            HashSet<State> upset = bu.get(i);

            RESum p1 = sumZero.multiply(new Rational(1, 1));
            RESum p2 = sumZero.multiply(new Rational(1, 1));

            for (State target : upset)
            {
                p1 = p1.add(tm.map2.get(s1).get(target));
                p2 = p2.add(tm.map2.get(s2).get(target));
            }

            int temp;

            if (map.containsKey(p1) && map.get(p1).containsKey(p2))
                temp = map.get(p1).get(p2);
            else
            {
                //System.out.println(p1 + "  " + p2);
                temp = p1.compare(p2);
                if (!map.containsKey(p1))
                    map.put(p1, new HashMap<>());
                map.get(p1).put(p2, temp);
            }

            if (temp == 2 || temp == 1)
            {
                System.out.println("yikes!");
                System.out.println(p1);
                System.out.println(p2);
//                bs1.add(s1);
//                bs2.add(s2);
//                bu.add(upset);
//                p1.add(p1);
//                p2.add(p2);
            }
        }

        System.out.println(((double) (System.nanoTime() - time)) / 1000000000 + "s");
    }

    boolean equal(HashSet<State> set1, HashSet<State> set2)
    {
        if (set1.size() != set2.size())
            return false;

        for (State s1 : set1)
            if (!set2.contains(s1))
                return false;

        return true;
    }

    void initializeMinUpset(HashMap<State, HashSet<State>> partialOrder)
    {
        for (State s1 : states)
            s1.minUpset = new HashSet<>();
        for (State s1 : states)
            for (State s2 : states)
                if (partialOrder.get(s1).contains(s2))
                    s2.minUpset.add(s1);
    }

    ArrayList<HashSet<State>> powerSet(HashSet<State> set)
    {
        ArrayList<HashSet<State>> out = new ArrayList<>();
        out.add(new HashSet<>());

        for (State s : set)
        {
            ArrayList<HashSet<State>> newList = new ArrayList<>();
            for (HashSet<State> temp : out)
            {
                HashSet<State> withS = new HashSet<>(temp), withoutS = new HashSet<>(temp);
                withS.add(s);
                newList.add(withS);
                newList.add(withoutS);
            }
            out = newList;
        }

        return out;
    }

    void evaluateAndPrint()
    {
        outer: for (n = 6; n < 7; n++)
        {
            System.out.println(n);
            BigDecimal lambda = new BigDecimal(1.1);
            for (int p = 0; p < 100; p++)
            {
                BigDecimal[][] arr = tm.evaluatePrecise(lambda), temp = new BigDecimal[arr.length][arr.length];

                for (int i = 0; i < arr.length; i++)
                    for (int j = 0; j < arr.length; j++)
                        temp[i][j] = arr[i][j].multiply(new BigDecimal(1));

                for (int i = 0; i < 10; i++)
                {
                    boolean bad = false;
                    for (int j = 0; j < arr.length - 1; j++)
                        if (arr[j][0].compareTo(arr[j + 1][0]) < 0 && arr[j + 1][0].subtract(arr[j][0]).compareTo(new BigDecimal(0.001)) > 0)
                            bad = true;
                    if (bad)
                    {
                        for (BigDecimal[] bigDecimals : arr)
                        {
                            BigDecimal sum = new BigDecimal(0);
                            for (BigDecimal bigDecimal : bigDecimals)
                                sum = sum.add(bigDecimal);
                            System.out.println(sum);
                        }
                        for (int w = 0; w < arr.length; w++)
                        {
                            BigDecimal[] bigDecimals = arr[w];
                            System.out.print(states.get(w) + "  ");
                            for (BigDecimal bigDecimal : bigDecimals)
                                System.out.print(new DecimalFormat("#.000000000000").format(bigDecimal) + " ");
                            System.out.println();
                        }
                        System.out.println(n);
                        System.out.println(lambda);
                        System.out.println(i);
                        break outer;
                    }
                    arr = multiply(arr, temp);
                }
                lambda = lambda.multiply(new BigDecimal(1.01));
            }
        }
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

    static ArrayList<int[]> partitions()
    {
        ArrayList<int[]> start = new ArrayList<>(), good = new ArrayList<>();
        for (int i = 0; i <= Main.n; i++)
        {
            int[] temp = new int[3];
            temp[0]= i;
            start.add(temp);
        }

        start = partitions(start, 1);

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
