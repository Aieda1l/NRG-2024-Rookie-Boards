package frc.robot;

import edu.wpi.first.wpilibj.util.Color;

public final class Constants {
    public static final class LED {
        public static final int PWMPORT = 1;
        public static final int BUFFERSIZE = 60;
        public static final Color[] COLORS = {Color.kRed, Color.kOrange, Color.kYellow, Color.kGreen, Color.kBlue, Color.kIndigo, Color.kViolet};
    }

    public static final class ObstacleSensor {
        public static final int DIOPORT = 1;
    }

    public static final class SparkMax {
        public static final int ID = 1;
        public static final double P = 0.0002;
        public static final double I = 0.0;
        public static final double D = 0.0;
        public static final double IZONE = 0.0;
        public static final double FF = 0.000175;
        public static final double MAXOUT = 1.0;
        public static final double MINOUT = 0.0;
    }
}