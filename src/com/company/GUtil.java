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
import static com.company.Main.n;

class GUtil
{
    //it is the current year
    //hehe xd
    private static int xShift = 0, yShift = 0;
    private static int xOff = 40, yOff = 90;
    private static double dist = 50;
    static double width = 800, height = yOff + dist * (n + 2);
    private static int caseIndex = 0, upsetIndex = 0, partialOrderIndex = 0;
    private Driver d;
    private TransitionMatrix tm;

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

            gc.beginPath();
            gc.moveTo(40, 40);
            gc.arcTo(40, 40, 100, 100, 20);
            gc.closePath();
            gc.setFill(Color.BLACK);
            gc.setStroke(Color.BLACK);
            gc.fill();
            gc.stroke();

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
            else if (e.getCode().equals(KeyCode.E))
            {
                if (Main.drawPartialOrdering)
                {
                    partialOrderIndex = d.partialOrders.size() - 1;
                }
            }
        });

        timeline.play();
    }

    private void drawPartialOrdering(GraphicsContext gc)
    {
        boolean[][] partialOrder = d.partialOrders.get(partialOrderIndex), minUpset = new boolean[partialOrder.length][partialOrder.length];

        for (int i = 0; i < partialOrder.length; i++)
            for (int j = 0; j < partialOrder.length; j++)
                minUpset[j][i] = partialOrder[i][j];

        gc.setStroke(Color.BLACK);

        partialOrder = d.copy(partialOrder);
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

            for (int j = 0; j < set.length; j++)
                if (set[j])
                {
                    if (tm.arr[i][j] != null)
                    {
                        gc.setStroke(Color.BLACK);
                        drawArrow(gc, xPos(Driver.states.get(j)), yPos(Driver.states.get(j)),
                                xPos(Driver.states.get(i)), yPos(Driver.states.get(i)));
                    }
                    else
                    {
                        gc.setStroke(Color.BLUE);
                        drawArrow(gc, xPos(Driver.states.get(j)), yPos(Driver.states.get(j)),
                                13 + (xPos(Driver.states.get(i)) + xPos(Driver.states.get(j))) / 2,
                                -13 + (yPos(Driver.states.get(i)) + yPos(Driver.states.get(j))) / 2);

                        drawArrow(gc, 13 + (xPos(Driver.states.get(i)) + xPos(Driver.states.get(j))) / 2,
                                -13 + (yPos(Driver.states.get(i)) + yPos(Driver.states.get(j))) / 2,
                                xPos(Driver.states.get(i)), yPos(Driver.states.get(i)));
                    }
                }
        }
    }

    private double yPos(State s)
    {
        return yOff + dist * (n - s.order[0]) + yShift;
    }

    private double xPos(State s)
    {
        return xOff + dist * s.order[2] + xShift;
    }

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2)
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

    private void drawDiagram(GraphicsContext gc)
    {
        gc.setStroke(Color.BLACK);

        for (int i = 1; i <= n + 1; i++)
        {
            int rowStates = (i + 1) / 2;

            for (int j = 0; j < rowStates; j++)
                gc.strokeOval(xOff + dist * j + xShift, yOff + dist * (i - 1) + yShift, 10, 10);
        }
    }

    private void drawCase(GraphicsContext gc)
    {
        gc.strokeText("s₁ = " + d.bs1.get(caseIndex) + " s₂ = " + d.bs2.get(caseIndex), 40, 40);

        for (int i = 0; i < d.bu.get(caseIndex).length; i++)
        {
            if (!d.bu.get(caseIndex)[i])
                continue;
            State uState = Driver.states.get(i);
            int stateI = n - uState.order[0] + 1;
            int stateJ = uState.order[2];

            gc.setFill(Color.RED);
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

        gc.setStroke(Color.BLUE);
        gc.strokeLine(c1x - 5, c1y - 5, c1x + 5, c1y + 5);
        gc.strokeLine(c1x - 5, c1y + 5, c1x + 5, c1y - 5);
        gc.strokeLine(c2x - 5, c2y - 5, c2x + 5, c2y + 5);
        gc.strokeLine(c2x - 5, c2y + 5, c2x + 5, c2y - 5);

        gc.setStroke(Color.BLACK);
        gc.strokeText("s₁", c1x - 6, c1y + 20);
        gc.strokeText("s₂", c2x - 6, c2y + 20);
    }

    private void drawUpset(GraphicsContext gc)
    {
        for (int i = 0; i < d.upsets.get(upsetIndex).length; i++)
        {
            if (d.upsets.get(upsetIndex)[i])
            {
                State uState = Driver.states.get(i);
                gc.setFill(Color.RED);
                gc.fillOval(xPos(uState), yPos(uState), 10, 10);
            }
        }
    }

    private void drawProbs(GraphicsContext gc)
    {
        gc.strokeText("P(s₁ → U) = ", xOff + 100, yOff + 8);
        gc.setStroke(Color.RED);
        gc.strokeText(d.p1.get(caseIndex).toString(), new Text("P(s₁ → U) = ").getLayoutBounds().getWidth() + xOff + 100, yOff + 8);
        gc.setStroke(Color.BLACK);
        gc.strokeText("P(s₂ → U) = ", xOff + 100, yOff + dist + 8);
        gc.setStroke(Color.BLUE);
        gc.strokeText(d.p2.get(caseIndex).toString(), new Text("P(s₂ → U) = ").getLayoutBounds().getWidth() + xOff + 100, yOff + dist + 8);
    }

    private void drawGraph(GraphicsContext gc)
    {
        double dL = 0.01;

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

    private double convertX(double in)
    {
        return 37.5 * in + 300;
    }

    private double convertY(double in)
    {
        //10px is 1
        return -50 * in + 300;
    }
}