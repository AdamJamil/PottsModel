package com.company;

import java.util.ArrayList;
import java.util.HashMap;

class PartialSolution
{
    static String s1, s2;
    static ArrayList<Arrow> allowedArrows1;
    static HashMap<Arrow, ArrayList<Arrow>> allowedArrows2;
    static HashMap<Arrow, RESum> prob1;
    static HashMap<Arrow, RESum> prob2;
    HashMap<Arrow, HashMap<Arrow, RESum>> conditionalProb = new HashMap<>();
    HashMap<Arrow, RESum> residualProb2 = new HashMap<>();

    PartialSolution solve(int index)
    {
        //print();

        if (index == allowedArrows1.size())
            return this;

        Arrow a1 = allowedArrows1.get(index);
        RESum currentProb = new RESum();

        for (Arrow arrow2 : allowedArrows2.get(a1))
            currentProb = currentProb.add(conditionalProb.get(a1).get(arrow2));

        //check if current a1 is done
        if (currentProb.equals(prob1.get(a1)))
            return solve(index + 1);

        //current a1 is not done, so we need to add some a2 with some probability
        //iterate over all a2
        //System.out.println(prob1.get(a1));
        for (Arrow a2 : allowedArrows2.get(a1))
        {
//            System.out.println(a2 + " hhh");
//            System.out.println(prob2.get(a2));
            if (!residualProb2.get(a2).equals(Main.sumZero))
            {
                //create new backtracking branch
                PartialSolution newBranch = new PartialSolution();
                for (Arrow arrow1 : conditionalProb.keySet())
                {
                    newBranch.conditionalProb.put(arrow1, new HashMap<>());
                    for (Arrow arrow2 : conditionalProb.get(arrow1).keySet())
                        newBranch.conditionalProb.get(arrow1).put(arrow2, conditionalProb.get(arrow1).get(arrow2).multiply(new Rational(1, 1)));
                }

                for (Arrow arrow2 : residualProb2.keySet())
                    newBranch.residualProb2.put(arrow2, residualProb2.get(arrow2).multiply(new Rational(1, 1)));

                //compare residual probability of a2 to prob1(a1) - currentProb
                RESum temp = prob1.get(a1).add(currentProb.multiply(new Rational(-1, 1)));

                switch (residualProb2.get(a2).compare(temp))
                {
                    case 0: //greater than
                    {
                        //replace residualProb2 with resid - temp
                        newBranch.residualProb2.put(a2, residualProb2.get(a2).add(temp.multiply(new Rational(-1, 1))));
                        //update conditional prob
                        newBranch.conditionalProb.get(a1).put(a2, temp);
                    }
                    break;
                    case 1: //less than
                    {
                        //set residualProb2 to 0
                        newBranch.residualProb2.put(a2, Main.sumZero.multiply(new Rational(1, 1)));
                        //update conditional prob
                        newBranch.conditionalProb.get(a1).put(a2, residualProb2.get(a2));
                    }
                    break;
                    case 2: //incomparable case
                    {
                        System.out.println("case 2");
                        continue;
                    }
                    case 3: //equal
                    {
                        //set residualProb2 to 0
                        newBranch.residualProb2.put(a2, Main.sumZero.multiply(new Rational(1, 1)));
                        //update conditional prob
                        newBranch.conditionalProb.get(a1).put(a2, residualProb2.get(a2));
                    }
                    break;
                }

                PartialSolution answer = newBranch.solve(index);
                if (answer != null)
                    return answer;
            }
        }

        return null;
    }

    void print()
    {
        System.out.println("s1 = " + s1 + ", s2 = " + s2);
        for (Arrow a1 : allowedArrows1)
        {
            System.out.println("if " + s1 + "" + a1 + "," + prob1.get(a1));
            for (Arrow a2 : allowedArrows2.get(a1))
                if (!conditionalProb.get(a1).get(a2).equals(Main.sumZero))
                    System.out.println("\t" + s2 + "" + a2 + " wp " + conditionalProb.get(a1).get(a2));
        }
    }
}
