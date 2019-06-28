package com.company;

import java.util.ArrayList;
import java.util.PriorityQueue;

class Driver
{
    PriorityQueue<PartialOrder> pq = new PriorityQueue<>();

    TransitionMatrix tm;

    static ArrayList<State> states;
    private int s;
    ArrayList<boolean[]> upsets = new ArrayList<>();

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
    private static boolean flag = true;

    Driver()
    {
        tm = new TransitionMatrix();
        s = states.size();

        reducePartialOrder(partialOrder = initPartialOrder());
    }

    private boolean[][] initPartialOrder()
    {
        boolean[][] geq = new boolean[s][s];

        for (int i = 0; i < s; i++)
            for (int j = 0; j < s; j++)
                geq[i][j] = true;//states.get(i).geq(states.get(j));

        for (double lambda = 1.01; lambda < 100; lambda *= 1.01)
        {
            double[][] arr = tm.evaluate(lambda), temp = new double[s][s];
            for (int i = 0; i < s; i++)
                System.arraycopy(arr[i], 0, temp[i], 0, s);
            for (int pow = 0; pow < 100; pow++)
            {
                arr = multiply(arr, temp);

                for (int i = 0; i < s; i++)
                    for (int j = 0; j < s; j++)
                        geq[i][j] &= arr[i][0] + err >= arr[j][0];
            }
        }

        return geq;
    }

    private void reducePartialOrder(boolean[][] partialOrder)
    {
        initializeMinUpset(partialOrder);
        initalizeOtherCrapTemp(partialOrder);
        getUpset(partialOrder);
        partialOrders.add(partialOrder);

        if (flag)
        {
            System.out.println("yatta!");
            return;
        }

        while (true)
        {
            flag = true;
            if (pq.peek() == null)
            {
                System.out.println("mitsukarenai");
                break;
            }
            partialOrders.add(this.partialOrder = pq.poll().partialOrder);

            initializeMinUpset(this.partialOrder);
            initalizeOtherCrapTemp(this.partialOrder);

            getUpset(this.partialOrder);

            if (flag)
            {
                System.out.println("yatta!!");
                break;
            }
        }
    }

    private void initializeMinUpset(boolean[][] partialOrder)
    {
        minUpset = new boolean[s][s];

        for (int i = 0; i < s; i++)
            for (int j = 0; j < s; j++)
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

    private void printMinPartialOrder(boolean[][] partialOrder)
    {
        partialOrder = copy(partialOrder);
        for (int i = 0; i < s; i++)
        {
            boolean[] set = minUpset[i];
            set[i] = false;

            outer: for (int j = 0; j < s; j++)
            {
                if (!set[j])
                    continue;

                for (int k = 0; k < s; k++)
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
            for (int j = 0; j < s; j++)
                if (minUpset[i][j])
                    System.out.print(states.get(j) + " ");
            System.out.println();
        }
    }

    private boolean done;

    private void getUpset(boolean[][] partialOrder)
    {
        //System.out.println("hajimaruyo!");
        //System.out.println(pq.size());
        temp = partialOrder;

        boolean[] allowed = new boolean[s];
        for (int i = 0; i < s; i++)
            allowed[i] = true;

        done = false;
        generateAntichains(new boolean[s], allowed);
    }

    private void generateAntichains(boolean[] curr, boolean[] allowed)
    {
        int idx = -1;
        for (int i = 0; i < s; i++)
            if (allowed[i])
            {
                idx = i;
                break;
            }

        if (idx == -1)
        {
            boolean[] upset = new boolean[s];
            for (int i = 0; i < s; i++)
                if (curr[i])
                    for (int j = 0; j < s; j++)
                        upset[j] |= minUpset[i][j];

            for (int i = 0; i < s; i++)
            {
                int key1 = i << 7;
                for (int j = 0; j < tm.neighbors[i].length; j++)
                    if (upset[tm.neighbors[i][j]])
                        key1 += (1 << j);

                for (int w = checklist[i].size() - 1; w >= 0; w--)
                {
                    int j = checklist[i].get(w);

                    int key2 = j << 7;
                    for (int k = 0; k < tm.neighbors[j].length; k++)
                        if (upset[tm.neighbors[j][k]])
                            key2 += (1 << k);

                    boolean temp = tm.cmp(key1, key2);

                    if (!temp)
                    {
                        int c1 = 0;
//                        for (boolean[] booleans : partialOrder)
//                            for (boolean aBoolean : booleans)
//                                System.out.print((aBoolean) ? 1 : 0);
//                        System.out.println();

                        flag = false;
                        //compute M(U \cap (L(s_1) \cup L(s_2)))
                        for (int k = 0; k < s; k++)
                            upset[k] &= tm.arr[i][k] != null || tm.arr[j][k] != null;
                        for (int k = 0; k < s; k++)
                            if (upset[k])
                                for (int l = 0; l < s; l++)
                                    upset[l] |= minUpset[k][l];

                        //want to look at all minimal collection of arrows that can be removed to fix case
                        //one is obvious: s1 \geq s2
                        //next we consider all arrows from g(U) (modified U) to (U \cap L(s2)) \setminus g(U)
                        //any minimal subset of these arrows that results in P(s2 -> U) \leq P(s1 -> U) is allowed

                        //compute g(U)
                        boolean[] gU = new boolean[s];
                        System.arraycopy(upset, 0, gU, 0, s);

                        outer: for (int k = 0; k < s; k++)
                        {
                            if (!gU[k])
                                continue;

                            for (int l = 0; l < s; l++)
                            {
                                if (k == l || !gU[l])
                                    continue;
                                if (partialOrder[k][l])
                                {
                                    gU[k] = false;
                                    continue outer;
                                }
                            }
                        }

                        //compute U \cap (L(s2) \setminus g(U)), the collection of states we can try to remove
                        int tCount = 0;
                        boolean[] target = new boolean[s];
                        for (int k = 0; k < s; k++)
                            if (upset[k] && (target[k] = tm.arr[k][j] != null) && !gU[k])
                                tCount++;

                        //create indexed array of U \cap (L(s2) \setminus g(U))
                        int[] targetIndex = new int[tCount];
                        tCount = 0;
                        for (int k = 0; k < s; k++)
                            if (upset[k] && target[k] && !gU[k])
                                targetIndex[tCount++] = k;

                        //get list of arrays of state indices to remove (call it I)
                        ArrayList<int[]> remove = powerset(targetIndex);
                        ArrayList<PartialOrder> guesses = new ArrayList<>();

                        //convert that into a new partial ordering (anything from U to I is removed)
                        //check if it is a new partial ordering, if so, add it
                        outer: for (int[] option : remove)
                        {
                            PartialOrder guess = new PartialOrder();
                            guess.partialOrder = copy(partialOrder);

                            for (int k : option)
                                for (int l = 0; l < s; l++)
                                    if (upset[l] && k != l)
                                        guess.partialOrder[k][l] = false;

                            //check if it is a subset of something already added

                            inner: for (PartialOrder otherGuess : guesses)
                            {
                                for (int k = 0; k < s; k++)
                                    for (int l = 0; l < s; l++)
                                        if (!otherGuess.partialOrder[k][l] && guess.partialOrder[k][l])
                                            continue inner; //not a subset
                                continue outer; //definitely a subset
                            }

                            //check if the probabilites are correct
                            //check what the new upset is
                            boolean[] newUpset = new boolean[s];
                            for (int l = 0; l < s; l++)
                                if (gU[l])
                                    for (int m = 0; m < s; m++)
                                        if (guess.partialOrder[m][l])
                                            newUpset[m] = true;

                            int newKey2 = j << 7;
                            for (int l = 0; l < tm.neighbors[j].length; l++)
                                if (newUpset[tm.neighbors[j][l]])
                                    newKey2 += (1 << l);

                            //TODO: can optimize by saving this result, and seeing if any future guesses are subsets
                            if (!tm.cmp(key1, newKey2))
                                continue;

                            //check if superset of anything else; if so, replace other

                            inner: for (int k = guesses.size() - 1; k >= 0; k--)
                            {
                                PartialOrder otherGuess = guesses.get(k);
                                for (int l = 0; l < s; l++)
                                    for (int m = 0; m < s; m++)
                                        if (!guess.partialOrder[l][m] && otherGuess.partialOrder[l][m])
                                            continue inner; //not a subset
                                guesses.remove(k); //definitely a subset
                            }

                            guess.count = 0;
                            for (int k = 0; k < s; k++)
                                for (int l = 0; l < s; l++)
                                    if (guess.partialOrder[k][l] && states.get(k).geq(states.get(l)))
                                        guess.count++;
//                            for (boolean[] booleans : guess.partialOrder)
//                                for (boolean aBoolean : booleans)
//                                    if (aBoolean)
//                                        guess.count++;

                            guesses.add(guess);
                        }

                        PartialOrder last = new PartialOrder();
                        last.partialOrder = copy(partialOrder);
                        last.partialOrder[i][j] = false;
//                        last.count = 0;
//                        for (boolean[] booleans : last.partialOrder)
//                            for (boolean aBoolean : booleans)
//                                if (aBoolean)
//                                    last.count++;

                        last.count = 0;
                        for (int k = 0; k < s; k++)
                            for (int l = 0; l < s; l++)
                                if (last.partialOrder[k][l] && states.get(k).geq(states.get(l)))
                                    last.count++;

                        if (isPartialOrdering(last.partialOrder))
                            guesses.add(last);

                        System.out.println(c1 + " h");

                        outer: for (PartialOrder guess : guesses)
                        {
                            inner: for (PartialOrder order : pq)
                            {
                                for (int k = 0; k < s; k++)
                                    for (int l = 0; l < s; l++)
                                        if (order.partialOrder[k][l] != guess.partialOrder[k][l])
                                            continue inner;
                                continue outer;
                            }

                            pq.add(guess);
                        }

                        done = true;

                        return;

                        //blacklist[i][j] = true;
                        //checklist[i].remove(w);
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

        if (done)
            return;

        other[idx] = true;
        for (int i = 0; i < otherAllowed.length; i++)
            if (minUpset[idx][i] || temp[idx][i])
                otherAllowed[i] = false;

        generateAntichains(other, otherAllowed);
    }

    //DOES NOT INCLUDE EMPTY SET
    ArrayList<int[]> powerset(int[] in)
    {
        if (in.length > 63)
            return null;

        ArrayList<int[]> out = new ArrayList<>(1 << in.length);

        for (long i = 1; i < (1 << in.length); i++)
        {
            int count = 0;
            for (int j = 0; j < in.length; j++)
                if ((i & (1 << j)) != 0)
                    count++;

            int[] temp = new int[count];

            count = 0;
            for (int j = 0; j < in.length; j++)
                if ((i & (1 << j)) != 0)
                    temp[count++] = in[j];

            out.add(temp);
        }

        return out;
    }

    boolean isPartialOrdering(boolean[][] in)
    {
        for (int i = 0; i < s; i++)
            if (!in[0][i])
            {
                System.out.println(-1);
                return false;
            }

        for (int i = 0; i < in.length; i++)
        {
            if (!in[i][i])
            {
                System.out.println(-2);
                return false;
            }

            for (int j = 0; j < in.length; j++)
                for (int k = 0; k < in.length; k++)
                    if (in[i][j] && in[j][k] && !in[i][k])
                    {
                        System.out.println(-3);
                        return false;
                    }
        }

        return true;
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

        checklist = new ArrayList[states.size()];

        for (int i = 0; i < partialOrder.length; i++)
        {
            checklist[i] = new ArrayList<>(partialOrder.length);
            for (int j = 0; j < partialOrder.length; j++)
                if (i != j && partialOrder[i][j] && minRel[j][i])
                    checklist[i].add(j);
        }
    }

    boolean[][] copy(boolean[][] arr)
    {
        boolean[][] out = new boolean[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; i++)
            System.arraycopy(arr[i], 0, out[i], 0, arr[i].length);

        return out;
    }
}