package frc.robot;

import java.util.Random;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.util.Color;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkPIDController;
import com.revrobotics.CANSparkLowLevel.MotorType;

import frc.robot.Constants.*;

public class Robot extends TimedRobot {
  private AddressableLED m_led;
  private AddressableLEDBuffer m_ledBuffer;
  private DigitalInput m_sensor;
  private CANSparkMax m_motor;
  private RelativeEncoder m_encoder;
  private SparkPIDController m_PIDController;
  private Random m_rand;

  // Animation Variables
  private Color m_randomColor;
  private int m_rainbowFirstPixelHue;
  private int m_animationIndex;
  private boolean m_off = false;
  private boolean m_bounceForward = true;
  private boolean m_lightsaberExtend = true;
  private int m_policeLightTimer = 0;
  private boolean m_flashState = true;
  private int m_blinkTimer = 0;
  private boolean m_blinkState = true;

  @Override
  public void robotInit() {
    m_led = new AddressableLED(LED.PWMPORT);
    m_sensor = new DigitalInput(ObstacleSensor.DIOPORT);
    m_ledBuffer = new AddressableLEDBuffer(LED.BUFFERSIZE);
    m_motor = new CANSparkMax(SparkMax.ID, MotorType.kBrushless);
    m_rand = new Random();

    m_encoder = m_motor.getEncoder();
    m_PIDController = m_motor.getPIDController();

    m_PIDController.setP(SparkMax.P);
    m_PIDController.setI(SparkMax.I);
    m_PIDController.setD(SparkMax.D);
    m_PIDController.setIZone(SparkMax.IZONE);
    m_PIDController.setFF(SparkMax.FF);
    m_PIDController.setOutputRange(SparkMax.MINOUT, SparkMax.MAXOUT);

    m_motor.set(0.0);
    m_motor.burnFlash();
    
    m_led.setLength(m_ledBuffer.getLength());
    m_led.setData(m_ledBuffer);
    m_led.start();
  }

  @Override
  public void robotPeriodic() {
    if (m_sensor.get()) {
      // Pick a random animation and color when sensor is triggered
      if (m_off) {
        m_rainbowFirstPixelHue = 0;
        m_animationIndex = m_rand.nextInt(7); // Now 7 unique animations
        m_randomColor = getRandomColor(); // Generate random color
        m_off = false;
      }

      // Play the selected animation
      switch (m_animationIndex) {
        case 0:
          rainbow();
          break;
        case 1:
          blink(m_randomColor);
          break;
        case 2:
          pulse(m_randomColor);
          break;
        case 3:
          bounce(m_randomColor);
          break;
        case 4:
          sparkle(m_randomColor);
          break;
        case 5:
          lightsaber(m_randomColor);
          break;
        case 6:
          policeLights();
          break;
      }

      m_motor.set(0.5);

      // Random motor direction
      if (m_rand.nextBoolean()) {
        //m_motor.set(0.75);
      } else {
        //m_motor.set(-0.75);
      }
    } else {
      if (!m_off) {
        setAll(Color.kBlack);
        m_led.setData(m_ledBuffer);
        m_motor.set(0.0);
        m_off = true;
      }
    }
  }

  // Rainbow animation (predefined color pattern)
  private void rainbow() {
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / m_ledBuffer.getLength())) % 180;
      m_ledBuffer.setHSV(i, hue, 255, 128);
    }
    m_rainbowFirstPixelHue += 3;
    m_rainbowFirstPixelHue %= 180;
    m_led.setData(m_ledBuffer);
  }

  // Blink animation (color blinking on/off)
  private void blink(Color color) {
    int blinkDelay = 20; // Adjust this value for how long each blink lasts

    // Check if it's time to toggle the blink state (on/off)
    if (m_blinkTimer >= blinkDelay) {
        m_blinkState = !m_blinkState; // Toggle between on and off
        m_blinkTimer = 0; // Reset the timer after switching
    } else {
        m_blinkTimer++; // Increment the timer
    }

    // Set all LEDs to a random color if blinkState is true, otherwise set to black
    if (m_blinkState) {
        setAll(color);
    } else {
        setAll(Color.kBlack); // Turn off all LEDs
    }

    m_led.setData(m_ledBuffer);
  }

  // Pulse animation with color
  private void pulse(Color color) {
    double brightness = 0.5 + 0.5 * Math.sin(m_rainbowFirstPixelHue * 0.075); 

    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      int red = (int) (color.red * 255 * brightness);
      int green = (int) (color.green * 255 * brightness);
      int blue = (int) (color.blue * 255 * brightness);
      
      m_ledBuffer.setLED(i, new Color(red / 255.0, green / 255.0, blue / 255.0));
    }
    m_rainbowFirstPixelHue++;
    m_led.setData(m_ledBuffer);
  }

  // Bounce animation (3 LEDs move back and forth across the strip)
  private void bounce(Color color) {
      setAll(Color.kBlack);

      // Ensure that the index is within bounds (positive and less than the buffer length)
      if (m_rainbowFirstPixelHue < 0) {
          m_rainbowFirstPixelHue = 0; // Reset to the start if negative
          m_bounceForward = true;     // Change direction to forward
      }

      int firstOnIndex = m_rainbowFirstPixelHue % m_ledBuffer.getLength();

      for (int i = 0; i < 3; i++) {
          int ledIndex = (firstOnIndex + i) % m_ledBuffer.getLength();
          m_ledBuffer.setLED(ledIndex, color);
      }

      // Handle the bouncing direction
      if (m_bounceForward) {
          m_rainbowFirstPixelHue++;
          if (m_rainbowFirstPixelHue >= m_ledBuffer.getLength() - 3) {
              m_bounceForward = false;
          }
      } else {
          m_rainbowFirstPixelHue--;
          if (m_rainbowFirstPixelHue <= 0) {
              m_bounceForward = true;
          }
      }

      m_led.setData(m_ledBuffer);
  }

  // Sparkle animation (random LEDs flicker)
  private void sparkle(Color color) {
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      if (m_rand.nextDouble() < 0.2) {
        m_ledBuffer.setLED(i, color);
      } else {
        m_ledBuffer.setLED(i, Color.kBlack);
      }
    }
    m_led.setData(m_ledBuffer);
  }

  // Lightsaber animation (extend/retract effect with occasional flicker)
  private void lightsaber(Color color) {
    // Randomly determine if flickering should occur
    boolean flicker = m_rand.nextDouble() < 0.01; // 10% chance of flickering

    if (flicker && m_rainbowFirstPixelHue > m_ledBuffer.getLength()) {
        // Flicker effect: briefly set all LEDs to the random color, then back to black
        for (var i = 0; i < m_ledBuffer.getLength(); i++) {
            m_ledBuffer.setLED(i, color);
        }
        m_led.setData(m_ledBuffer);
        try {
            Thread.sleep(100); // Flicker duration (100 ms)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Reset to black after flicker
        for (var i = 0; i < m_ledBuffer.getLength(); i++) {
            m_ledBuffer.setLED(i, Color.kBlack);
        }
        m_led.setData(m_ledBuffer);
        try {
            Thread.sleep(100); // Delay before resuming normal animation (100 ms)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Proceed with lightsaber extension or retraction
    if (m_lightsaberExtend) {
        for (var i = 0; i < m_rainbowFirstPixelHue && i < m_ledBuffer.getLength(); i++) {
            m_ledBuffer.setLED(i, color);
        }
        m_rainbowFirstPixelHue++;
        if (m_rainbowFirstPixelHue > m_ledBuffer.getLength()) {
            //m_lightsaberExtend = false; // Start retracting after fully extended
        }
    } else {
        for (var i = 0; i < m_rainbowFirstPixelHue && i < m_ledBuffer.getLength(); i++) {
            m_ledBuffer.setLED(i, color);
        }
        m_rainbowFirstPixelHue--;
        if (m_rainbowFirstPixelHue <= 0) {
            //m_lightsaberExtend = true; // Start extending again
        }
    }
    m_led.setData(m_ledBuffer);
  }

  // Police lights animation (flashing red/blue with alternating halves and delay)
  private void policeLights() {
    int halfLength = m_ledBuffer.getLength() / 2;
    int flashDelay = 20; // Adjust this value to set how long each color stays

    // Only alternate after the timer reaches the set delay
    if (m_policeLightTimer >= flashDelay) {
        m_flashState = !m_flashState; // Toggle flash state (red/blue halves)
        m_policeLightTimer = 0; // Reset timer after switching
    } else {
        m_policeLightTimer++; // Increment the timer
    }

    Color color1 = m_flashState ? Color.kRed : Color.kBlue;
    Color color2 = m_flashState ? Color.kBlue : Color.kRed;

    // Set the first half to color1 (either red or blue)
    for (int i = 0; i < halfLength; i++) {
        m_ledBuffer.setLED(i, color1);
    }

    // Set the second half to color2 (either blue or red)
    for (int i = halfLength; i < m_ledBuffer.getLength(); i++) {
        m_ledBuffer.setLED(i, color2);
    }

    m_led.setData(m_ledBuffer);
  }

  // Helper function to generate a random color
  private Color getRandomColor() {
    return Constants.LED.COLORS[m_rand.nextInt(Constants.LED.COLORS.length)];
  }

  // Helper function to set all LEDs to a single color
  private void setAll(Color color) {
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      m_ledBuffer.setLED(i, color);
    }
  }
}
