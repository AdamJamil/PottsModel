package com.company;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import static com.company.Main.*;

class Driver
{
    TransitionMatrix tm;

    static ArrayList<State> states;
    ArrayList<boolean[]> upsets = new ArrayList<>();
    private static ArrayList<Arrow> arrows = new ArrayList<>();
    private static int[][] neighbors;

    //stores all the information regarding "bad cases" (ie the upset criteria doesn't work)
    ArrayList<State> bs1, bs2;
    ArrayList<RESum> p1, p2;
    ArrayList<boolean[]> bu;

    private boolean[][] partialOrder, temp;
    private boolean[][] minUpset;
    private boolean[][] blacklist;
    private boolean[][] minRel;
    ArrayList<boolean[][]> partialOrders = new ArrayList<>();
    private ArrayList<Integer>[] checklist;

    private static final double err = 0.00000000001;
    private long time;

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

        fixPartialOrder();

        System.out.println(new DecimalFormat("#.000").format(((double) (System.nanoTime() - time)) / 1000000000) + "s");

        partialOrders.add(partialOrder);
        checkTransitivity();
        printMinPartialOrder(partialOrder);
    }

    private void fixPartialOrder()
    {
        boolean good = false;

        while (!good)
        {
            partialOrders.add(copy(partialOrder));
            initializeMinUpset(partialOrder);
            initalizeOtherCrapTemp(partialOrder); //TODO: REMOVE THIS LATER OR NOT //DEPENDS ON MEMORY
            getUpset(partialOrder);
            //oneStepCoupling(partialOrder);

            good = true;

            for (int i = 1; i < blacklist.length; i++)
            {
                int idx = -1;
                for (int j = 0; j < blacklist.length; j++)
                    if (blacklist[i][j])
                        partialOrder[i][idx = j] = (blacklist[i][j] = false);
                good &= idx == -1;
            }

            if (good)
            {
                boolean actuallyGood = true;
                for (boolean bArr[] : blacklist)
                    for (boolean b : bArr)
                        actuallyGood &= !b;

                if (!actuallyGood)
                    System.out.println("shippaishita :(");
                else
                    System.out.println("sakusen kanryo!!!");
            }
        }
    }

    private boolean[][] guessAndInitPartialOrder()
    {
        boolean[][] geq = new boolean[states.size()][states.size()];

        for (int i = 0; i < geq.length; i++)
            for (int j = 0; j < geq.length; j++)
            {
                State s1 = states.get(i), s2 = states.get(j);
                int[] o1 = s1.order, o2 = s2.order;
                geq[i][j] = (i == j) || (i == 0) || (o1[0] >= k && o2[0] >= k && s1.geq(s2)) || (o2[2] == 0 && o1[0] == o2[0]) || (o2[2] == 0 && s1.geq(s2));
            }

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

    //maps from pair of (state|neighbors) (format 32-7|7) to result of comparison
    private HashMap<Integer, Boolean> result = new HashMap<>();
    //maps from (state|neighbors) to RESum of probability of going into upset
    private HashMap<Integer, RESum> probability = new HashMap<>();

//    @SuppressWarnings("Duplicates")
//    void oneStepCoupling(boolean[][] partialOrder)
//    {
//        blacklist = new boolean[partialOrder.length][partialOrder.length];
//        boolean[][] minRel = copy(minUpset);
//
//        //init minRel
//        for (int i = 0; i < partialOrder.length; i++)
//        {
//            boolean[] set = minRel[i];
//            set[i] = false;
//
//            outer:
//            for (int j = 0; j < minUpset.length; j++)
//            {
//                if (!set[j])
//                    continue;
//
//                for (int k = 0; k < minUpset.length; k++)
//                {
//                    if (!set[k])
//                        continue;
//
//                    if (j == k)
//                        continue;
//
//                    if (minRel[k][j])
//                    {
//                        set[j] = false;
//                        continue outer;
//                    }
//                }
//            }
//        }
//
//        long time = System.nanoTime();
//        System.out.println("sakusen kaishi!");
//
//        ArrayList<Integer>[] checklist = new ArrayList[states.size()];
//
//        for (int i = 0; i < partialOrder.length; i++)
//        {
//            checklist[i] = new ArrayList<>(partialOrder.length);
//            for (int j = 0; j < partialOrder.length; j++)
//                if (i != j && partialOrder[i][j] && minRel[j][i])
//                    checklist[i].add(j);
//        }
//
//        for (boolean[] upset : upsets)
//        {
//            for (int i = 0; i < partialOrder.length; i++)
//            {
//                int key1 = i << 7;
//                for (int j = 0; j < neighbors[i].length; j++)
//                    if (upset[neighbors[i][j]])
//                        key1 += (1 << j);
//
//                outer:
//                for (int w = checklist[i].size() - 1; w >= 0; w--)
//                {
//                    int j = checklist[i].get(w);
//
//                    int key2 = j << 7;
//                    for (int k = 0; k < neighbors[j].length; k++)
//                        if (upset[neighbors[j][k]])
//                            key2 += (1 << k);
//
//                    boolean temp;
//                    int key = (key1 << 16) + key2;
//
//                    if (result.containsKey(key))
//                        temp = result.get(key);
//                    else
//                    {
//                        RESum p1, p2;
//
//                        if (probability.containsKey(key1))
//                            p1 = probability.get(key1);
//                        else
//                        {
//                            p1 = new RESum();
//                            for (int k = 0; k < 7; k++)
//                                if ((key1 & (1 << k)) != 0)
//                                    p1.add(tm.arr[i][neighbors[i][k]]);
//                            probability.put(key1, p1);
//                        }
//
//                        if (probability.containsKey(key2))
//                            p2 = probability.get(key2);
//                        else
//                        {
//                            p2 = new RESum();
//                            for (int k = 0; k < 7; k++)
//                                if ((key2 & (1 << k)) != 0)
//                                    p2.add(tm.arr[j][neighbors[j][k]]);
//                            probability.put(key2, p2);
//                        }
//
//                        result.put(key, temp = p1.geq(p2));
//                    }
//
//                    if (!temp)
//                    {
//                        blacklist[i][j] = true;
//                        checklist[i].remove(w);
//                    }
//                }
//            }
//        }
//
//        System.out.println(((double) (System.nanoTime() - time)) / 1000000000 + "s");
//    }

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

    private void initializeMinUpset(boolean[][] partialOrder)
    {
        minUpset = new boolean[partialOrder.length][partialOrder.length];

        for (int i = 0; i < partialOrder.length; i++)
            for (int j = 0; j < partialOrder.length; j++)
                minUpset[j][i] = partialOrder[i][j];
    }

    private double[][] multiply(double[][] arr1, double[][] arr2)
    {
        double[][] out = new double[arr1.length][arr2[0].length];

        for (int i = 0; i < out.length; i++)
            for (int j = 0; j < out[0].length; j++)
                for (int k = 0; k < arr1.length; k++)
                    out[i][j] += arr1[i][k] * arr2[k][j];

        return out;
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

    private static ArrayList<int[]> partitions(ArrayList<int[]> list, int pos)
    {
        if (pos == 3)
            return list;

        ArrayList<int[]> out = new ArrayList<>();

        for (int[] ints : list)
        {
            for (int i = 0; i <= n; i++)
            {
                int[] temp = new int[3];
                System.arraycopy(ints, 0, temp, 0, pos);
                temp[pos] = i;
                out.add(temp);
            }
        }

        return partitions(out, pos + 1);
    }

    private void initializePow()
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

    private void initializeNeighbors()
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

    private void initializeProbArr()
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

    private void checkTransitivity()
    {
        for (int i = 0; i < states.size(); i++)
            for (int j = 0; j < states.size(); j++)
                for (int k = 0; k < states.size(); k++)
                    if (partialOrder[i][j] && partialOrder[j][k] && !partialOrder[i][k])
                        System.out.println("not transitive :(");
    }

    private void printMinPartialOrder(boolean[][] partialOrder)
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

    private void getUpset(boolean[][] partialOrder)
    {
        //upsets = new ArrayList<>();
        temp = partialOrder;

        boolean[] allowed = new boolean[partialOrder.length];
        for (int i = 0; i < partialOrder.length; i++)
            allowed[i] = true;

        generateAntichains(new boolean[partialOrder.length], allowed);

        //System.out.println(upsets.size());
    }

    private void generateAntichains(boolean[] curr, boolean[] allowed)
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
            boolean[] upset = new boolean[partialOrder.length], cp = new boolean[partialOrder.length];
            for (int i = 0; i < curr.length; i++)
                if (curr[i])
                    for (int j = 0; j < minUpset.length; j++)
                        upset[j] |= minUpset[i][j];

            System.arraycopy(upset, 0, cp, 0, upset.length);
            upsets.add(cp);

            for (int i = 0; i < partialOrder.length; i++)
            {
                int key1 = i << 7;
                for (int j = 0; j < neighbors[i].length; j++)
                    if (upset[neighbors[i][j]])
                        key1 += (1 << j);

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

    private void initalizeOtherCrapTemp(boolean[][] partialOrder)
    {
        blacklist = new boolean[partialOrder.length][partialOrder.length];
        minRel = copy(minUpset);

        //init minRel
        for (int i = 0; i < partialOrder.length; i++)
        {
            boolean[] set = minRel[i];
            set[i] = false;

            outer:
            for (int j = 0; j < minUpset.length; j++)
            {
                if (!set[j])
                    continue;

                for (int k = 0; k < minUpset.length; k++)
                {
                    if (!set[k])
                        continue;

                    if (j == k)
                        continue;

                    if (minRel[k][j])
                    {
                        set[j] = false;
                        continue outer;
                    }
                }
            }
        }

        time = System.nanoTime();
        System.out.println("sakusen kaishi!");

        checklist = new ArrayList[states.size()];

        for (int i = 0; i < partialOrder.length; i++)
        {
            checklist[i] = new ArrayList<>(partialOrder.length);
            for (int j = 0; j < partialOrder.length; j++)
                if (i != j && partialOrder[i][j])// && minRel[j][i])
                    checklist[i].add(j);
        }
    }
}