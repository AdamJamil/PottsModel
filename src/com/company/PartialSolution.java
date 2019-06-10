package com.company;

import java.util.ArrayList;
import java.util.HashMap;

class PartialSolution
{
    ArrayList<Arrow> allowedArrows1 = new ArrayList<>();
    HashMap<Arrow, ArrayList<Arrow>> allowedArrows2 = new HashMap<>();
    HashMap<Arrow, RESum> prob1 = new HashMap<>();
    HashMap<Arrow, RESum> prob2 = new HashMap<>();
    HashMap<Arrow, HashMap<Arrow, RESum>> conditionalProb = new HashMap<>();

    PartialSolution solve(int index)
    {
        return null;
    }
}
