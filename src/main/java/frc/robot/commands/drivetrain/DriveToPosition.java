package frc.robot.commands.drivetrain;

import frc.robot.Robot;
import frc.robot.RobotMap;
import frc.robot.subsystems.Drivetrain;

import harkerrobolib.util.Conversions;
import harkerrobolib.util.Conversions.PositionUnit;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

/**
 * Drives the robot to a given position using PID.
 * @author Finn Frankis
 * @version Jun 14, 2018
 */
public class DriveToPosition extends PositionCommand
{
    /**
     * The desired position for this closed loop.
     */
    private double position;
    
    /**
     * Constructs a new DriveToPositionCommand.
     * @param position the final position for the robot in feet
     */
    public DriveToPosition (double position)
    {
        super (10, Conversions.convertPosition(PositionUnit.FEET, position, PositionUnit.ENCODER_UNITS));
        requires(Robot.dt);
        this.position = Conversions.convertPosition(PositionUnit.FEET, position, PositionUnit.ENCODER_UNITS);
    }
    
    /**
     * Initializes this command.
     */
    public void initialize()
    {
        initPosition();
        Robot.dt.setBothSensorPositions(0, RobotMap.PRIMARY_PID_INDEX);
        Robot.dt.setBoth(ControlMode.Position, position);
        Robot.dt.resetTalonCoefficients(RobotMap.PRIMARY_PID_INDEX);
    }
    
    /**
     * Initializes the position part of this command (to be used if combined with a subsequent turn).
     */
    private void initPosition()
    {
        Robot.dt.selectProfileSlots(Drivetrain.POS_PID, RobotMap.PRIMARY_PID_INDEX);
        
        Robot.dt.configBothFeedbackSensors(FeedbackDevice.CTRE_MagEncoder_Relative, RobotMap.PRIMARY_PID_INDEX);
        
        Robot.dt.setTalonSensorPhase(Drivetrain.LEFT_TALON_PHASE, Drivetrain.RIGHT_TALON_PHASE);
        
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
        Robot.dt.setBoth(ControlMode.Position, position);
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
            return Robot.dt.isClosedLoopErrorWithin(RobotMap.PRIMARY_PID_INDEX, getAllowableError());
        }
        return false;
    }
    
    public void end()
    {
        System.out.println("Command finished");
        Robot.dt.getLeftMaster().set(ControlMode.PercentOutput, 0);
        Robot.dt.getRightMaster().set(ControlMode.PercentOutput, 0);
    }
}
