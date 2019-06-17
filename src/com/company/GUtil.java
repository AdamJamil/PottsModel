package com.company;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static com.company.Main.n;
import static com.company.Main.tonyMode;

class GUtil
{
    private static int xShift = 0, yShift = 0;
    private static int xOff = 40, yOff = 40;
    private static double dist = 50;
    static double width = 500, height = xOff + dist * (n + 2);
    private static int caseIndex = 0, upsetIndex = 0;
    TransitionMatrix tm;

    GUtil(TransitionMatrix tm, GraphicsContext gc, Scene scene)
    {
        this.tm = tm;
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(0.1), event ->
        {
            gc.clearRect(0, 0, width, height);

            drawDiagram(gc);
            if (tm.bu.size() > 0)
                drawCase(gc);
            else
                drawUpset(gc);
        }));

        scene.setOnKeyPressed(e ->
        {
            if (e.getCode().equals(KeyCode.SPACE))
            {
                if (tm.bs1.size() > 0)
                {
                    caseIndex++;
                    caseIndex %= tm.bs1.size();
                }
                else
                {
                    upsetIndex++;
                    upsetIndex %= tm.upsets.size();
                }
            }
        });

        timeline.play();
    }

    void drawDiagram(GraphicsContext gc)
    {
        for (int i = 1; i <= n + 1; i++)
        {
            int rowStates = (i + 1) / 2;

            gc.setStroke(Color.BLACK);
            for (int j = 0; j < rowStates; j++)
                if (tonyMode)
                    gc.strokeOval(xOff + dist * j + xShift, yOff + dist * (i - 1) + yShift - (j * dist / 2), 10, 10);
                else
                    gc.strokeOval(xOff + dist * j + xShift, yOff + dist * (i - 1) + yShift, 10, 10);
        }
    }

    void drawCase(GraphicsContext gc)
    {
        for (State uState : tm.bu.get(caseIndex))
        {
            int stateI = n - uState.order[0] + 1;
            int stateJ = uState.order[2];

            gc.setFill(Color.RED);
            if (tonyMode)
                gc.fillOval(xOff + dist * stateJ + xShift, yOff + dist * (stateI - 1) + yShift - (stateJ * dist / 2), 10, 10);
            else
                gc.fillOval(xOff + dist * stateJ + xShift, yOff + dist * (stateI - 1) + yShift, 10, 10);
        }

        int s1I = n - tm.bs1.get(caseIndex).order[0] + 1;
        int s2I = n - tm.bs2.get(caseIndex).order[0] + 1;
        int s1J = tm.bs1.get(caseIndex).order[2];
        int s2J = tm.bs2.get(caseIndex).order[2];
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
        gc.strokeText("s1", c1x - 6, c1y + 20);
        gc.strokeText("s2", c2x - 6, c2y + 20);
    }

    void drawUpset(GraphicsContext gc)
    {
        for (State uState : tm.upsets.get(upsetIndex))
        {
            int stateI = n - uState.order[0] + 1;
            int stateJ = uState.order[2];

            gc.setFill(Color.RED);
            gc.fillOval(xOff + dist * stateJ + xShift, yOff + dist * (stateI - 1) + yShift, 10, 10);
        }
    }
}