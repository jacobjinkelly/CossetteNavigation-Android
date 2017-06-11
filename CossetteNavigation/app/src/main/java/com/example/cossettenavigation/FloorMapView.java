package com.example.cossettenavigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

import com.example.cossettenavigation.beacons.ApplicationBeaconManager;
import com.example.cossettenavigation.map.Beacon;
import com.example.cossettenavigation.map.Floor;
import com.example.cossettenavigation.map.Map;
import com.example.cossettenavigation.map.Point2D;

/**
 * A view showing the beacons in {@link Map} and the estimated location on a grid.
 */
public class FloorMapView extends View {

    private static final String TAG = "FloorMapView";

    ApplicationBeaconManager beaconManager;

    private Paint rectanglePaint;
    private Paint anchorBeaconPaint;
    private Paint anchorBeaconLabelPaint;
    private Paint locationPaint;




    public FloorMapView(Context context, ApplicationBeaconManager beaconManager) {
        super(context);

        this.beaconManager = beaconManager;

        rectanglePaint = new Paint();
        rectanglePaint.setColor(Color.WHITE);
        rectanglePaint.setStyle(Paint.Style.STROKE);
        rectanglePaint.setStrokeWidth(25);

        anchorBeaconPaint = new Paint();
        anchorBeaconPaint.setStyle(Paint.Style.FILL);
        anchorBeaconPaint.setStrokeWidth(75);

        anchorBeaconLabelPaint = new Paint();
        anchorBeaconLabelPaint.setColor(Color.GRAY);
        anchorBeaconLabelPaint.setTypeface(Typeface.DEFAULT);
        anchorBeaconLabelPaint.setTextSize(36);
        anchorBeaconLabelPaint.setTextAlign(Paint.Align.CENTER);

        locationPaint = new Paint();
        locationPaint.setColor(Color.RED);
        locationPaint.setStyle(Paint.Style.FILL);
        locationPaint.setStrokeWidth(150);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.v(TAG, "onDraw()");

/*        Log.v(TAG, String.format(
                "onDraw(): width = %d, height = %d",
                getWidth(), getHeight()));*/

        super.onDraw(canvas);

        // Dimensions and sizes
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int rectangleMargin = 125;
        int rectangleWidth = canvasWidth - (2 * rectangleMargin);
        int rectangleHeight = canvasHeight - (2 * rectangleMargin);

        double mapWidth = Map.gridWidth;
        double mapHeight = Map.gridHeight;


        // Draw grid outline
        canvas.drawRect(
                rectangleMargin,
                rectangleMargin,
                canvasWidth - rectangleMargin,
                canvasHeight - rectangleMargin,
                rectanglePaint);


        // Draw beacons
        int floorsUp = 0;

        // Draw beacons, colour coded by floor (using grayscale)
        for (Floor floor : Map.floors) {
            int colorValue = 255 - (75 * floorsUp);
            if (colorValue < 100) {
                colorValue = 100;
            }
            anchorBeaconPaint.setColor(Color.argb(255, colorValue, colorValue, colorValue));

            for (Beacon beacon : floor.getAllBeacons()) {
                double x = rectangleMargin + (beacon.getXPosition() / mapWidth * rectangleWidth);
                double y = canvasHeight - (rectangleMargin + (beacon.getYPosition() / mapHeight * rectangleHeight));

/*            Log.v(TAG, String.format(
                    "onDraw(): x = %f, y = %f, canvasWidth = %d, canvasHeight = %d",
                    x, y, canvasWidth, canvasHeight));*/

                // Draw beacon
                //Log.v(TAG, String.format("onDraw(): x = %.0f, y = %.0f\n%s", x, y, beacon));
                canvas.drawPoint((float) x, (float) y, anchorBeaconPaint);

                // Draw name
                canvas.drawText(beacon.getName(), (float) x, (float) y - 60, anchorBeaconLabelPaint);
            }

            floorsUp++;
        }


        // Draw estimated location
        Point2D estimatedLocation = beaconManager.getEstimatedLocation();
        if (estimatedLocation != null) {
            double x = rectangleMargin + (estimatedLocation.x / mapWidth * rectangleWidth);
            double y = canvasHeight - (rectangleMargin + (estimatedLocation.y / mapHeight * rectangleHeight));

            canvas.drawCircle((float) x, (float) y, 50, locationPaint);
        }
    }

}
