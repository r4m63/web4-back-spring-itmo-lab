package dev.ramil21.lab4back.util;

public class PointCheckerUtil {

    public static boolean checkHit(float x, float y, float r) {
        if (x <= 0 && y >= 0 && y <= 0.5 * x + r / 2) return true;
        if (x * x + y * y <= r * r && x >= 0 && y >= 0) return true;
        return x <= 0 && y <= 0 && x >= -r / 2 && y >= -r;
    }

}
