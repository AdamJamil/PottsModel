package com.company;

class Arrow
{
    private String name;
    private int[] diff;

    State map(State s)
    {
        return new State(new int[]{s.order[0] + diff[0], s.order[1] + diff[1], s.order[2] + diff[2]});
    }

    boolean valid(State s1)
    {
        State s2 = map(s1);
        if (s2.order[0] < 0 || s2.order[0] > Main.n)
            return false;
        if (s2.order[1] < 0 || s2.order[1] > Main.n)
            return false;
        if (s2.order[2] < 0 || s2.order[2] > Main.n)
            return false;

        return s2.order[1] >= s2.order[2];
    }

    Arrow(String a, int[] b)
    {
        name = a;
        diff = b;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
