package com.company;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.text.DecimalFormat;

public class Main extends Application
{
    static Polynomial zero, one, x;
    static RESum sumZero, sumOne;
    static boolean printTM = false;
    static int n = 8, k = 1;
    private static Driver d;

    static boolean drawUpsets = false, drawBadCases = false, drawPartialOrdering = true;

    public Main()
    {
        long time = System.nanoTime();
        d = new Driver();
        System.out.println("total run time: " + new DecimalFormat("#.000").format(((double) (System.nanoTime() - time)) / 1000000000) + "s");
    }

    //new thread that runs graphics, called by itself
    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Potts Model");
        Group root = new Group();
        Canvas canvas = new Canvas(GUtil.width, GUtil.height);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);

        new GUtil(d, gc, scene);
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}