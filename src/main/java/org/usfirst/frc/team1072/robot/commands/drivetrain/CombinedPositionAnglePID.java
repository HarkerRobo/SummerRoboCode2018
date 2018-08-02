package org.usfirst.frc.team1072.robot.commands.drivetrain;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.RemoteFeedbackDevice;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.motorcontrol.SensorTerm;

import org.usfirst.frc.team1072.robot.Robot;
import org.usfirst.frc.team1072.robot.RobotMap;
import org.usfirst.frc.team1072.robot.RobotMap.DrivetrainConstants;
import org.usfirst.frc.team1072.util.Conversions;
import org.usfirst.frc.team1072.util.Conversions.AngleUnit;
import org.usfirst.frc.team1072.util.Conversions.PositionUnit;

/**
 * Combines position and angle for a smoother PID-based autonomous.
 * @author Finn Frankis
 * @version Jul 14, 2018
 */
public class CombinedPositionAnglePID extends PositionCommand
{

    /**
     * The desired position for this closed loop.
     */
    private double position;
    
    /**
     * The desired angle for this closed loop.
     */
    private double angle;
    
    /**
     * Constructs a new CombinedPositionAnglePID.
     * @param position the final position for the robot in feet
     * @param angle the final angle for the robot in feet
     */
    public CombinedPositionAnglePID (double position, double angle)
    {
        super (15, Conversions.convertPosition(PositionUnit.FEET, position, PositionUnit.ENCODER_UNITS));
        this.position = Conversions.convertPosition(PositionUnit.FEET, position, PositionUnit.ENCODER_UNITS);
        this.angle = Conversions.convertAngle(AngleUnit.DEGREES, angle, AngleUnit.PIGEON_UNITS);
    }
    
    /**
     * Initializes this command.
     */
    public void initialize()
    {
        initPosition();
        ;
        ;
        ;
        ;
        ;
        //Robot.dt.getRightTalon().setSelectedSensorPosition(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().set(ControlMode.Position, position, DemandType.AuxPID, angle);
    }
    
    /**
     * Initializes the position part of this command (to be used if combined with a subsequent turn).
     */
    private void initPosition()
    {   
        Robot.dt.getLeftTalon().getSensorCollection().setQuadraturePosition(0, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().getSensorCollection().setQuadraturePosition(0, RobotMap.TIMEOUT);
        
        Robot.dt.getRightTalon().selectProfileSlot(DrivetrainConstants.POS_PID, RobotMap.PRIMARY_PID_INDEX);
        Robot.dt.getRightTalon().selectProfileSlot(DrivetrainConstants.ANGLE_PID, RobotMap.AUXILIARY_PID_INDEX);
        Robot.dt.getLeftTalon().follow(Robot.dt.getRightTalon(), FollowerType.AuxOutput1);
        
        Robot.dt.getRightTalon().configRemoteFeedbackFilter(Robot.dt.getPigeon().getDeviceID(), 
                RemoteSensorSource.Pigeon_Yaw, RobotMap.REMOTE_SLOT_0, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().configRemoteFeedbackFilter(Robot.dt.getLeftTalon().getDeviceID(), 
                RemoteSensorSource.TalonSRX_SelectedSensor, RobotMap.REMOTE_SLOT_1, RobotMap.TIMEOUT);

        
        Robot.dt.getLeftTalon().configSelectedFeedbackSensor(
                FeedbackDevice.CTRE_MagEncoder_Relative, 
                RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        
        Robot.dt.getRightTalon().configSensorTerm(SensorTerm.Sum0, FeedbackDevice.CTRE_MagEncoder_Relative, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().configSensorTerm(SensorTerm.Sum1, FeedbackDevice.RemoteSensor1, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().configSelectedFeedbackSensor(
                FeedbackDevice.SensorSum, 
                RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().configSelectedFeedbackSensor(RemoteFeedbackDevice.RemoteSensor0, 
                RobotMap.AUXILIARY_PID_INDEX, RobotMap.TIMEOUT);
        
        Robot.dt.getLeftTalon().configSelectedFeedbackCoefficient(1, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().configSelectedFeedbackCoefficient(0.5, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        Robot.dt.setTalonSensorPhase(DrivetrainConstants.LEFT_TALON_PHASE, DrivetrainConstants.RIGHT_TALON_PHASE);
        
    }
    
    /**
     * Executes this command periodically.
     */
    public void execute()
    {
        if (!passedMaxExecutes())
        {
            incrementNumExecutes();
        }

        Robot.dt.getRightTalon().set(ControlMode.Position, position, DemandType.AuxPID, angle);
    }
    /**
    * Determines whether this command has finished.
    * @return true if the error is within POS_ALLOWABLE_ERROR; false otherwise
    */
    @Override
    protected boolean isFinished()
    {
        if (passedMaxExecutes())
        {
            return Math.abs(Robot.dt.getRightTalon().getClosedLoopError(RobotMap.PRIMARY_PID_INDEX))
                    < DrivetrainConstants.POS_ALLOWABLE_ERROR;
        }
        //;
        return false;
    }
    
    public void end()
    {
        Robot.dt.getLeftTalon().set(ControlMode.PercentOutput, 0);
        Robot.dt.getRightTalon().set(ControlMode.PercentOutput, 0);
        ;
        ;
        ;
        ;
        ;
    }
    
}
