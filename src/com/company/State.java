package com.company;

import java.util.ArrayList;
import java.util.HashSet;

class State implements Comparable<State>
{
    String name = "";
    int[] order = new int[3];

    boolean g(State other)
    {
        return geq(other) && !equals(other);
    }

    boolean geq(State other)
    {
        if (this.equals(other))
            return true;

        if (this.order[0] < other.order[0])
            return false;

        return (this.order[0] + this.order[2] >= other.order[0] + other.order[2]);

//        if (this.equals(other))
//            return true;
//
//        if (this.order[0] < other.order[0] || this.order[1] > other.order[1] || this.order[2] > other.order[2])
//            return false;
//
//        return (this.order[0] - other.order[0]) >= (other.order[2] - this.order[2] + other.order[1] - this.order[1]);
    }

    @Override
    public int compareTo(State o)
    {
        if (order[0] != o.order[0])
            return o.order[0] - order[0];
        if (order[2] != o.order[2])
            return o.order[2] - order[2];
        return o.order[1] - order[1];
    }

    State(int[] arr)
    {
        System.arraycopy(arr, 0, order, 0, arr.length);

        for (int i = 0; i < order[0]; i++)
            name += "B";

        for (int i = 0; i < order[1]; i++)
            name += "*";

        for (int i = 0; i < order[2]; i++)
            name += "†";
    }

    @Override
    public int hashCode()
    {
        return order[0] + order[1] + order[2];
    }

    @Override
    public boolean equals(Object obj)
    {
        return name.equals(((State) obj).name);
    }

    @Override
    public String toString()
    {
        return name;
    }

    static ArrayList<State> generateStates()
    {
        ArrayList<int[]> partitions = Main.partitions();
        HashSet<State> temp = new HashSet<>();

        for (int[] partition : partitions)
        {
            int a = partition[0], b = partition[1], c = partition[2];
            temp.add(new State(new int[]{a, b, c}));
            temp.add(new State(new int[]{b, a, c}));
            temp.add(new State(new int[]{c, a, b}));
        }

        return new ArrayList<>(temp);
    }
}
