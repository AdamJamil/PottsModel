package com.company;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.HashSet;

import static com.company.Main.drawBadCases;
import static com.company.Main.n;
import static com.company.Main.tonyMode;

class GUtil
{
    //it is the current year
    //hehe xd
    private static int xShift = 0, yShift = 0;
    private static int xOff = 40, yOff = 90;
    private static double dist = 50;
    static double width = 800, height = yOff + dist * (n + 2);
    private static int caseIndex = 0, upsetIndex = 0, partialOrderIndex = 0;
    private static double dL = 0.01;
    Driver d;
    TransitionMatrix tm;

    GUtil(Driver d, GraphicsContext gc, Scene scene)
    {
        this.d = d;
        tm = d.tm;
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(0.1), event ->
        {
            gc.clearRect(0, 0, width, height);

            drawDiagram(gc);

            if (Main.drawBadCases)
            {
                drawCase(gc);
                drawProbs(gc);
                drawGraph(gc);
            }
            else if (Main.drawUpsets)
                drawUpset(gc);
            else if (Main.drawPartialOrdering)
                drawPartialOrdering(gc);

        }));

        scene.setOnKeyPressed(e ->
        {
            if (e.getCode().equals(KeyCode.SPACE))
            {
                if (Main.drawBadCases)
                {
                    caseIndex++;
                    caseIndex %= d.bs1.size();
                    System.out.println(d.p1.get(caseIndex).LaTeX());
                    System.out.println(d.p2.get(caseIndex).LaTeX() + "\n");
                    //System.out.println((tm.p1.get(caseIndex).add(tm.p2.get(caseIndex).multiply(new Rational(-1, 1)))).LaTeX());
                }
                else if (Main.drawUpsets)
                {
                    upsetIndex++;
                    upsetIndex %= d.upsets.size();
                }
                else if (Main.drawPartialOrdering)
                {
                    partialOrderIndex++;
                    partialOrderIndex %= d.partialOrders.size();
                }
            }
            else if (e.getCode().equals(KeyCode.Z))
            {
                gc.setTransform(1.1, 0, 0, 1.1, 0, 0);
            }
        });

        timeline.play();
    }

    void drawPartialOrdering(GraphicsContext gc)
    {
        boolean[][] partialOrder = d.partialOrders.get(partialOrderIndex);

        gc.setStroke(Color.BLACK);
        for (int i = 1; i < n + 1; i++)
        {
            int rowStates = (i + 1) / 2;

            for (int j = 0; j < rowStates - 1; j++)
            {
                State s1 = new State(new int[]{n - i + 1, n - (n - i + 1 + j), j});
                int idx1 = Driver.states.indexOf(s1);
                State down = Driver.arrows.get(3).map(s1);
                State right = Driver.arrows.get(1).map(s1);
                State downRight = Driver.arrows.get(2).map(s1);
                int idxDown = Driver.states.indexOf(down);
                int idxRight = Driver.states.indexOf(right);
                int idxDownRight = Driver.states.indexOf(downRight);

                if (partialOrder[idx1][idxDown])
                    drawArrow(gc, xOff + dist * j + xShift, yOff + dist * (i - 1) + yShift,
                            xOff + dist * j + xShift, yOff + dist * i + yShift);

                if (partialOrder[idxRight][idx1])
                    drawArrow(gc,xOff + dist * (j + 1) + xShift, yOff + dist * (i - 1) + yShift,
                            xOff + dist * j + xShift, yOff + dist * (i - 1) + yShift);

                if (partialOrder[idx1][idxDownRight])
                    drawArrow(gc, xOff + dist * j + xShift, yOff + dist * (i - 1) + yShift,
                            xOff + dist * (j + 1) + xShift, yOff + dist * i + yShift);
            }

            State rowEnd = new State(new int[]{n - i + 1, n - (n - i + 1 + (rowStates - 1)), (rowStates - 1)});
            State down = Driver.arrows.get(3).map(rowEnd);
            int idx1 = Driver.states.indexOf(rowEnd);
            int idxDown = Driver.states.indexOf(down);

            if (partialOrder[idx1][idxDown])
                drawArrow(gc, xOff + dist * (rowStates - 1) + xShift, yOff + dist * (i - 1) + yShift,
                        xOff + dist * (rowStates - 1) + xShift, yOff + dist * i + yShift);

            if (i % 2 == 0)
            {
                State downRight = Driver.arrows.get(2).map(rowEnd);
                int idxDownRight = Driver.states.indexOf(downRight);
                if (partialOrder[idx1][idxDownRight])
                    drawArrow(gc, xOff + dist * (rowStates - 1) + xShift, yOff + dist * (i - 1) + yShift,
                            xOff + dist * ((rowStates - 1) + 1) + xShift, yOff + dist * i + yShift);
            }
        }

        int rowStates = (n + 2) / 2;
        for (int j = 0; j < rowStates - 1; j++)
        {
            State s1 = new State(new int[]{0, n - j, j});
            State right = Driver.arrows.get(1).map(s1);
            int idx1 = Driver.states.indexOf(s1);
            int idxRight = Driver.states.indexOf(right);
            if (partialOrder[idxRight][idx1])
                drawArrow(gc,xOff + dist * (j + 1) + xShift, yOff + dist * n + yShift,
                        xOff + dist * j + xShift, yOff + dist * n + yShift);
        }
    }

    void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2)
    {
        x1 += 5;
        x2 += 5;
        y1 += 5;
        y2 += 5;

        gc.strokeLine(x1, y1, x2, y2);

        double cx = (x1 + x2) / 2, cy = (y1 + y2) / 2;

        double Dx = x2 - x1, Dy = y2 - y1;
        double dx = 4 * Dx / Math.sqrt(Dx * Dx + Dy * Dy);
        double dy = 4 * Dy / Math.sqrt(Dx * Dx + Dy * Dy);

        double cos = 0.866, sin = 0.5;

        gc.strokeLine(cx + dx * cos - dy * sin, cy + dx * sin + dy * cos, cx, cy);
        gc.strokeLine(cx + dx * cos + dy * sin, cy - dx * sin + dy * cos, cx, cy);
    }

    void drawDiagram(GraphicsContext gc)
    {
        gc.setStroke(Color.BLACK);

        for (int i = 1; i <= n + 1; i++)
        {
            int rowStates = (i + 1) / 2;

            for (int j = 0; j < rowStates; j++)
                if (tonyMode)
                    gc.strokeOval(xOff + dist * j + xShift, yOff + dist * (i - 1) + yShift - (j * dist / 2), 10, 10);
                else
                    gc.strokeOval(xOff + dist * j + xShift, yOff + dist * (i - 1) + yShift, 10, 10);
        }
    }

    void drawCase(GraphicsContext gc)
    {
        gc.strokeText("s₁ = " + d.bs1.get(caseIndex) + " s₂ = " + d.bs2.get(caseIndex), 40, 40);
        gc.strokeText("U generated by: " + d.generators.get(d.bu.get(caseIndex)), 40, 70);

        for (State uState : d.bu.get(caseIndex))
        {
            int stateI = n - uState.order[0] + 1;
            int stateJ = uState.order[2];

            gc.setFill(Color.RED);
            if (tonyMode)
                gc.fillOval(xOff + dist * stateJ + xShift, yOff + dist * (stateI - 1) + yShift - (stateJ * dist / 2), 10, 10);
            else
                gc.fillOval(xOff + dist * stateJ + xShift, yOff + dist * (stateI - 1) + yShift, 10, 10);
        }

        int s1I = n - d.bs1.get(caseIndex).order[0] + 1;
        int s2I = n - d.bs2.get(caseIndex).order[0] + 1;
        int s1J = d.bs1.get(caseIndex).order[2];
        int s2J = d.bs2.get(caseIndex).order[2];

        double c1x = 5 + xOff + dist * s1J + xShift;
        double c2x = 5 + xOff + dist * s2J + xShift;
        double c1y = 5 + yOff + dist * (s1I - 1) + yShift;
        double c2y = 5 + yOff + dist * (s2I - 1) + yShift;

        if (tonyMode)
        {
            c1y -= s1J * dist / 2;
            c2y -= s2J * dist / 2;
        }

        gc.setStroke(Color.BLUE);
        gc.strokeLine(c1x - 5, c1y - 5, c1x + 5, c1y + 5);
        gc.strokeLine(c1x - 5, c1y + 5, c1x + 5, c1y - 5);
        gc.strokeLine(c2x - 5, c2y - 5, c2x + 5, c2y + 5);
        gc.strokeLine(c2x - 5, c2y + 5, c2x + 5, c2y - 5);

        gc.setStroke(Color.BLACK);
        gc.strokeText("s₁", c1x - 6, c1y + 20);
        gc.strokeText("s₂", c2x - 6, c2y + 20);
    }

    void drawUpset(GraphicsContext gc)
    {
        for (int i = 0; i < d.upsets.get(0).length; i++)
        {
            State uState = Driver.states.get(0);
            int stateI = n - uState.order[0] + 1;
            int stateJ = uState.order[2];

            gc.setFill(Color.RED);
            if (tonyMode)
                gc.fillOval(xOff + dist * stateJ + xShift, yOff + dist * (stateI - 1) + yShift - (stateJ * dist / 2), 10, 10);
            else
                gc.fillOval(xOff + dist * stateJ + xShift, yOff + dist * (stateI - 1) + yShift, 10, 10);
        }
    }

    void drawProbs(GraphicsContext gc)
    {
        gc.strokeText("P(s₁ → U) = ", xOff + 100, yOff + 8);
        gc.setStroke(Color.RED);
        gc.strokeText(d.p1.get(caseIndex).toString(), new Text("P(s₁ → U) = ").getLayoutBounds().getWidth() + xOff + 100, yOff + 8);
        gc.setStroke(Color.BLACK);
        gc.strokeText("P(s₂ → U) = ", xOff + 100, yOff + dist + 8);
        gc.setStroke(Color.BLUE);
        gc.strokeText(d.p2.get(caseIndex).toString(), new Text("P(s₂ → U) = ").getLayoutBounds().getWidth() + xOff + 100, yOff + dist + 8);
    }

    void drawGraph(GraphicsContext gc)
    {
        gc.setStroke(Color.BLACK);
        gc.strokeLine(300, 200, 300, 400);
        gc.strokeLine(225, 300, 487.5, 300);
        gc.strokeLine(337.5, 305, 337.5, 295);

        //lambda = 0 -> x = 250
        //lambda = 4 -> x = 400

        double lastY = d.p1.get(caseIndex).evaluate(-2);

        gc.setStroke(Color.RED);

        for (double lambda = -2 + dL; lambda <= 5; lambda += dL)
        {
            double temp = d.p1.get(caseIndex).evaluate(lambda);
            if (Math.abs(temp - lastY) > 3 || convertY(temp) < 200 || convertY(temp) > 400)
            {
                lastY = temp;
                continue;
            }
            gc.strokeLine(convertX(lambda - dL), convertY(lastY), convertX(lambda), convertY(temp));
            lastY = temp;
        }

        lastY = d.p2.get(caseIndex).evaluate(-2);

        gc.setStroke(Color.BLUE);

        for (double lambda = -2 + dL; lambda <= 5; lambda += dL)
        {
            double temp = d.p2.get(caseIndex).evaluate(lambda);
            if (Math.abs(temp - lastY) > 3 || convertY(temp) < 200 || convertY(temp) > 400)
            {
                lastY = temp;
                continue;
            }
            gc.strokeLine(convertX(lambda - dL), convertY(lastY), convertX(lambda), convertY(temp));
            lastY = temp;
        }
    }

    double convertX(double in)
    {
        return 37.5 * in + 300;
    }

    double convertY(double in)
    {
        //10px is 1
        return -50 * in + 300;
    }
}
