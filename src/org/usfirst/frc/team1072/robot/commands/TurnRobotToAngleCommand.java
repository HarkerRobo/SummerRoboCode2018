package org.usfirst.frc.team1072.robot.commands;

import org.usfirst.frc.team1072.robot.OI;
import org.usfirst.frc.team1072.robot.Robot;
import org.usfirst.frc.team1072.robot.RobotMap;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TurnRobotToAngleCommand extends Command
{
    private double angle;
    
    public TurnRobotToAngleCommand()
    {
        requires (Robot.dt);
        angle = -1;
    }
    /**
     * 
     * @param angle the angle to which the robot should turn (in degrees)
     */
    public TurnRobotToAngleCommand(double angle)
    {
        requires (Robot.dt);
        this.angle = angle * RobotMap.PIGEON_UNITS_PER_ROTATION/360;
    }
    
    public void initialize()
    {
        Robot.dt.selectProfileSlots(RobotMap.DT_ANGLE_PID, RobotMap.PRIMARY_PID_INDEX);
        
        Robot.dt.getLeftTalon().configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0, 
                RobotMap.PRIMARY_PID_INDEX, 
                RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0, 
                RobotMap.PRIMARY_PID_INDEX, 
                RobotMap.TIMEOUT);
        
        Robot.dt.getLeftTalon().setSelectedSensorPosition(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().setSelectedSensorPosition(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        
        Robot.dt.getLeftTalon().configSelectedFeedbackCoefficient(0.5, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        Robot.dt.getRightTalon().configSelectedFeedbackCoefficient(0.5, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
        
    }
    @Override
    public void execute()
    {
        Robot.dt.getLeftTalon().setSensorPhase(false);
        Robot.dt.getRightTalon().setSensorPhase(false);
        if (angle == -1)
        {
            OI oi = new OI();
    
            double joystickRight = oi.getGamepad().getLeftX() *RobotMap.PIGEON_UNITS_PER_ROTATION / 2;
            if (Math.abs(oi.getGamepad().getLeftX()) < 0.1)
                joystickRight = 0;
            Robot.dt.getRightTalon().selectProfileSlot(RobotMap.DT_ANGLE_PID, RobotMap.PRIMARY_PID_INDEX);
            Robot.dt.getLeftTalon().selectProfileSlot(RobotMap.DT_ANGLE_PID, RobotMap.PRIMARY_PID_INDEX);
            Robot.dt.getRightTalon().set(ControlMode.Position, joystickRight);
            Robot.dt.getLeftTalon().set(ControlMode.Position, -1 * joystickRight);
            if (Math.abs(Robot.dt.getRightTalon().getClosedLoopError(RobotMap.PRIMARY_PID_INDEX)) > RobotMap.ANGLE_INTEGRAL_BAND)   
                Robot.dt.getRightTalon().setIntegralAccumulator(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
            if (Math.abs(Robot.dt.getLeftTalon().getClosedLoopError(RobotMap.PRIMARY_PID_INDEX)) > RobotMap.ANGLE_INTEGRAL_BAND)   
                Robot.dt.getLeftTalon().setIntegralAccumulator(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
            SmartDashboard.putNumber("COMMANDED VALUE TO PIGEON", joystickRight);
            SmartDashboard.putNumber("Pigeon Talon Error", Robot.dt.getRightTalon().getClosedLoopError(RobotMap.PRIMARY_PID_INDEX));//joystickRight - Robot.dt.getRightTalon().getSelectedSensorPosition(RobotMap.DT_ANGLE_PID));
        }
        else
        {
            if (Math.abs(Robot.dt.getRightTalon().getClosedLoopError(RobotMap.PRIMARY_PID_INDEX)) > RobotMap.ANGLE_INTEGRAL_BAND)   
                Robot.dt.getRightTalon().setIntegralAccumulator(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
            if (Math.abs(Robot.dt.getLeftTalon().getClosedLoopError(RobotMap.PRIMARY_PID_INDEX)) > RobotMap.ANGLE_INTEGRAL_BAND)   
                Robot.dt.getLeftTalon().setIntegralAccumulator(0, RobotMap.PRIMARY_PID_INDEX, RobotMap.TIMEOUT);
            
            Robot.dt.getLeftTalon().set(ControlMode.Position, -1 * angle);
            Robot.dt.getRightTalon().set(ControlMode.Position, angle);
        }
        

    }
    @Override
    protected boolean isFinished()
    {
        return false;
        //return Robot.dt.getRightTalon().getClosedLoopError(arg0) < RobotMap.ANGLE_INTEGRAL_BAND;
    }
    
    @Override
    public void end()
    {
    }

}
