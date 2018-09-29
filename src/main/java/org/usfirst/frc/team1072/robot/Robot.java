/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team1072.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;

import org.usfirst.frc.team1072.robot.RobotMap.DrivetrainConstants;
import org.usfirst.frc.team1072.robot.commands.auton.AutonomousCommand;
import org.usfirst.frc.team1072.robot.commands.auton.AutonomousCommand.AutonType;
import org.usfirst.frc.team1072.robot.commands.auton.AutonomousCommand.RobotLocation;
import org.usfirst.frc.team1072.robot.subsystems.Drivetrain;
import org.usfirst.frc.team1072.robot.subsystems.Elevator;
import org.usfirst.frc.team1072.robot.subsystems.Intake;
import org.usfirst.frc.team1072.util.Conversions;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;

/**
 * Represents the central code for the robot.
 * @author Finn Frankis 
 * @version 6/11/18
 */
public class Robot extends TimedRobot
{
    /**
     * The drivetrain on the robot.
     */
    public static Drivetrain dt;
    
    /**
     * The intake on the robot.
     */
    public static Intake intake;
    
    /**
     * The elevator on the robot.
     */
    public static Elevator el;

    /**
     * The time at which the program began.
     */
    public static double startTime;
    
    /**
     * The class to hold all constants and methods to deal with input and output.
     */
    public static OI oi;

    /**
     * The command to be utilized during autonomous.
     */
    public static AutonomousCommand m_autonomousCommand;
    
    /**
     * The autonomous chooser.
     */
    SendableChooser<RobotLocation> loc_chooser = new SendableChooser<RobotLocation>();
    
    private String gameData = "";
    
    private Subsystem[] subsystems;
    

    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */
    public void robotInit() 
    {
        Conversions.setWheelDiameter(DrivetrainConstants.WHEELDIAMETER);
        intake = Intake.getInstance();
        dt = Drivetrain.getInstance();
        el = Elevator.getInstance();
        oi = OI.getInstance();

        subsystems = new Subsystem[] {dt, el, intake, Intake.pn};
    
        loc_chooser.addObject ("Left", RobotLocation.LEFT);
        loc_chooser.addObject ("Center", RobotLocation.CENTER);
        loc_chooser.addObject ("Right", RobotLocation.RIGHT);
    }
    

    /**
     * This function is called once each time the robot enters Disabled mode. You
     * can use it to reset any subsystem information you want to clear when the
     * robot is disabled.
     */
    public void disabledInit() 
    { 
        NeutralMode nm;
        if (Robot.dt.getLeftTalon().getMotorOutputPercent() > DrivetrainConstants.NOMINAL_OUTPUT_LEFT * 1.5
                && Robot.dt.getRightTalon().getMotorOutputPercent() > DrivetrainConstants.NOMINAL_OUTPUT_RIGHT * 1.5)
        {
            nm = NeutralMode.Brake;
        }
        else
            nm = NeutralMode.Coast;
        Robot.dt.getLeftTalon().setNeutralMode(nm);
        Robot.dt.getRightTalon().setNeutralMode(nm);
        Robot.dt.getLeftVictor().setNeutralMode(nm);
        Robot.dt.getRightVictor().setNeutralMode(nm);
    }

    /**
     * Called periodically while the robot is disabled.
     */
    public void disabledPeriodic() { Scheduler.getInstance().run(); }

    /**
     * Initializes the autonomous period of the robot.
     */
    public void autonomousInit()
    {
        startTime = Timer.getFPGATimestamp();

        m_autonomousCommand.start();
    }

    /**
     * This function is called periodically during autonomous.
     */
    public void autonomousPeriodic()
    { 
        String newData = DriverStation.getInstance().getGameSpecificMessage();
        if (newData.length() == 3 && (gameData.length() != 3 || !newData.equals(gameData))) {
        	gameData = newData;
        	if (m_autonomousCommand != null)
        		m_autonomousCommand.cancel();
        	(m_autonomousCommand = new AutonomousCommand (loc_chooser.getSelected(), subsystems, newData)).start();
        }
        Scheduler.getInstance().run();
    }

    /**
     * Calls necessary methods to initialize for the teleoperated period.
     */
    public void teleopInit()
    {
        // stops autonomous command when teleop begins
        if (m_autonomousCommand != null)
        {
            m_autonomousCommand.cancel();
        }
        
        dt.talonInitTeleop();
        el.talonInit();
        intake.talonInit();
        dt.zeroPigeon(); 
    }

    /**
     * This function is called periodically during operator control.
     */
    public void teleopPeriodic() 
    {
          Scheduler.getInstance().run();
    }
    
    /**
     * Gets the current time elapsed since the program started.
     * @return the total time elapsed
     */
    public static double getCurrentTimeMs()
    {
        return 1000 * (Timer.getFPGATimestamp() - startTime);
    }
}