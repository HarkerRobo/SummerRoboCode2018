package org.usfirst.frc.team1072.robot.subsystems;

import java.util.Map;

import org.usfirst.frc.team1072.robot.OI;
import org.usfirst.frc.team1072.robot.Robot;
import org.usfirst.frc.team1072.robot.RobotMap;
import org.usfirst.frc.team1072.robot.RobotMap.CAN_IDs;
import org.usfirst.frc.team1072.robot.RobotMap.DrivetrainConstants;
import org.usfirst.frc.team1072.robot.RobotMap.PigeonConstants;
import org.usfirst.frc.team1072.robot.commands.drivetrain.DriveWithVelocity;
import org.usfirst.frc.team1072.robot.commands.drivetrain.TurnToAngle;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.ctre.phoenix.sensors.PigeonIMU_StatusFrame;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import jaci.pathfinder.Trajectory.Segment;

/**
 * Represents a drive train with two Talons and two Victors.
 * @author Finn Frankis
 * @version 6/11/18
 */
public class Drivetrain extends Subsystem
{
    /**
     * The current instance of this singleton Drivetrain.
     */
    private static Drivetrain instance = null;
    
    /**
     * The left Talon on the drivetrain.
     */
    private TalonSRX leftTalon;
    
    /**
     * The right Talon on the drivetrain.
     */
    private TalonSRX rightTalon;
    
    /**
     * The left Victor on the drivetrain.
     */
    private VictorSPX leftVictor;
    
    /**
     * The right Victor on the drivetrain.
     */
    private VictorSPX rightVictor;
    
    /**
     * The Pigeon IMU for use on the drivetrain.
     */
    private PigeonIMU pigeon;
    
    /**
     * Initializes this subsystem.
     */
    private Drivetrain()
    {
        // initialize talons
        leftTalon = new TalonSRX (CAN_IDs.LEFT_CIM_TALON);
        rightTalon = new TalonSRX (CAN_IDs.RIGHT_CIM_TALON);
        leftVictor = new VictorSPX (CAN_IDs.LEFT_CIM_VICTOR);
        rightVictor = new VictorSPX (CAN_IDs.RIGHT_CIM_VICTOR);
        pigeon = new PigeonIMU(CAN_IDs.PIGEON);
    }
  
    /**
     * Initializes the command using the necessary default command.
     */
    public void initDefaultCommand()
    {
        setDefaultCommand(new DriveWithVelocity(OI.BLACK_XBOX_DEADBAND));
    }
    
    /**
     * Runs both motor controllers given a velocity and turn.
     * @param speed the speed at which the controllers should run
     * @param turn the amount by which each speed should be modified to account for turn
     */
    public void arcadeDriveVelocity(double speed, double turn)
    {
        // victor follows talon
        rightTalon.set(ControlMode.Velocity, speed + turn);  
        leftTalon.set(ControlMode.Velocity, speed - turn);  

    }
    
    /**
     * Moves the robot to a given position using PID.
     * @param target the target position 
     */
    public void arcadeDrivePosition (double target)
    {
        rightTalon.set(ControlMode.Position, target);
    }
    
    /**
     * Moves the robot left and right sides to given positions using PID.
     * @param leftTarget the target position for the left side
     * @param rightTarget the target position for the right side
     */
    public void arcadeDrivePosition (double leftTarget, double rightTarget)
    {        
        leftTalon.set(ControlMode.Position, leftTarget);
        rightTalon.set(ControlMode.Position, rightTarget);
    }
    
    /**
     * Performs autonomous-specific commands on the Talon.
     */
    public void talonInitAutonomous()
    {
        talonInit();
    }
    
    /**
     * Performs teleop-specific commands on the Talon.
     */
    public void talonInitTeleop()
    {
        talonInit();
        configureVelocityClosedLoop();
        setRampTime(DrivetrainConstants.MAX_RAMP_TIME);
        
    }
    /**
     * Performs all commands to initialize the talons.
     * 
     * @postcondition the talons are in the correct state to begin robot operation
     */
    public void talonInit()
    {
        zeroAllSensors();
        zeroPigeon();
        setTalonDeadbands();
        initTalonOutput(0);

        clearTrajectoryPoints();

        victorInit();
        
        invertControllers();
        setNeutralMode(NeutralMode.Brake);
        
        ;
        scaleVoltage(RobotMap.NOMINAL_BATTERY_VOLTAGE);
        configureNominalPeakOutputs();
        
        ;
        dtsetTalonFramePeriods();
        
        configurePositionClosedLoop();
        configureMotionProfileDriveClosedLoop();

        dtSetCurrentLimit(DrivetrainConstants.PEAK_CURRENT_LIMIT, DrivetrainConstants.PEAK_TIME_MS,
                DrivetrainConstants.CONTINUOUS_CURRENT_LIMIT);

    }
    
    /**
     * Configures the deadbands for the Talons (the output below which the output is essentially zero).
     */
    private void setTalonDeadbands()
    {
        getLeftTalon().configNeutralDeadband(DrivetrainConstants.TALON_DEADBAND, RobotMap.TIMEOUT);
        getRightTalon().configNeutralDeadband(DrivetrainConstants.TALON_DEADBAND, RobotMap.TIMEOUT);
    }

    /**
     * Sets both talons to a given value.
     * @param cm the ControlMode to which both talons will be set (like PercentOutput, Velocity, Position, or Disabled)
     * @param value the value to which both talons will be set
     */
    public void setBoth (ControlMode cm, double value)
    {
        setLeft(cm, value);
        setRight(cm, value);
    }
    
    /**
     * Sets the left talon to a given value.
     * @param cm the ControlMode to which the left talon will be set 
     * (like PercentOutput, Velocity, Position, or Disabled)
     * @param value the value to which the left talon will be set
     */
    public void setLeft (ControlMode cm, double value)
    {
        Robot.dt.getLeftTalon().set(cm, value);
    }
    
    /**
     * Sets the right talon to a given value.
     * @param cm the ControlMode to which the right talon will be set 
     * (like PercentOutput, Velocity, Position, or Disabled)
     * @param value the value to which the right talon will be set
     */
    public void setRight (ControlMode cm, double value)
    {
        Robot.dt.getRightTalon().set(cm, value);
    }
    
    /**
     * Sets both talon sensor positions to a given value in a given PID loop.
     * @param value the value to which both will be set
     * @param pidLoop the loop index (primary/auxiliary) [0,1]
     */
    public void setBothSensorPositions (int value, int pidLoop)
    {
        getLeftTalon().setSelectedSensorPosition(value, pidLoop, RobotMap.TIMEOUT);
        getRightTalon().setSelectedSensorPosition(value, pidLoop, RobotMap.TIMEOUT);
    }
    
    /**
     * Clears trajectory points from both talons.
     */
    public void clearTrajectoryPoints()
    {
        Robot.dt.getLeftTalon().clearMotionProfileTrajectories();
        Robot.dt.getRightTalon().clearMotionProfileTrajectories();
    }
    
    /**
     * Initializes the talon output to a given value.
     * @param output the output to which both talons will be initialized
     */
    public void initTalonOutput(double output)
    {
        setBoth(ControlMode.PercentOutput, output);
    }

    /**
     * Slaves the Victors to directly follow the behavior of their parent talons.
     */
    private void victorInit()
    {
        getLeftVictor().follow(getLeftTalon());
        getRightVictor().follow(getRightTalon());
    }
    
    /**
     * Resets the coefficients in all four PID slots on the Talons.
     */
    public void resetTalonCoefficients()
    {
        // for all PID slots
        for (int slot = 0; slot < 2; slot++)
        {
            resetTalonCoefficients(slot);
        }
    }
    
    /**
     * Resets the coefficients stored in a given PID slot.
     * @param pid_slot the slot where the coefficients will be reset
     */
    public void resetTalonCoefficients (int pid_slot)
    {
        getLeftTalon().configSelectedFeedbackCoefficient(1,
                pid_slot, RobotMap.TIMEOUT); 
        getRightTalon().configSelectedFeedbackCoefficient(1,
                pid_slot, RobotMap.TIMEOUT); 
    }
    
    /**
     * Zeroes all sensors currently configured for the Talons.
     */
    private void zeroAllSensors()
    {
        getLeftTalon().setSelectedSensorPosition(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        getRightTalon().setSelectedSensorPosition(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
    }
   

    /**
     * Inverts the Talons and Victors to account for wiring inconsistencies (must be
     * tested).
     */
    private void invertControllers()
    {
        getLeftTalon().setInverted(DrivetrainConstants.LEFT_TALON_INVERT);
        getLeftVictor().setInverted(DrivetrainConstants.LEFT_VICTOR_INVERT);
        
        // Invert the following direction (left Talons and Victors were wired
        // oppositely)
    }

    /**
     * Sets the neutral mode to either coast or brake.
     * 
     * @param n
     *            either coast or brake
     */
    private void setNeutralMode(NeutralMode n)
    {
        getLeftTalon().setNeutralMode(n);
        getLeftVictor().setNeutralMode(n);
        getRightTalon().setNeutralMode(n);
        getRightVictor().setNeutralMode(n);
    }

    /**
     * Configure the talons to ramp gradually to peak voltage.
     * 
     * @param t
     *            the ramp time (time from 0 voltage to max)
     */
    private void setRampTime(double t)
    {
        getLeftTalon().configOpenloopRamp(t, RobotMap.TIMEOUT);
        getRightTalon().configOpenloopRamp(t, RobotMap.TIMEOUT);
    }

    /**
     * Scales the voltage to modify percent output to account for the battery
     * voltage loss.
     * 
     * @param nomVoltage
     *            the assumed nominal voltage, used to scale relative to battery
     *            voltage
     */
    private void scaleVoltage(double nomVoltage)
    {
        getLeftTalon().configVoltageCompSaturation(nomVoltage, RobotMap.TIMEOUT);
        getRightTalon().configVoltageCompSaturation(nomVoltage, RobotMap.TIMEOUT);
        getLeftTalon().enableVoltageCompensation(true);
        getRightTalon().enableVoltageCompensation(true);
    }


    /**
     * Aligns the sensor phase of the encoders to match the motions of the motors.
     * @param leftPhase the left sensor phase 
     * @param rightPhase the right sensor phase
     */
    public void setTalonSensorPhase(boolean leftPhase, boolean rightPhase)
    {
        getLeftTalon().setSensorPhase(leftPhase);
        getRightTalon().setSensorPhase(rightPhase);
    }

    /**
     * Configures the drivetrain velocity closed loop.
     * 
     * @postcondition nominal and peak output in both directions has been set
     */
    private void configureVelocityClosedLoop()
    {
        getLeftTalon().config_kF(DrivetrainConstants.VEL_PID, DrivetrainConstants.VEL_KF_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kF(DrivetrainConstants.VEL_PID, DrivetrainConstants.VEL_KF_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kP(DrivetrainConstants.VEL_PID, DrivetrainConstants.VEL_KP_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kP(DrivetrainConstants.VEL_PID, DrivetrainConstants.VEL_KP_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kI(DrivetrainConstants.VEL_PID, DrivetrainConstants.VEL_KI_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kI(DrivetrainConstants.VEL_PID, DrivetrainConstants.VEL_KI_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kD(DrivetrainConstants.VEL_PID, DrivetrainConstants.VEL_KD_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kD(DrivetrainConstants.VEL_PID, DrivetrainConstants.VEL_KD_RIGHT, RobotMap.TIMEOUT);
      
    }

    /**
     * Configures the nominal and peak outputs for the two Talons, where nominal output is the minimum percent which can be applied (without breaking static friction)
     * and peak output is the maximum percent which can be applied.
     */
    public void configureNominalPeakOutputs()
    {
        getLeftTalon().configNominalOutputForward(DrivetrainConstants.NOMINAL_OUTPUT_LEFT, RobotMap.TIMEOUT);
        getRightTalon().configNominalOutputForward(DrivetrainConstants.NOMINAL_OUTPUT_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().configNominalOutputReverse(-1 * DrivetrainConstants.NOMINAL_OUTPUT_LEFT, RobotMap.TIMEOUT);
        getRightTalon().configNominalOutputReverse(-1 * DrivetrainConstants.NOMINAL_OUTPUT_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().configPeakOutputForward(DrivetrainConstants.PEAK_OUTPUT_LEFT, RobotMap.TIMEOUT);
        getRightTalon().configPeakOutputForward(DrivetrainConstants.PEAK_OUTPUT_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().configPeakOutputReverse(-1 * DrivetrainConstants.PEAK_OUTPUT_LEFT, RobotMap.TIMEOUT);
        getRightTalon().configPeakOutputReverse(-1 * DrivetrainConstants.PEAK_OUTPUT_RIGHT, RobotMap.TIMEOUT);
    }
    /**
     * Configures the drivetrain position closed loop.
     * 
     * @postcondition each talon has configured F, P, I, D and D constants, the
     *                encoders have been zeroed, and the allowable error has been
     *                set
     */
    private void configurePositionClosedLoop()
    {
        getLeftTalon().configAllowableClosedloopError(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_ALLOWABLE_ERROR, RobotMap.TIMEOUT);
        getRightTalon().configAllowableClosedloopError(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_ALLOWABLE_ERROR, RobotMap.TIMEOUT);

        getLeftTalon().config_kF(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_KF_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kF(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_KF_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kP(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_KP_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kP(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_KP_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kI(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_KI_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kI(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_KI_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kD(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_KD_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kD(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_KD_RIGHT, RobotMap.TIMEOUT);
        
        getLeftTalon().config_IntegralZone(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_IZONE_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_IntegralZone(DrivetrainConstants.POS_PID, DrivetrainConstants.POS_IZONE_RIGHT, RobotMap.TIMEOUT);
    }
    
    /**
     * Selects the profile slots of both talons to their correct places.
     * @param pidSlot the PID slot on the Talon [0, 3]
     * @param pidIndex the PID index (primary/auxiliary or inner/outer) [0, 1]
     */
    public void selectProfileSlots(int pidSlot, int pidIndex)
    {
        getLeftTalon().selectProfileSlot(pidSlot, pidIndex);
        getRightTalon().selectProfileSlot(pidSlot, pidIndex);
    }
    
    /**
     * Configures the angle closed loop for turning in place.
     */
    public void configureAngleClosedLoop()
    {
        getPigeon().setStatusFramePeriod(PigeonIMU_StatusFrame.CondStatus_9_SixDeg_YPR, PigeonConstants.PERIOD_MS, RobotMap.TIMEOUT);
        
        getRightTalon().config_kF(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_KF, RobotMap.TIMEOUT);
        getRightTalon().config_kP(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_KP, RobotMap.TIMEOUT);
        getRightTalon().config_kI(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_KI, RobotMap.TIMEOUT);
        getRightTalon().config_kD(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_KD, RobotMap.TIMEOUT);
        
        getRightTalon().configMotionCruiseVelocity(PigeonConstants.TURN_VEL, RobotMap.TIMEOUT);
        getRightTalon().configMotionAcceleration(PigeonConstants.TURN_ACCEL, RobotMap.TIMEOUT);
        
        getRightTalon().config_IntegralZone(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_IZONE, RobotMap.TIMEOUT);
        
        
        getLeftTalon().config_kF(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_KF, RobotMap.TIMEOUT);
        getLeftTalon().config_kP(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_KP, RobotMap.TIMEOUT);
        getLeftTalon().config_kI(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_KI, RobotMap.TIMEOUT);
        getLeftTalon().config_kD(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_KD, RobotMap.TIMEOUT);
        
        getLeftTalon().configMotionCruiseVelocity(PigeonConstants.TURN_VEL, RobotMap.TIMEOUT);
        getLeftTalon().configMotionAcceleration(PigeonConstants.TURN_ACCEL, RobotMap.TIMEOUT);
        
        getLeftTalon().config_IntegralZone(DrivetrainConstants.ANGLE_PID, PigeonConstants.TURN_IZONE, RobotMap.TIMEOUT);
    }
    
    /**
     * Configures the angle closed loop for motion profiling.
     * 
     * @postcondition P, I, and D have been set for both sides; the pigeon zeroed; the frame period set
     */
    public void configureMotionProfileAngleClosedLoop()
    {
        getPigeon().setStatusFramePeriod(PigeonIMU_StatusFrame.CondStatus_9_SixDeg_YPR, PigeonConstants.PERIOD_MS, RobotMap.TIMEOUT);
        
        getRightTalon().config_kF(DrivetrainConstants.ANGLE_PID, PigeonConstants.MOT_PROF_KF, RobotMap.TIMEOUT);
        getRightTalon().config_kP(DrivetrainConstants.ANGLE_PID, PigeonConstants.MOT_PROF_KP, RobotMap.TIMEOUT);
        getRightTalon().config_kI(DrivetrainConstants.ANGLE_PID, PigeonConstants.MOT_PROF_KI, RobotMap.TIMEOUT);
        getRightTalon().config_kD(DrivetrainConstants.ANGLE_PID, PigeonConstants.MOT_PROF_KD, RobotMap.TIMEOUT);
        
        getLeftTalon().config_kF(DrivetrainConstants.ANGLE_PID, PigeonConstants.MOT_PROF_KF, RobotMap.TIMEOUT);
        getLeftTalon().config_kP(DrivetrainConstants.ANGLE_PID, PigeonConstants.MOT_PROF_KP, RobotMap.TIMEOUT);
        getLeftTalon().config_kI(DrivetrainConstants.ANGLE_PID, PigeonConstants.MOT_PROF_KI, RobotMap.TIMEOUT);
        getLeftTalon().config_kD(DrivetrainConstants.ANGLE_PID, PigeonConstants.MOT_PROF_KD, RobotMap.TIMEOUT);

    }

    /**
     * Configures the motion profile closed loop.
     * 
     * @postcondition P, I, and D have been set for both sides; the allowable error configured
     */
    public void configureMotionProfileDriveClosedLoop()
    {
        getLeftTalon().configAllowableClosedloopError(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.POS_ALLOWABLE_ERROR, RobotMap.TIMEOUT);
        getRightTalon().configAllowableClosedloopError(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.POS_ALLOWABLE_ERROR, RobotMap.TIMEOUT);

        getLeftTalon().config_kF(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.MOTION_PROF_KF_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kF(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.MOTION_PROF_KF_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kP(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.MOTION_PROF_KP_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kP(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.MOTION_PROF_KP_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kI(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.MOTION_PROF_KI_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kI(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.MOTION_PROF_KI_RIGHT, RobotMap.TIMEOUT);

        getLeftTalon().config_kD(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.MOTION_PROF_KD_LEFT, RobotMap.TIMEOUT);
        getRightTalon().config_kD(DrivetrainConstants.MOTION_PROFILE_PID, DrivetrainConstants.MOTION_PROF_KD_RIGHT, RobotMap.TIMEOUT);
    }
    
    
    /**
     * Resets the Talon frame periods (the frequency at which the Talons will broadcast certain status frames along the CAN bus).
     */
    private void dtsetTalonFramePeriods()
    {
        getLeftTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_1_General, 20, RobotMap.TIMEOUT);
        getLeftTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 3, RobotMap.TIMEOUT);
        getLeftTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_3_Quadrature, RobotMap.MAX_TALON_FRAME_PERIOD_MS, RobotMap.TIMEOUT);
        getLeftTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_9_MotProfBuffer, RobotMap.MAX_TALON_FRAME_PERIOD_MS, RobotMap.TIMEOUT);
        getLeftTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 20, RobotMap.TIMEOUT);
        getLeftTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 20, RobotMap.TIMEOUT);
        getLeftTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_14_Turn_PIDF1, 20, RobotMap.TIMEOUT);
        
        getRightTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_1_General, 20, RobotMap.TIMEOUT);
        getRightTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 3, RobotMap.TIMEOUT);
        getRightTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_9_MotProfBuffer, 20, RobotMap.TIMEOUT);
        getRightTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_11_UartGadgeteer, RobotMap.MAX_TALON_FRAME_PERIOD_MS, RobotMap.TIMEOUT);
        getRightTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, RobotMap.TIME_PER_TRAJECTORY_POINT_MS, RobotMap.TIMEOUT);
        getRightTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 20, RobotMap.TIMEOUT);
        getRightTalon().setStatusFramePeriod(StatusFrameEnhanced.Status_14_Turn_PIDF1, 20, RobotMap.TIMEOUT);

        
    }

    /**
     * Sets a current limit for the left and right talons on the drivetrain.
     * 
     * @param peakCurrentLimit
     *            the peak current limit (only temporary)
     * @param peakTime
     *            the time for which the peak current limit is allowed
     * @param continuousLimit
     *            the continuous limit (represents the limit after peakTime passes)
     */
    private void dtSetCurrentLimit(int peakCurrentLimit, int peakTime, int continuousLimit)
    {
        getLeftTalon().configPeakCurrentLimit(peakCurrentLimit, RobotMap.TIMEOUT);
        getRightTalon().configPeakCurrentLimit(peakCurrentLimit, RobotMap.TIMEOUT);

        getLeftTalon().configPeakCurrentDuration(peakTime, RobotMap.TIMEOUT);
        getRightTalon().configPeakCurrentDuration(peakTime, RobotMap.TIMEOUT);

        getLeftTalon().configContinuousCurrentLimit(continuousLimit, RobotMap.TIMEOUT);
        getRightTalon().configContinuousCurrentLimit(continuousLimit, RobotMap.TIMEOUT);

        getLeftTalon().enableCurrentLimit(true);
        getRightTalon().enableCurrentLimit(true);
    }
    
    /**
     * Gets the left Talon on the drivetrain.
     * @return the left Talon
     */
    public TalonSRX getLeftTalon() { return leftTalon; }

    /**
     * Gets the right Talon on the drivetrain.
     * @return the right Talon
     */
    public TalonSRX getRightTalon() { return rightTalon; }

    /**
     * Gets the left Victor on the drivetrain.
     * @return the left Victor
     */
    public VictorSPX getLeftVictor() { return leftVictor; }
    
    /**
     * Gets the right Victor on the drivetrain.
     * @return the right Victor.
     */
    public VictorSPX getRightVictor() { return rightVictor; }

    /**
     * Gets the instance of this Drivetrain, creating a new one if necessary.
     * @return the instance of this singleton class
     */
    public static Drivetrain getInstance()
    {
        if (instance == null) instance = new Drivetrain();
        return instance;
    }
    /**
     * Gets the pigeon for this drivetrain.
     * @return the pigeon
     */
    public PigeonIMU getPigeon()
    {
        return pigeon;
    }
    
    /**
     * Gets the current yaw value of the pigeon.
     * @return the yaw
     */
    public double getPigeonYaw()
    {
        double[] ypr = new double[3];
        pigeon.getYawPitchRoll(ypr);
        return ypr[0];
    }
    
    /**
     * Gets the current pitch value of the pigeon.
     * @return the pitch
     */
    public double getPigeonPitch()
    {
        double[] ypr = new double[3];
        pigeon.getYawPitchRoll(ypr);
        return ypr[1];
    }
    
    /**
     * Gets the current roll value of the pigeon.
     * @return the roll
     */
    public double getPigeonRoll()
    {
        double[] ypr = new double[3];
        pigeon.getYawPitchRoll(ypr);
        return ypr[2];
    }
    
    /**
     * Sets the pigeon yaw to a given value.
     * @param angle the angle value to which the pigeon should be set, in pigeon units 
     * where 1 rotation is 8192 units
     */
    public void setPigeonYaw(double angle)
    {
        getPigeon().setYaw(angle * 64, RobotMap.TIMEOUT);
    }
    
    /**
     * Adds a given value to the pigeon yaw.
     * @param angle the angle value which should be added to the pigeon yaw value, in pigeon units 
     * where 1 rotation is 8192 units
     */
    public void addPigeonYaw (double angle)
    {
        getPigeon().addYaw(angle * 64, RobotMap.TIMEOUT);
    }
 
    /**
     * Zeros the pigeon.
     */
    public void zeroPigeon()
    {
        getPigeon().setYaw(0, RobotMap.TIMEOUT);
        getPigeon().setAccumZAngle(0, RobotMap.TIMEOUT);
    }
    
    /**
     * Configures both Talons to point to a given sensor.
     * @param fd the type of sensor to which the Talon should use
     * @param pidLoop the loop index where this sensor should be placed [0,1]
     */
    public void configBothFeedbackSensors(FeedbackDevice fd, int pidLoop)
    {
        getLeftTalon().configSelectedFeedbackSensor(fd, 
                    pidLoop, RobotMap.TIMEOUT);
        getRightTalon().configSelectedFeedbackSensor(fd, 
                    pidLoop, RobotMap.TIMEOUT);
    }
    
    /**
     * Prints the current output percentage to the motors to SmartDashboard.
     */
    public void printMotorOutputPercentage()
    {
        SmartDashboard.putNumber("Left Talon Output Percentage", Robot.dt.getLeftTalon().getMotorOutputPercent());
        SmartDashboard.putNumber("Right Talon Output Percentage", Robot.dt.getRightTalon().getMotorOutputPercent());
    }
    
    /**
     * Prints the closed loop error of the Talons in a given loop.
     * @param pidLoop the loop index [0,1]
     */
    public void printClosedLoopError (int pidLoop)
    {
        SmartDashboard.putNumber("Left Talon Closed Loop Error " + (pidLoop == 0 ? "Primary" : "Auxiliary"), Robot.dt.getLeftTalon().getClosedLoopError(pidLoop));
        SmartDashboard.putNumber("Right Talon Closed Loop Error " + (pidLoop == 0 ? "Primary" : "Auxiliary"), Robot.dt.getRightTalon().getClosedLoopError(pidLoop));
    }
    
    /**
     * Prints the sensor positions of the Talons in a given loop.
     * @param pidLoop the loop index [0,1]
     */
    public void printSensorPositions (int pidLoop)
    {
        SmartDashboard.putNumber("Left Talon Position " + (pidLoop == 0 ? "Primary" : "Auxiliary"), Robot.dt.getLeftTalon().getSelectedSensorPosition(pidLoop));
        SmartDashboard.putNumber("Right Talon Position" + (pidLoop == 0 ? "Primary" : "Auxiliary"), Robot.dt.getRightTalon().getSelectedSensorPosition(pidLoop));
    }
    
    /**
     * Determines whether the closed loop error for both sides is within a given value.
     * @param loopIndex the loop index, either primary or auxiliary [0,1]
     * @param allowableError the error tolerance to be checked
     * @return true if the absolute value of the error is within the value; false otherwise
     */
    public boolean isClosedLoopErrorWithin (int loopIndex, double allowableError)
    {
        return Math.abs(getLeftTalon().getClosedLoopError(loopIndex)) < allowableError
                && Math.abs(getRightTalon().getClosedLoopError(loopIndex)) < allowableError;
    }

}
