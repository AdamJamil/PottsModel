package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import static com.company.Driver.states;

class TransitionMatrix
{
    //map.get(s1).get(s2) -> gives P(s1 -> s2)
    HashMap<State, HashMap<State, RESum>> map;
    RESum[][] arr;
    int[][] neighbors;
    private HashMap<Integer, RESum> probability = new HashMap<>();
    private HashMap<Integer, Boolean> result = new HashMap<>();
    private ArrayList<Arrow> arrows = new ArrayList<>();

    double[][] evaluate(double lambda)
    {
        double[][] out = new double[states.size()][states.size()];

        for (int i = 0; i < out.length; i++)
            for (int j = 0; j < out.length; j++)
                out[i][j] += map.get(states.get(i)).get(states.get(j)).evaluate(lambda);

        return out;
    }

    boolean cmp(int key1, int key2)
    {
        int i = key1 >> 7, j = key2 >> 7;
        int key = (key1 << 16) + key2;

        if (result.containsKey(key))
            return result.get(key);

        RESum p1, p2;

        if (probability.containsKey(key1))
            p1 = probability.get(key1);
        else
        {
            p1 = new RESum();
            for (int k = 0; k < 7; k++)
                if ((key1 & (1 << k)) != 0)
                    p1.add(arr[i][neighbors[i][k]]);
            probability.put(key1, p1);
        }

        if (probability.containsKey(key2))
            p2 = probability.get(key2);
        else
        {
            p2 = new RESum();
            for (int k = 0; k < 7; k++)
                if ((key2 & (1 << k)) != 0)
                    p2.add(arr[j][neighbors[j][k]]);
            probability.put(key2, p2);
        }

        boolean temp;
        result.put(key, temp = p1.geq(p2));
        return temp;
    }

    TransitionMatrix()
    {
        arrows.add(new Arrow(" does (*→B)", new int[]{1, -1, 0}));
        arrows.add(new Arrow(" does (*→†)", new int[]{0, -1, 1}));
        arrows.add(new Arrow(" does (B→†)", new int[]{-1, 0, 1}));
        arrows.add(new Arrow(" does (B→*)", new int[]{-1, 1, 0}));
        arrows.add(new Arrow(" does (†→*)", new int[]{0, 1, -1}));
        arrows.add(new Arrow(" does (†→B)", new int[]{1, 0, -1}));
        arrows.add(new Arrow(" does (nothing)", new int[]{0, 0, 0}));

        states = State.generateStates();
        Collections.sort(states);

        map = new HashMap<>();

        Main.zero = new Polynomial();
        Main.zero.coefficients.add(new Rational(0, 1));
        Main.one = new Polynomial();
        Main.one.coefficients.add(new Rational(1, 1));
        Main.x = new Polynomial();
        Main.x.coefficients.add(new Rational(0, 1));
        Main.x.coefficients.add(new Rational(1, 1));
        Main.sumZero = new RESum();
        Main.sumZero.add(new RationalExpression(Main.zero));
        Main.sumOne = new RESum();
        Main.sumOne.terms.set(0, new RationalExpression(Main.one));

        for (State s1 : states)
        {
            map.put(s1, new HashMap<>());
            for (State s2 : states)
                map.get(s1).put(s2, new RESum());
        }

        for (int state1 = 0; state1 < states.size(); state1++)
        {
            int[] startOrder = states.get(state1).order;
            for (int source = 0; source < 3; source++)
            {
                if (startOrder[source] == 0)
                    continue;
                Rational pPickSource = new Rational(startOrder[source], Main.n);
                HashMap<RationalExpression, Integer> map2 = new HashMap<>();
                HashMap<RationalExpression, Integer> map3 = new HashMap<>();

                for (int dest = 0; dest < 3; dest++)
                {
                    int[] endOrder = new int[3];
                    System.arraycopy(startOrder, 0, endOrder, 0, 3);
                    endOrder[source]--;
                    endOrder[dest]++;
                    if (endOrder[1] < endOrder[2])
                    {
                        int temp = endOrder[1];
                        endOrder[1] = endOrder[2];
                        endOrder[2] = temp;
                    }
                    State endState = new State(endOrder);
                    int state2 = 0;
                    for (; state2 < states.size(); state2++)
                        if (states.get(state2).equals(endState))
                        {
                            Polynomial prob = Main.one.copy();
                            prob.multiply(pPickSource);
                            for (int i = 0; i < startOrder[dest] - ((source == dest) ? 1 : 0); i++)
                                prob.multiply(Main.x);
                            RationalExpression re = new RationalExpression(prob.copy());
                            map2.put(re, state1);
                            map3.put(re, state2);
                            break;
                        }
                }

                Polynomial denom = Main.zero.copy();
                for (RationalExpression rationalExpression : map2.keySet())
                    denom.add(rationalExpression.num);

                denom.multiply(new Rational(pPickSource.q, pPickSource.p));

                for (RationalExpression rE : map2.keySet())
                {
                    rE.divide(denom);
                    map.get(states.get(map2.get(rE))).get(states.get(map3.get(rE))).add(rE);
                }
            }
        }

        if (Main.printTM)
            print();

        //initialize probability arr
        arr = new RESum[states.size()][states.size()];

        for (int i = 0; i < states.size(); i++)
        {
            State s1 = states.get(i);

            for (Arrow arrow : arrows)
                if (arrow.valid(s1))
                    arr[i][states.indexOf(arrow.map(s1))] = map.get(states.get(i)).get(arrow.map(s1)).copy();
        }

        for (RESum[] reSums : arr)
            for (RESum reSum : reSums)
                if (reSum != null)
                    reSum.simplify();

        //initialize neighbors
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

    private void print()
    {
        StringBuilder sb = new StringBuilder();

        for (State state : states)
        {
            sb.append(" & ");
            sb.append(state.toString().replace("†", "\\dagger"));
        }

        for (State s1 : states)
        {
            sb.append(" \\\\ \n");
            sb.append(s1.toString().replace("†", "\\dagger"));
            sb.append(" & ");

            for (State s2 : states)
            {
                StringBuilder temp = new StringBuilder();
                for (RationalExpression rE : map.get(s1).get(s2).terms)
                {
                    if (rE.num.equals(Main.zero))
                        continue;
                    temp.append("\\frac{");
                    temp.append(rE.num.LaTeX());
                    temp.append("}{");
                    temp.append(rE.denom.LaTeX());
                    temp.append("} + ");
                }
                if (temp.toString().isEmpty())
                    temp.append("0000");
                String ee = temp.toString().substring(0, Math.max(0, temp.toString().length() - 3));
                sb.append(ee);
                sb.append(" & ");
            }
        }

        System.out.println(sb.toString());
    }
}