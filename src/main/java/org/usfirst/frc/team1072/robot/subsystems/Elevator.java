package org.usfirst.frc.team1072.robot.subsystems;

import org.usfirst.frc.team1072.robot.RobotMap;
import org.usfirst.frc.team1072.robot.RobotMap.CAN_IDs;
import org.usfirst.frc.team1072.robot.RobotMap.DrivetrainConstants;
import org.usfirst.frc.team1072.robot.RobotMap.ElevatorConstants;
import org.usfirst.frc.team1072.robot.commands.elevator.MoveElevatorVelocity;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * Represents an Elevator subsystem with control over the four motors.
 * @author Finn Frankis
 * @version 6/14/18
 */
public class Elevator extends Subsystem
{
    /**
     * The instance of this singleton Elevator.
     */
    private static Elevator el;
    
    /**
     * The top right Victor on the elevator.
     */
    private VictorSPX topRightVictor;
    
    /**
     * The bottom left Victor on the elevator.
     */
    private VictorSPX bottomLeftVictor;
    
    /**
     * The top left Victor on the elevator.
     */
    private VictorSPX topLeftVictor;
    
    /**
     * The bottom right (and only) Talon on the elevator.
     */
    private TalonSRX bottomRightTalon; 
    
    /**
     * Initializes the command using the four ports in RobotMap.
     */
    public Elevator()
    {
        topRightVictor = new VictorSPX(CAN_IDs.ELEVATOR_VICTOR_TOPRIGHT);
        topLeftVictor = new VictorSPX (CAN_IDs.ELEVATOR_VICTOR_TOPLEFT);
        bottomLeftVictor = new VictorSPX(CAN_IDs.ELEVATOR_VICTOR_BOTTOMLEFT);
        bottomRightTalon = new TalonSRX(CAN_IDs.ELEVATOR_TALON);
    }

    protected void initDefaultCommand()
    {
        setDefaultCommand(new MoveElevatorVelocity());
    }
    
    /**
     * Moves the elevator given a velocity.
     * @param speed the speed with which the elevator will be moved
     */
    public void moveElevatorVelocity(double speed)
    {
        // feed forward counterracts gravity
        bottomRightTalon.set(ControlMode.PercentOutput, speed, DemandType.ArbitraryFeedForward, ElevatorConstants.POS_FGRAV);
    }
    
    /**
     * Moves the elevator given a position setpoint.
     * @param position the position to which the elevator will be moved
     */
    public void moveElevatorPosition(double position)
    {
        bottomRightTalon.set(ControlMode.Position, position, DemandType.ArbitraryFeedForward, ElevatorConstants.POS_FGRAV);
    }
    
    /**
     * Moves the elevator using motion magic. 
     * @param targetPos the position to which the elevatr will be moved
     */
    public void moveElevatorMotionMagic(double targetPos)
    {
        bottomRightTalon.set(ControlMode.MotionMagic, targetPos, DemandType.ArbitraryFeedForward, ElevatorConstants.POS_FGRAV);
    }
    
    /**
     * Initializes the elevator talon for autonomous.
     */
    public void talonInitAutonomous()
    {
        talonInit();
    }
    
    /**
     * Initializes the elevator talon for teleoperated.
     */
    public void talonInitTeleop()
    {
        talonInit();
        elSetSoftLimit(ElevatorConstants.FORWARD_SOFT, ElevatorConstants.REVERSE_SOFT);
        elSetCurrentLimit(ElevatorConstants.PEAK_CURRENT_LIMIT, ElevatorConstants.PEAK_TIME_MS,
                ElevatorConstants.CONTINOUS_CURRENT_LIMIT);
        elSetRampRate (ElevatorConstants.RAMP_RATE);
        elConfigurePositionClosedLoop();
    }
    /**
     * Initializes the talons and victors on the elevator.
     */
    public void talonInit()
    {
        elSlaveVictors();
        elSetNeutralMode(NeutralMode.Brake);

        elInvertControllers();

        elScaleVoltage(RobotMap.NOMINAL_BATTERY_VOLTAGE);
        
        elConfigureMotionMagic();

    }

    
    /**
     * Sets the ramp rate for closed loop elevator motion.
     * @param rampRate the time from zero to full voltage
     */
    private void elSetRampRate (double rampRate)
    {
        getBottomRightTalon().configOpenloopRamp(rampRate, RobotMap.TIMEOUT);
    }
    /**
     * Slaves the three elevator Victors to the one Talon.
     */
    private void elSlaveVictors()
    {
        getBottomLeftVictor().follow(getBottomRightTalon());
        getTopLeftVictor().follow(getBottomRightTalon());
        getTopRightVictor().follow(getBottomRightTalon());
    }

    /**
     * Inverts the motor controllers so that all align correctly.
     */
    private void elInvertControllers()
    {
        //getBottomLeftVictor().setInverted(ElevatorConstants.BOTTOM_LEFT_VICTOR_INVERT);
    }

    /**
     * Sets the correct neutral mode for the motor controllers on the elevator
     * 
     * @param n the neutral mode (coast or brake) to be set
     */
    private void elSetNeutralMode(NeutralMode n)
    {
        getBottomRightTalon().setNeutralMode(n);
        getTopRightVictor().setNeutralMode(n);
        getBottomLeftVictor().setNeutralMode(n);
        getTopLeftVictor().setNeutralMode(n);
    }

    /**
     * Scales the voltage on the elevator controllers given nominal voltage.
     * 
     * @param nomVoltage
     *            the nominal voltage of the battery
     */
    private void elScaleVoltage(double nomVoltage)
    {
        getBottomRightTalon().configVoltageCompSaturation(nomVoltage, RobotMap.TIMEOUT);
    }

    /**
     * Sets the elevator soft limit given it as a parameter.
     * 
     * @param forwardSoftLimit the forward soft limit
     * @param reverseSoftLimit the reverse soft limit
     */
    private void elSetSoftLimit(int forwardSoftLimit, int reverseSoftLimit)
    {
        getBottomRightTalon().configForwardSoftLimitThreshold(forwardSoftLimit, RobotMap.TIMEOUT);
        getBottomRightTalon().configForwardSoftLimitEnable(true, RobotMap.TIMEOUT);
        
        getBottomRightTalon().configReverseSoftLimitThreshold(reverseSoftLimit, RobotMap.TIMEOUT);
        getBottomRightTalon().configReverseSoftLimitEnable(true, RobotMap.TIMEOUT);
        
        getBottomRightTalon().overrideLimitSwitchesEnable(true);
    }

    /**
     * Configures the elevator position closed loop.
     * 
     * @postcondition the F/P/I/D constants have been set, as well as the nominal
     *                output in both directions
     */
    private void elConfigurePositionClosedLoop()
    {
        getBottomRightTalon().configNominalOutputForward(ElevatorConstants.NOMINAL_OUTPUT, RobotMap.TIMEOUT);
        
        getBottomRightTalon().configNominalOutputReverse(-1 * ElevatorConstants.NOMINAL_OUTPUT, RobotMap.TIMEOUT);
        
        getBottomRightTalon().configPeakOutputForward(ElevatorConstants.PEAK_OUTPUT, RobotMap.TIMEOUT);
        
        getBottomRightTalon().configPeakOutputReverse(-1 * ElevatorConstants.PEAK_OUTPUT, RobotMap.TIMEOUT);
        
        getBottomRightTalon().config_kF(ElevatorConstants.POS_PID, ElevatorConstants.POS_KF, RobotMap.TIMEOUT);
        
        getBottomRightTalon().config_kP(ElevatorConstants.POS_PID, ElevatorConstants.POS_KP, RobotMap.TIMEOUT);
        
        getBottomRightTalon().config_kI(ElevatorConstants.POS_PID, ElevatorConstants.POS_KI, RobotMap.TIMEOUT);
        
        getBottomRightTalon().config_kD(ElevatorConstants.POS_PID, ElevatorConstants.POS_KD, RobotMap.TIMEOUT);
        
        getBottomRightTalon().configAllowableClosedloopError(DrivetrainConstants.POS_PID, ElevatorConstants.POS_ALLOWABLE_ERROR, RobotMap.TIMEOUT);
    }
    
    /**
     * Configures Motion Magic on the elevator, which seamlessly allows the robot to move between positions.
     */
    private void elConfigureMotionMagic()
    {
        // set motion magic port to be the velocity PID port 
        getBottomRightTalon().selectProfileSlot(ElevatorConstants.MOTION_MAGIC_PID, RobotMap.PRIMARY_PID_INDEX);
        getBottomRightTalon().config_kF(ElevatorConstants.MOTION_MAGIC_PID, ElevatorConstants.MOTION_MAGIC_KF, RobotMap.TIMEOUT);
        getBottomRightTalon().config_kP(ElevatorConstants.MOTION_MAGIC_PID, ElevatorConstants.MOTION_MAGIC_KP, RobotMap.TIMEOUT);
        getBottomRightTalon().config_kI(ElevatorConstants.MOTION_MAGIC_PID, ElevatorConstants.MOTION_MAGIC_KI, RobotMap.TIMEOUT);
        getBottomRightTalon().config_kD(ElevatorConstants.MOTION_MAGIC_PID, ElevatorConstants.MOTION_MAGIC_KD, RobotMap.TIMEOUT);
        
        getBottomRightTalon().configMotionCruiseVelocity(ElevatorConstants.MOTION_MAGIC_VEL, RobotMap.TIMEOUT);
        getBottomRightTalon().configMotionAcceleration(ElevatorConstants.MOTION_MAGIC_ACCEL, RobotMap.TIMEOUT);
        
        getBottomRightTalon().configAllowableClosedloopError(DrivetrainConstants.VEL_PID, ElevatorConstants.MOTION_MAGIC_ALLOWABLE_ERROR, RobotMap.TIMEOUT);
        
    }

    /**
     * Sets the current limit for the elevator in amps.
     * 
     * @param peakCurrentLimit
     *            the peak limit (only temporary)
     * @param peakTime
     *            the time (in ms) for which the peak limit is permitted
     * @param continuousLimit
     *            the limit after the peak time has expired.
     */
    private void elSetCurrentLimit(int peakCurrentLimit, int peakTime, int continuousLimit)
    {
        getBottomRightTalon().configPeakCurrentLimit(peakCurrentLimit, RobotMap.TIMEOUT);
        getBottomRightTalon().configPeakCurrentDuration(peakTime, RobotMap.TIMEOUT);
        getBottomRightTalon().configContinuousCurrentLimit(continuousLimit, RobotMap.TIMEOUT);
        getBottomRightTalon().enableCurrentLimit(true);
    }

    /**
     * Zeros the elevator sensor, should always be called during initialization.
     */
    private void elZeroSensors()
    {
        getBottomRightTalon().setSelectedSensorPosition(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
    }
    /**
     * Gets the top right Victor on the elevator.
     * @return the top right Victor
     */
    public VictorSPX getTopRightVictor() { return topRightVictor; }

    /**
     * Gets the bottom left Victor on the elevator.
     * @return the bottom left Victor
     */
    public VictorSPX getBottomLeftVictor() { return bottomLeftVictor; }

    /**
     * Gets the top left Victor on the elevator.
     * @return the top left Victor
     */
    public VictorSPX getTopLeftVictor() { return topLeftVictor; }

    /**
     * Gets the bottom right Talon on the elevator.
     * @return the bottom right Talon
     */
    public TalonSRX getBottomRightTalon() { return bottomRightTalon; }

    /**
     * Gets the instance of the singleton Elevator, creating a new one if necessary.
     * @return the instance of Elevator
     */
    public static Elevator getInstance()
    {
        if (el == null) el = new Elevator();
        return el;
    }
    
}