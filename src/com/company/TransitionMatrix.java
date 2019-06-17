package com.company;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class TransitionMatrix
{
    HashMap<State, HashMap<State, RESum>> map;
    ArrayList<State> states;
    static ArrayList<Arrow> arrows = new ArrayList<>();

    void findCoupling()
    {
        for (State s1 : states)
            for (State s2 : states)
                if (s1.g(s2))
                {
                    //System.out.println("s1 = " + s1 + ", s2 = " + s2);
                    PartialSolution.s1 = s1.toString();
                    PartialSolution.s2 = s2.toString();

                    ArrayList<Arrow> availableArrows = new ArrayList<>();

                    PartialSolution ps = new PartialSolution();
                    PartialSolution.allowedArrows1 = new ArrayList<>();
                    PartialSolution.allowedArrows2 = new HashMap<>();
                    PartialSolution.prob1 = new HashMap<>();
                    PartialSolution.prob2 = new HashMap<>();

                    for (Arrow a : arrows)
                        if (a.valid(s2))
                        {
                            availableArrows.add(a);
                            PartialSolution.prob2.put(a, map.get(s2).get(a.map(s2)));
                            ps.residualProb2.put(a, map.get(s2).get(a.map(s2)));
                        }

                    for (Arrow a1 : arrows)
                    {
                        if (!a1.valid(s1) || map.get(s1).get(a1.map(s1)).terms.size() == 0)
                            continue;
                        //System.out.print(s1 + "" + a1 + ": ");

                        PartialSolution.prob1.put(a1, map.get(s1).get(a1.map(s1)));

                        ArrayList<Arrow> allowedArrows = new ArrayList<>();
                        for (Arrow a2 : availableArrows)
                            if (a1.map(s1).geq(a2.map(s2)))
                                allowedArrows.add(a2);

                        if (allowedArrows.size() < availableArrows.size())
                        {
                            //System.out.print(allowedArrows + "\n");
                            PartialSolution.allowedArrows1.add(a1);
                            PartialSolution.allowedArrows2.put(a1, allowedArrows);
                            ps.conditionalProb.put(a1, new HashMap<>());
                            for (Arrow allowedArrow : allowedArrows)
                                ps.conditionalProb.get(a1).put(allowedArrow, new RESum());
                        }
                        else;
                            //System.out.println("unrestricted");
                    }
                    System.out.println();

                    PartialSolution answer = ps.solve(0);
                    answer.print();
                }
    }

    BigDecimal[][] evaluatePrecise(BigDecimal lambda)
    {
        BigDecimal[][] out = new BigDecimal[states.size()][states.size()];

        for (int i = 0; i < out.length; i++)
            for (int j = 0; j < out.length; j++)
                out[i][j] = map.get(states.get(i)).get(states.get(j)).evaluatePrecise(lambda);

        return out;
    }

    double[][] evaluate(double lambda)
    {
        double[][] out = new double[states.size()][states.size()];

        for (int i = 0; i < out.length; i++)
            for (int j = 0; j < out.length; j++)
                out[i][j] += map.get(states.get(i)).get(states.get(j)).evaluate(lambda);

        return out;
    }

    TransitionMatrix()
    {
        states = State.generateStates();
        Collections.sort(states);

        map = new HashMap<>();

        Main.zero = new Polynomial();
        Main.zero.coefficients.add(new Rational(0, 1));
        Main.one = new Polynomial();
        Main.one.coefficients.add(new Rational(1, 1));
        Main.x = new Polynomial();
        Main.x.degree = 1;
        Main.x.coefficients.add(new Rational(0, 1));
        Main.x.coefficients.add(new Rational(1, 1));
        Main.sumZero = new RESum();
        Main.sumZero.add(new RationalExpression(Main.zero));
        Main.sumOne = new RESum();
        Main.sumZero.add(new RationalExpression(Main.one));

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
                            Polynomial prob = Main.one.multiply(pPickSource);
                            for (int i = 0; i < startOrder[dest] - ((source == dest) ? 1 : 0); i++)
                                prob = prob.multiply(Main.x);
                            RationalExpression re = new RationalExpression(prob.multiply(Main.one));
                            map2.put(re, state1);
                            map3.put(re, state2);
                            break;
                        }
                }

                Polynomial denom = Main.zero.multiply(Main.zero);
                for (RationalExpression rationalExpression : map2.keySet())
                    denom = denom.add(rationalExpression.num);

                denom = denom.multiply(new Rational(pPickSource.q, pPickSource.p));

                for (RationalExpression rE : map2.keySet())
                    map.get(states.get(map2.get(rE))).put(states.get(map3.get(rE)), map.get(states.get(map2.get(rE))).get(states.get(map3.get(rE))).add(rE.divide(denom.multiply(Main.one))));
            }
        }

        for (State s1 : map.keySet())
            for (State s2 : map.get(s1).keySet())
                for (RationalExpression rationalExpression : map.get(s1).get(s2).terms)
                    if (!rationalExpression.num.equals(Main.zero))
                        rationalExpression.setCoefficient();

        //printBS(states);

        if (Main.printTM)
        {
            String s = " ";
            for (State state : states)
                s += " & " + state.toString().replace("†", "\\dagger");

            for (State s1 : states)
            {
                s += " \\\\ \n" + s1.toString().replace("†", "\\dagger") + " & ";
                for (State s2 : states)
                {
                    String temp = "";
                    for (RationalExpression rE : map.get(s1).get(s2).terms)
                    {
                        if (rE.num.equals(Main.zero))
                            continue;
                        temp += "\\frac{";
                        if (rE.coeff.p != 1)
                            temp += rE.coeff.p + "(" + rE.num.LaTeX() + ")}{";
                        else
                            temp += rE.num.LaTeX() + "}{";

                        if (rE.coeff.q != 1)
                            temp += rE.coeff.q + "(" + rE.denom.LaTeX() + ")}";
                        else
                            temp += rE.denom.LaTeX() + "}";
                        //                    if (!rE.coeff.toString().equals(""))
                        //                        if (rE.coeff.q == 1)
                        //                            temp += rE.coeff.p + " \\cdot ";
                        //                        else
                        //                            temp += "\\frac{" + rE.coeff.p + "}{" + rE.coeff.q + "} \\cdot ";
                        //
                        //                    if (rE.denom.degree != 0)
                        //                        temp += "\\frac{" + rE.num.LaTeX() + "}{" + rE.denom.LaTeX() + "}";
                        temp += " + ";
                    }
                    if (temp.equals(""))
                        temp = "0000";
                    s += temp.substring(0, Math.max(0, temp.length() - 3)) + " & ";
                }
            }

            System.out.println(s);
        }
    }
}
