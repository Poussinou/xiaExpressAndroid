package fr.ac_versailles.dane.xiaexpress;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * xiaDetail.java
 * XiaExpress
 *
 * Created by guillaume on 24/10/2016.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * @author : guillaume.barre@ac-versailles.fr
 */

class xiaDetail {

    final Map<Integer, ImageView> points = new HashMap<>();
    String constraint = "";
    Boolean locked = false;
    String path = "";
    private Integer tag = 0;
    private float scale = 1;
    private float toolbarHeight;
    private float cornerWidth, cornerHeight;
    private DisplayMetrics metrics;
    private Context ctx;

    xiaDetail(Integer tag, float scale, float tbH, float cw, float ch, DisplayMetrics m, Context c) {
        this.tag = tag;
        points.clear();
        this.scale = scale;
        toolbarHeight = tbH;
        cornerWidth = cw;
        cornerHeight = ch;
        metrics = m;
        ctx = c;
    }

    Rect bezierFrame() {
        Rect rect = new Rect();
        float xMin = 99999999;
        float xMax = 0;
        float yMin = 99999999;
        float yMax = 0;
        // Get dimensions of the shape
        SortedSet<Integer> keys = new TreeSet<>(points.keySet());
        for (Integer key : keys) {
            ImageView point = points.get(key);
            float xPoint = point.getX();
            float yPoint = point.getY();
            if ( xPoint < xMin ) {
                xMin = xPoint;
            }
            if ( yPoint < yMin ) {
                yMin = yPoint;
            }
            if ( xPoint > xMax ) {
                xMax = xPoint;
            }
            if ( yPoint > yMax ) {
                yMax = yPoint;
            }
        }
        rect.set(Math.round((xMin) / scale)-1,
                Math.round((yMin) / scale)-1,
                Math.round((xMax) / scale)+1,
                Math.round((yMax) / scale)+1);

        return rect;
    }

    String createPath(float xMin, float ymin) {
        if (points.size() < 2) {
            return "0;0";
        }
        else {

            String path = "";
            SortedSet<Integer> keys = new TreeSet<>(points.keySet());
            for (Integer key : keys) {
                ImageView point = points.get(key);
                float x = (point.getX() - xMin) / scale;
                float y = (point.getY() - ymin) / scale;
                path = path + x + ";" + y + " ";
            }

            return path.trim(); // return X1.xxx;Y1.yyy X2.xxx;Y2.yyy X3.xxx;Y3.yyy ...
        }
    }

    ImageView createPoint(float x, float y, Integer index, Context ctx) {
        ImageView image = new ImageView(ctx);
        image.setImageResource(R.drawable.corner);
        image.setX(x);
        image.setY(y);
        image.setTag(this.tag);
        points.put(index, image);
        return image;
    }

    ImageView createShape(Boolean fill, int color, Boolean drawEllipse) {
        ImageView shapeView = new ImageView(ctx);
        ShapeDrawable shape = new ShapeDrawable();
        GradientDrawable drawable = new GradientDrawable();

        if (drawEllipse) {
            int width = Math.abs(Math.round(points.get(1).getX() - points.get(3).getX()));
            int height = Math.abs(Math.round(points.get(0).getY() - points.get(2).getY()));
            float x = Math.min(points.get(1).getX(), points.get(3).getX());
            float y = Math.min(points.get(0).getY(), points.get(2).getY());

            drawable.setShape(GradientDrawable.OVAL);
            drawable.setSize(width, height);
            shapeView.setX(x + cornerWidth / 2);
            shapeView.setY(y + cornerHeight / 2);

            shapeView.setBackground(drawable);

        }
        else {
            Path p = new Path();
            p.reset();

            ImageView endPoint = new ImageView(ctx);
            SortedSet<Integer> keys = new TreeSet<>(points.keySet());
            for (Integer key : keys) {
                ImageView point = points.get(key);

                float x = point.getX() + cornerWidth / 2;
                float y = point.getY() + cornerHeight / 2;

                if (key != 0) {
                    p.lineTo(x, y);
                } else {
                    p.moveTo(x, y);
                    endPoint = point;
                }
            }
            p.lineTo(endPoint.getX() + cornerWidth / 2, endPoint.getY() + cornerHeight / 2);
            shape = new ShapeDrawable(new PathShape(p, metrics.widthPixels, metrics.heightPixels - Math.round(toolbarHeight)));
            shape.setIntrinsicWidth(metrics.widthPixels);
            shape.setIntrinsicHeight(metrics.heightPixels);
            shape.getPaint().setColor(color);
            shapeView.setBackground(shape);
        }
        if (fill) {
            shape.getPaint().setStyle(Paint.Style.FILL);
            drawable.setColor(color);
            drawable.setAlpha(150);
            shape.getPaint().setAlpha(150); // 150 / 255 = 80%

        }
        else {
            shape.getPaint().setStyle(Paint.Style.STROKE);
            shape.getPaint().setPathEffect(new DashPathEffect(new float[] {10,5}, 0));
            shape.getPaint().setStrokeWidth(3);

            drawable.setColor(Color.TRANSPARENT);
            drawable.setStroke(3, color, 10, 5);

        }


        shapeView.setTag(this.tag + 100);

        return shapeView;
    }

    Map<Integer, ImageView> makeVirtPoints() {
        int nbPoints = points.size();
        Map<Integer, ImageView> virtPoints = new HashMap<>();

        for (int i = 0; i < nbPoints; i++) {
            int j = (i + 1) % nbPoints;
            // Get distance between point1 & point2
            float x = points.get(i).getX() - points.get(j).getX();
            float y = points.get(i).getY() - points.get(j).getY();
            double dist = Math.sqrt(x * x + y * y);
            if (dist > 50) {
                // We can show a virt point
                ImageView image = new ImageView(ctx);
                image.setImageResource(R.drawable.corner);
                image.setX((points.get(i).getX() + points.get(j).getX()) / 2);
                image.setY((points.get(i).getY() + points.get(j).getY()) / 2);
                image.setTag(tag + 100);
                image.setAlpha((float) 0.2);
                virtPoints.put(i, image);
            }
        }
        return virtPoints;
    }

}
