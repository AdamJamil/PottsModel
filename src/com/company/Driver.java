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
    static int[][] neighbors;

    //stores all the information regarding "bad cases" (ie the upset criteria doesn't work)
    ArrayList<State> bs1, bs2;
    ArrayList<RESum> p1, p2;
    ArrayList<boolean[]> bu;

    ArrayList<boolean[]> upsets;
    HashMap<HashSet<State>, HashSet<State>> generators = new HashMap<>();

    boolean[][] partialOrder, temp;
    boolean[][] minUpset;
    boolean[][] blacklist;
    ArrayList<boolean[][]> partialOrders = new ArrayList<>();

    private static final double err = 0.00000000001;

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
        initializeNeighbors();
        initializePow();
        initializeProbArr();
        partialOrder = guessAndInitPartialOrder();
//        initializeMinUpset(partialOrder);
//        findUpsets(partialOrder);
//        oneStepCoupling(partialOrder);
        fixPartialOrder();
        partialOrders.add(partialOrder);
        checkTransitivity();
        printMinPartialOrder(partialOrder);
//        for (boolean[] booleans : partialOrder)
//        {
//            for (boolean aBoolean : booleans)
//                System.out.print(aBoolean);
//            System.out.println();
//        }

//        for (RESum reSum : map.keySet())
//        {
//            for (RESum sum : map.get(reSum).keySet())
//            {
//                System.out.println(map.get(reSum).get(sum) + ": " + reSum + ",   " + sum);
//            }
//
    }

    void fixPartialOrder()
    {
        boolean good = false;

        while (!good)
        {
            partialOrders.add(copy(partialOrder));
            initializeMinUpset(partialOrder);
            findUpsets(partialOrder);
            oneStepCoupling(partialOrder);

            good = true;

            for (int i = 1; i < blacklist.length; i++)
            {
                int idx = -1;
                for (int j = 0; j < blacklist.length; j++)
                    if (blacklist[i][j])// && !states.get(i).geq(states.get(j)))
                        partialOrder[i][idx = j] = false;
                good &= idx == -1;
            }

            if (good)
            {
                boolean actuallyGood = true;
                for (boolean b : blacklist[0])
                    actuallyGood &= !b;

                if (!actuallyGood)
                    System.out.println("shippaishita :(");
                else
                    System.out.println("sakusen kanryo!!!");
            }
        }
    }

    void initializeProbArr()
    {
        tm.arr = new RESum[states.size()][states.size()];

        for (int i = 0; i < states.size(); i++)
        {
            State s1 = states.get(i);

            for (Arrow arrow : arrows)
                if (arrow.valid(s1))
                    tm.arr[i][states.indexOf(arrow.map(s1))] = tm.map.get(states.get(i)).get(arrow.map(s1)).copy();
        }

        for (RESum[] reSums : tm.arr)
            for (RESum reSum : reSums)
                if (reSum != null)
                    reSum.simplify();
    }

    void checkTransitivity()
    {
        for (int i = 0; i < states.size(); i++)
            for (int j = 0; j < states.size(); j++)
                for (int k = 0; k < states.size(); k++)
                    if (partialOrder[i][j] && partialOrder[j][k] && !partialOrder[i][k])
                        System.out.println("not transitive :(");
    }

    void printMinPartialOrder(boolean[][] partialOrder)
    {
        partialOrder = copy(partialOrder);
        for (int i = 0; i < partialOrder.length; i++)
        {
            boolean[] set = minUpset[i];
            set[i] = false;

            outer: for (int j = 0; j < minUpset.length; j++)
            {
                if (!set[j])
                    continue;

                for (int k = 0; k < minUpset.length; k++)
                {
                    if (!set[k])
                        continue;

                    if (j == k)
                        continue;

                    if (minUpset[k][j])
                    {
                        set[j] = false;
                        continue outer;
                    }
                }
            }

            System.out.print(states.get(i) + ": ");
            for (int j = 0; j < minUpset[i].length; j++)
                if (minUpset[i][j])
                    System.out.print(states.get(j) + " ");
            System.out.println();
        }
    }

    boolean[][] copy(boolean[][] arr)
    {
        boolean[][] out = new boolean[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; i++)
            System.arraycopy(arr[i], 0, out[i], 0, arr[i].length);

        return out;
    }

    boolean[][] guessAndInitPartialOrder()
    {
        boolean[][] geq = new boolean[states.size()][states.size()];

        for (int i = 0; i < geq.length; i++)
            for (int j = 0; j < geq.length; j++)
                geq[i][j] = true;//states.get(i).geq(states.get(j));

        for (double lambda = 1.01; lambda < 100; lambda *= 1.01)
        {
            double[][] arr = tm.evaluate(lambda), temp = new double[arr.length][arr.length];
            for (int i = 0; i < arr.length; i++)
                System.arraycopy(arr[i], 0, temp[i], 0, arr[i].length);
            for (int pow = 0; pow < 100; pow++)
            {
                arr = multiply(arr, temp);

                for (int i = 0; i < geq.length; i++)
                    for (int j = 0; j < geq.length; j++)
                        geq[i][j] &= arr[i][0] + err >= arr[j][0];
            }
        }

        return geq;
    }

    void findUpsets(boolean[][] partialOrder)
    {
        upsets = new ArrayList<>();
        temp = partialOrder;

        boolean[] allowed = new boolean[partialOrder.length];
        for (int i = 0; i < partialOrder.length; i++)
            allowed[i] = true;

        generateAntichains(new boolean[partialOrder.length], allowed);

        System.out.println(upsets.size());
    }

    void generateAntichains(boolean[] curr, boolean[] allowed)
    {
        int idx = -1;
        for (int i = 0; i < allowed.length; i++)
            if (allowed[i])
            {
                idx = i;
                break;
            }

        if (idx == -1)
        {
            boolean[] upset = new boolean[partialOrder.length];
            for (int i = 0; i < curr.length; i++)
                if (curr[i])
                    for (int j = 0; j < minUpset.length; j++)
                        upset[j] |= minUpset[i][j];

            upsets.add(upset);
            return;
        }

        allowed[idx] = false;
        boolean[] other = new boolean[curr.length], otherAllowed = new boolean[allowed.length];
        System.arraycopy(curr, 0, other, 0, curr.length);
        System.arraycopy(allowed, 0, otherAllowed, 0, allowed.length);
        generateAntichains(curr, allowed);

        other[idx] = true;
        for (int i = 0; i < otherAllowed.length; i++)
            if (minUpset[idx][i] || temp[idx][i])
                otherAllowed[i] = false;

        generateAntichains(other, otherAllowed);
    }

    //maps from pair of (state|neighbors) (format 32-7|7) to result of comparison
    HashMap<Integer, Boolean> result = new HashMap<>();
    //maps from (state|neighbors) to RESum of probability of going into upset
    HashMap<Integer, RESum> probability = new HashMap<>();

    @SuppressWarnings("Duplicates")
    void oneStepCoupling(boolean[][] partialOrder)
    {
        blacklist = new boolean[partialOrder.length][partialOrder.length];

        long time = System.nanoTime();
        System.out.println("sakusen kaishi!");

        ArrayList<Integer>[] checklist = new ArrayList[states.size()];

        for (int i = 0; i < partialOrder.length; i++)
        {
            checklist[i] = new ArrayList<>(partialOrder.length);
            for (int j = 0; j < partialOrder.length; j++)
                if (i != j && partialOrder[i][j])
                    checklist[i].add(j);
        }

        for (boolean[] upset : upsets)
            for (int i = 0; i < partialOrder.length; i++)
            {
                int key1 = i << 7;
                for (int j = 0; j < neighbors[i].length; j++)
                    if (upset[neighbors[i][j]])
                        key1 += (1 << j);

                outer:
                for (int w = checklist[i].size() - 1; w >= 0; w--)
                {
                    int j = checklist[i].get(w);

                    int key2 = j << 7;
                    for (int k = 0; k < neighbors[j].length; k++)
                        if (upset[neighbors[j][k]])
                            key2 += (1 << k);

                    boolean temp;
                    int key = (key1 << 16) + key2;

                    if (result.containsKey(key))
                        temp = result.get(key);
                    else
                    {
                        RESum p1, p2;

                        if (probability.containsKey(key1))
                            p1 = probability.get(key1);
                        else
                        {
                            p1 = new RESum();
                            for (int k = 0; k < 7; k++)
                                if ((key1 & (1 << k)) != 0)
                                    p1.add(tm.arr[i][neighbors[i][k]]);
                            probability.put(key1, p1);
                        }

                        if (probability.containsKey(key2))
                            p2 = probability.get(key2);
                        else
                        {
                            p2 = new RESum();
                            for (int k = 0; k < 7; k++)
                                if ((key2 & (1 << k)) != 0)
                                    p2.add(tm.arr[j][neighbors[j][k]]);
                            probability.put(key2, p2);
                        }

                        result.put(key, temp = p1.geq(p2));
                    }

                    if (!temp)
                    {
                        blacklist[i][j] = true;
                        checklist[i].remove(w);
                    }
                }
            }

        System.out.println(((double) (System.nanoTime() - time)) / 1000000000 + "s");
    }

//    void twoStepCoupling()
//    {
//        HashMap<RESum, HashMap<RESum, Integer>> map = new HashMap<>();
//
//        long time = System.nanoTime();
//        System.out.println("sakusen kaishi! dainimaku!!!");
//
//        for (int i = 0; i < bs1.size(); i++)
//        {
//            State s1 = bs1.get(i), s2 = bs2.get(i);
//            boolean[] upset = bu.get(i);
//
//            RESum p1 = sumZero.copy();
//            RESum p2 = sumZero.copy();
//
//            for (State target : upset)
//            {
//                p1.add(tm.map2.get(s1).get(target));
//                p2.add(tm.map2.get(s2).get(target));
//            }
//
//            int temp;
//
//            if (map.containsKey(p1) && map.get(p1).containsKey(p2))
//                temp = map.get(p1).get(p2);
//            else
//            {
//                //System.out.println(p1 + "  " + p2);
//                temp = p1.compare(p2);
//                if (!map.containsKey(p1))
//                    map.put(p1, new HashMap<>());
//                map.get(p1).put(p2, temp);
//            }
//
//            if (temp == 2 || temp == 1)
//            {
//                System.out.println("yikes!");
//                System.out.println(p1);
//                System.out.println(p2);
////                bs1.add(s1);
////                bs2.add(s2);
////                bu.add(upset);
////                p1.add(p1);
////                p2.add(p2);
//            }
//        }
//
//        System.out.println(((double) (System.nanoTime() - time)) / 1000000000 + "s");
//    }

    boolean equal(HashSet<State> set1, HashSet<State> set2)
    {
        if (set1.size() != set2.size())
            return false;

        for (State s1 : set1)
            if (!set2.contains(s1))
                return false;

        return true;
    }

    void initializeMinUpset(boolean[][] partialOrder)
    {
        minUpset = new boolean[partialOrder.length][partialOrder.length];

        for (int i = 0; i < partialOrder.length; i++)
            for (int j = 0; j < partialOrder.length; j++)
                minUpset[j][i] = partialOrder[i][j];
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

    void initializePow()
    {
        Polynomial.pow = new ArrayList<>();
        Polynomial.pow.add(Main.one.copy());

        for (int i = 1; i <= 100; i++)
        {
            Polynomial temp = Polynomial.pow.get(i - 1).copy();
            temp.multiply(Main.x);
            Polynomial.pow.add(temp);
        }
    }

    void initializeNeighbors()
    {
        neighbors = new int[states.size()][];

        for (int i = 0; i < states.size(); i++)
        {
            ArrayList<Integer> temp = new ArrayList<>();
            for (int j = 0; j < arrows.size(); j++)
                if (arrows.get(j).valid(states.get(i)))
                    temp.add(j);

            neighbors[i] = new int[temp.size()];
            for (int j = 0; j < neighbors[i].length; j++)
                neighbors[i][j] = states.indexOf(arrows.get(temp.get(j)).map(states.get(i)));
        }
    }
}