package frc.robot.subsystems;

import frc.robot.RobotMap;
import frc.robot.RobotMap.CAN_IDs;
import frc.robot.commands.intake.IntakeOuttakeCube;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import edu.wpi.first.wpilibj.command.Subsystem;
import harkerrobolib.wrappers.HSTalon;

/**
 * Represents an intake subsystem with pneumatic and motor control.
 * @author Finn Frankis
 * @version 6/14/18
 */
public class Intake extends Subsystem
{
    public static final double INTAKE_SPEED_SAFETY_MULTIPLIER = 0.7;

    /**
     * The current instance of this subsystem.
     */
    public static Intake intake = null;
    
    /**
     * The instance of the pneumatics subsystem.
     */
    public static Pneumatics pn = Pneumatics.getInstance();
    
    /**
     * The left Talon on the intake.
     */
    private HSTalon leftTalon;
    
    /**
     * The right Talon on the intake.
     */
    private HSTalon rightTalon;
    
    /**
     * Represents the possible directions for the intake.
     * @author Finn Frankis
     * @version Jul 25, 2018
     */
    public enum IntakeType
    {
        /**
         * Used to signify the intaking of a cube.
         */
        INTAKE, 
        
        /**
         * Used to signify the outtaking of a cube.
         */
        OUTTAKE,
        
        /**
         * Used to signify no motion whatsoever.
         */
        NONE;
    }
    /**
     * Constructs a new Intake.
     */
    public Intake()
    {
        leftTalon = new HSTalon(CAN_IDs.INTAKE_TALON_LEFT, RobotMap.TIMEOUT);
        rightTalon = new HSTalon(CAN_IDs.INTAKE_TALON_RIGHT, RobotMap.TIMEOUT);
    }

    protected void invertTalons () {
        rightTalon.setInverted(Intake.RIGHT_TALON_INVERTED);
    }
    /**
     * Initializes the command using the ports for the left and right Talons.
     */
    protected void initDefaultCommand()
    {
        setDefaultCommand(new IntakeOuttakeCube());
    }

    /**
     * Intakes/outakes a cube given a left and right speed (if two joysticks)
     * @param speedLeft the percent output for the left intake
     * @param speedRight the percent output for the right intake
     */
    public void intakeOuttakeCube(double speedLeft, double speedRight)
    {
        leftTalon.set(ControlMode.PercentOutput, speedLeft);
        rightTalon.set(ControlMode.PercentOutput, speedRight);
    }
    
    /**
     * Intakes/outakes a cube given a single speed for both halves.
     * @param speed the speed at which both halves should be run
     */
    public void intakeOuttakeCube (double speed)
    {
        if(RobotMap.SAFETY_MODE == RobotMap.SafetyMode.SAFE)
            speed *= INTAKE_SPEED_SAFETY_MULTIPLIER;
        leftTalon.set(ControlMode.PercentOutput, speed);
        rightTalon.set(ControlMode.PercentOutput, speed);
    }
    
    /**
     * Initializes the intake talons.
     */
    public void talonInit()
    {
        invertTalons();
        intakeSetNeutralMode(NeutralMode.Brake);
        intakeSetCurrentLimit(Intake.PEAK_CURRENT_LIMIT, Intake.PEAK_TIME_MS,
                Intake.CONTINUOUS_CURRENT_LIMIT);
    }

    /**
     * Sets the neutral mode for the Talons
     * 
     * @param nm the neutral mode
     */
    private void intakeSetNeutralMode(NeutralMode nm)
    {
        getLeftTalon().setNeutralMode(nm);
        getRightTalon().setNeutralMode(nm);
    }

    /**
     * Sets the current limit on the 
     * 
     * @param peakCurrentLimit
     *            the peak limit (only temporary)
     * @param peakTime
     *            the time (in ms) for the peak limit to be allowed
     * @param continuousLimit
     *            the continuous current limit (after peak has expired)
     */
    private void intakeSetCurrentLimit(int peakCurrentLimit, int peakTime, int continuousLimit)
    {
        getLeftTalon().configPeakCurrentLimit(peakCurrentLimit);
        getRightTalon().configPeakCurrentLimit(peakCurrentLimit);

        getLeftTalon().configPeakCurrentDuration(peakTime);
        getRightTalon().configPeakCurrentLimit(peakCurrentLimit);

        getLeftTalon().configContinuousCurrentLimit(continuousLimit);
        getRightTalon().configPeakCurrentLimit(peakCurrentLimit);

        getLeftTalon().enableCurrentLimit(true);
        getRightTalon().configPeakCurrentLimit(peakCurrentLimit);
    }
    
    /**
     * Gets the left Talon on the 
     * @return the left Talon
     */
    public HSTalon getLeftTalon() { return leftTalon; }

    /**
     * Gets the right Talon on the 
     * @return the right Talon
     */
    public HSTalon getRightTalon() { return rightTalon; }

    /**
     * Gets the instance of this singleton Intake, returning a new one if one has not yet been created.
     * @return this Intake instance
     */
    public static Intake getInstance()
    {
        if (intake == null) intake = new Intake();
        return intake;
    }
    
    /**
     * Sets the left intake talon to run at a given % output.
     * @param speed the % output at which the talon should run, positive for intaking
     */
    public void setLeft (double speed)
    {
        getLeftTalon().set(ControlMode.PercentOutput, speed);
    }
    
    /**
     * Sets the right intake talon to run at a given % output.
     * @param speed the % output at which the talon should run, positive for intaking
     */
    public void setRight (double speed)
    {
        getRightTalon().set(ControlMode.PercentOutput, speed);
    }

    /**
     * The direction of Talon input such that the intake will intake.
     */
    public static final double INTAKE_DIR = 1;
    
    /**
     * The peak current limit for the intake.
     */
    public static int PEAK_CURRENT_LIMIT = 15;
    
    /**
     * The time (in ms) for which the peak current limit remains in use.
     */
    public static int PEAK_TIME_MS = 2000;
    
    /**
     * The continuous current limit for the intake.
     */
    public static int CONTINUOUS_CURRENT_LIMIT = 15;
    
    /**
     * The constant to signify that the intake will be controlled without manual control.
     */
    public static boolean NO_MANUAL_INTAKE = false;

    public static boolean RIGHT_TALON_INVERTED = true;
}



