package org.usfirst.frc.team1072.robot.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.usfirst.frc.team1072.robot.Robot;
import org.usfirst.frc.team1072.robot.RobotMap;
import org.usfirst.frc.team1072.robot.RobotMap.AutonomousPaths;
import org.usfirst.frc.team1072.robot.RobotMap.DrivetrainConstants;
import org.usfirst.frc.team1072.robot.RobotMap.ElevatorConstants;
import org.usfirst.frc.team1072.robot.RobotMap.IntakeConstants;
import org.usfirst.frc.team1072.robot.commands.PauseUntilPathBeginsCommand.PauseType;
import org.usfirst.frc.team1072.robot.subsystems.Intake;
import org.usfirst.frc.team1072.util.Position;
import org.usfirst.frc.team1072.util.Position.PositionUnit;

import com.ctre.phoenix.motion.TrajectoryPoint;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.command.TimedCommand;
import edu.wpi.first.wpilibj.command.WaitCommand;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Trajectory.Segment;

/**
 * A set of commands to be called during the autonomous period.
 * @author Finn Frankis
 * @version 6/20/18
 */
public class AutonomousCommand extends CommandGroup
{
    private int numPoints = 0;
    
    
    
    //private final String CENTER_LEFT_HEAD_ON_ONE_CUBE_SLOW_LEFT_PART2 = "/home/summer2018/paths/center_left_headonTest(switch_1)/center_left_headonTest(switch_1)_left_detailed.csv";
    
    /**
     * Constructs a new command
     * @param subsystems the list of subsystems
     */
    public AutonomousCommand(Subsystem[] subsystems)
    {
        for (Subsystem s : subsystems)
            requires(s);
        System.out.println("ALL SUBSYSTEMS INITIALIZED " + 1000 * (Timer.getFPGATimestamp() - Robot.startTime));
        /*addSequential(setupPathFollower("/home/summer2018/paths/test_5ft/test_5ft_left_detailed.csv", 
                "/home/summer2018/paths/test_5ft/test_5ft_right_detailed.csv"));*/
        /*addSequential(setupPathFollower("/home/summer2018/paths/curved_path/curved_path_left_detailed.csv", 
                "/home/summer2018/paths/curved_path/curved_path_right_detailed.csv"));*/
        
        switchAuton(true);

    }

    /**
     * The command to be performed for a one-cube switch autonomous.
     * @param onLeft true if the switch is on the left; false otherwise
     */
    private void switchAuton (boolean onLeft)
    {
        // sequential commands are run first, take complete precedence over parallel
        //addParallel(new IntakeOuttakeTimedCommand(2, IntakeConstants.OUTTAKE_BOOL));
        
        //addSequential(new SetSolenoidCommand(IntakeConstants.UPDOWN_KEY, IntakeConstants.UP));
         
        FollowPathCommand fpc1, fpc2, fpc3, fpc4, fpc5, fpc6, fpc7, fpc8, fpc9;
        //DriveToPositionCommand fpc3;
        
        fpc1 = setupPathFollowerArc(AutonomousPaths.CLH_P1_LEFT, AutonomousPaths.CLH_P1_RIGHT, false)
                .zeroPigeonAtStart(true);
       
        CommandGroup initSubsystems = new CommandGroup();
            initSubsystems.addParallel(new InitializeDrivetrainCommand());
            initSubsystems.addParallel(new InitializeElevatorCommand());
            initSubsystems.addParallel(new InitializeIntakeCommand());
        //addSequential(initSubsystems);
        
        
        CommandGroup firstCube = new CommandGroup();
            firstCube.addParallel(initSubsystems);
            firstCube.addParallel(new SetSolenoidCommand(IntakeConstants.UPDOWN_KEY, IntakeConstants.UP));
            firstCube.addParallel(new SetSolenoidCommand(IntakeConstants.COMPRESSDECOMPRESS_KEY, IntakeConstants.COMPRESS));
            firstCube.addParallel(fpc1);
            CommandGroup raiseElevatorFirstCube = new CommandGroup();
                raiseElevatorFirstCube.addSequential(new PauseUntilPathBeginsCommand(fpc1, PauseType.END_OF_PATH, 0.9, fpc1.getTotalTime()));
                raiseElevatorFirstCube.addSequential(new MoveElevatorMotionMagicCommand(ElevatorConstants.SWITCH_HEIGHT_AUTON));
            firstCube.addParallel(raiseElevatorFirstCube);
            CommandGroup outtakeFirstCube = new CommandGroup();
                outtakeFirstCube.addSequential(new PauseUntilPathBeginsCommand(fpc1, PauseType.END_OF_PATH, 0.15, fpc1.getTotalTime()));
                outtakeFirstCube.addSequential(new SetSolenoidCommand(IntakeConstants.COMPRESSDECOMPRESS_KEY,
                        IntakeConstants.DECOMPRESS));
                outtakeFirstCube.addSequential(new IntakeOuttakeTimedCommand(0.15, RobotMap.IntakeConstants.OUTTAKE_BOOL));
            firstCube.addParallel(outtakeFirstCube);
        addSequential(firstCube);
        
            
        fpc2 = setupPathFollowerArc(AutonomousPaths.CLH_P2_LEFT_REV, AutonomousPaths.CLH_P2_RIGHT_REV, true)
                .zeroPigeonAtStart(false);
        fpc3 = setupPathFollowerArc(AutonomousPaths.CLH_P3_LEFT, AutonomousPaths.CLH_P3_RIGHT, false); /*new DriveToPositionCommand(
                new Position(PositionUnit.FEET, 3.85, 
                DrivetrainConstants.WHEELDIAMETER).getEncoderUnits(), 50);*/
        CommandGroup getSecondCube = new CommandGroup();
            CommandGroup pathGroupSecondCube = new CommandGroup();
                pathGroupSecondCube.addSequential(fpc2);
                pathGroupSecondCube.addSequential(fpc3);
            getSecondCube.addParallel(pathGroupSecondCube);
            CommandGroup elevatorLowerSecondCube = new CommandGroup();
                elevatorLowerSecondCube.addSequential(new PauseUntilPathBeginsCommand
                        (fpc2, PauseType.START_OF_PATH, 0.7, fpc2.getTotalTime()));
                elevatorLowerSecondCube.addSequential(new MoveElevatorMotionMagicCommand
                        (RobotMap.ElevatorConstants.INTAKE_HEIGHT));
                elevatorLowerSecondCube.addSequential(new SetSolenoidCommand(IntakeConstants.UPDOWN_KEY,
                        IntakeConstants.DOWN));
            getSecondCube.addParallel(elevatorLowerSecondCube);
            CommandGroup intakeSecondCube = new CommandGroup();
                getSecondCube.addSequential(
                        new PauseUntilPathBeginsCommand(fpc3, PauseType.END_OF_PATH, 0.5, fpc3.getTotalTime()));
                CommandGroup secondCubePneumatics = new CommandGroup();
                    secondCubePneumatics.addParallel(new SetSolenoidCommand(IntakeConstants.UPDOWN_KEY,
                            IntakeConstants.DOWN));
                intakeSecondCube.addSequential(secondCubePneumatics);
                getSecondCube.addSequential(new IntakeOuttakeTimedCommand(0.6, IntakeConstants.INTAKE_BOOL));
                getSecondCube.addSequential(new SetSolenoidCommand(IntakeConstants.COMPRESSDECOMPRESS_KEY,
                        IntakeConstants.COMPRESS));
           getSecondCube.addParallel(intakeSecondCube);
         addSequential(getSecondCube);
        
         
        fpc4 = setupPathFollowerArc(AutonomousPaths.CLH_P4_LEFT_REV, AutonomousPaths.CLH_P4_RIGHT_REV, true);
        fpc5 = setupPathFollowerArc(AutonomousPaths.CLH_P5_LEFT, AutonomousPaths.CLH_P5_RIGHT, false);
        CommandGroup scoreSecondCube = new CommandGroup();
            CommandGroup pathGroupOuttakeSecondCube = new CommandGroup();
                pathGroupOuttakeSecondCube.addSequential(fpc4);
                pathGroupOuttakeSecondCube.addSequential(fpc5);
            scoreSecondCube.addParallel(pathGroupOuttakeSecondCube);
            CommandGroup elevatorRaiseSecondCube = new CommandGroup();
                elevatorRaiseSecondCube.addSequential(new PauseUntilPathBeginsCommand(fpc5, PauseType.END_OF_PATH, 
                        0.9, fpc5.getTotalTime()));
                elevatorRaiseSecondCube.addSequential(new MoveElevatorMotionMagicCommand(ElevatorConstants.SWITCH_HEIGHT_AUTON));
            scoreSecondCube.addParallel(elevatorRaiseSecondCube);
            CommandGroup outtakeSecondCube = new CommandGroup();
                outtakeSecondCube.addSequential(
                        new PauseUntilPathBeginsCommand(fpc5, PauseType.END_OF_PATH, 0.8, fpc5.getTotalTime()));
        addSequential(scoreSecondCube);

        fpc6 = setupPathFollowerArc(AutonomousPaths.CLH_P6_LEFT_REV, AutonomousPaths.CLH_P6_RIGHT_REV, true);
        fpc7 = setupPathFollowerArc(AutonomousPaths.CLH_P7_LEFT, AutonomousPaths.CLH_P7_RIGHT, false);
        
        CommandGroup getThirdCube = new CommandGroup();
            CommandGroup pathGroupIntakeThirdCube = new CommandGroup();
                pathGroupIntakeThirdCube.addSequential(fpc6);
                pathGroupIntakeThirdCube.addSequential(fpc7);
            getThirdCube.addParallel(pathGroupIntakeThirdCube);
            CommandGroup lowerElevatorThirdCube = new CommandGroup();
                lowerElevatorThirdCube.addSequential(
                        new PauseUntilPathBeginsCommand(fpc6, PauseType.END_OF_PATH, 0.7, fpc7.getTotalTime()));
                lowerElevatorThirdCube.addSequential(new MoveElevatorMotionMagicCommand(ElevatorConstants.INTAKE_HEIGHT));
            getThirdCube.addParallel(lowerElevatorThirdCube);
            CommandGroup intakeThirdCube = new CommandGroup();
                intakeThirdCube.addParallel(
                        new PauseUntilPathBeginsCommand(fpc7, PauseType.END_OF_PATH, 0.7, fpc7.getTotalTime()));
                CommandGroup thirdCubePneumatics = new CommandGroup();
                thirdCubePneumatics.addParallel(new SetSolenoidCommand(IntakeConstants.UPDOWN_KEY,
                        IntakeConstants.DOWN));
                thirdCubePneumatics.addParallel(new SetSolenoidCommand(IntakeConstants.COMPRESSDECOMPRESS_KEY,
                        IntakeConstants.DECOMPRESS));
            intakeThirdCube.addSequential(thirdCubePneumatics);
            getThirdCube.addSequential(new IntakeOuttakeTimedCommand(0.6, IntakeConstants.INTAKE_BOOL));
            getThirdCube.addSequential(new SetSolenoidCommand(IntakeConstants.COMPRESSDECOMPRESS_KEY,
                    IntakeConstants.COMPRESS));
        //addSequential(getThirdCube);
        
        fpc8 = setupPathFollowerArc(AutonomousPaths.CLH_P8_LEFT_REV, AutonomousPaths.CLH_P8_RIGHT_REV, true);
        fpc9 = setupPathFollowerArc(AutonomousPaths.CLH_P9_LEFT, AutonomousPaths.CLH_P9_RIGHT, false);
        CommandGroup scoreThirdCube = new CommandGroup();
            CommandGroup pathGroupScoreThirdCube = new CommandGroup();
                pathGroupScoreThirdCube.addSequential(fpc8);
                pathGroupScoreThirdCube.addSequential(fpc9);
            scoreThirdCube.addParallel(pathGroupScoreThirdCube);
            CommandGroup raiseElevatorThirdCube = new CommandGroup();
                raiseElevatorThirdCube.addSequential(new PauseUntilPathBeginsCommand(fpc9, PauseType.END_OF_PATH, 0.9, fpc9.getTotalTime()));
                raiseElevatorThirdCube.addSequential(new MoveElevatorMotionMagicCommand(ElevatorConstants.SWITCH_HEIGHT_AUTON));
            scoreThirdCube.addParallel(raiseElevatorThirdCube);
                CommandGroup outtakeThirdCube = new CommandGroup();
                outtakeThirdCube.addSequential(new PauseUntilPathBeginsCommand(fpc1, PauseType.END_OF_PATH, 0.15, fpc1.getTotalTime()));
                outtakeThirdCube.addSequential(new SetSolenoidCommand(IntakeConstants.COMPRESSDECOMPRESS_KEY,
                        IntakeConstants.DECOMPRESS));
                outtakeThirdCube.addSequential(new IntakeOuttakeTimedCommand(0.15, RobotMap.IntakeConstants.OUTTAKE_BOOL));
        // addSequential(scoreThirdCube);
      
       
                           
        
        
        
        
    }
    
    /**
     * Reads in a trajectory (from Jaci's pathfinder) given the filename as a CSV.
     * @param filename the file name
     * @return the Trajectory
     * @throws FileNotFoundException
     */
    public static Trajectory readTrajectory(String filename) throws FileNotFoundException
    {
        File f = new File(filename);
        if (f.exists() && f.isFile() && filename.endsWith(".csv"))
        {
            try
            {
                return Pathfinder.readFromCSV(f);
            }
            catch (Exception e)
            {
                throw new FileNotFoundException("Pathfinder failed to read trajectory: " + filename);
            }
        }
        else
        {
            throw new FileNotFoundException("Trajectory: " + filename + ", does not exist or is not a csv file");
        }
    }

    @SuppressWarnings("unused")
    private FollowPathCommand setupPathFollower(String leftFileName, String rightFileName, boolean reverse)
    {
        FollowPathCommand fpc = new FollowPathCommand();

        Trajectory leftPath1 = null;
        Trajectory rightPath1 = null;

        try
        {
            leftPath1 = readTrajectory(leftFileName);
            rightPath1 = readTrajectory(rightFileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("FILE. NOT. FOUND.");
        }
        fpc.addProfile(leftPath1, Robot.dt.getLeftTalon(), reverse);
        fpc.addProfile(rightPath1, Robot.dt.getRightTalon(), reverse);
        numPoints = (leftPath1.segments.length + rightPath1.segments.length)/2;
        fpc.setTotalTime(numPoints * RobotMap.TIME_PER_TRAJECTORY_POINT_MS);
        return fpc;
    }
    
    /**
     * Sets up a motion profile arc path.
     * @param leftFileName the file name of the left path 
     * @param rightFileName the file name of the right path
     * @param reverse if true: perform the trajectory in reverse order; if false: perform it normally
     * @return the new follow path command
     */
    private FollowPathCommand setupPathFollowerArc(String leftFileName, String rightFileName, boolean reverse)
    {
        FollowPathArcCommand fpc = new FollowPathArcCommand();

        Trajectory leftPath1 = null;
        Trajectory rightPath1 = null;

        try
        {
            leftPath1 = readTrajectory(leftFileName);
            rightPath1 = readTrajectory(rightFileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("FILE. NOT. FOUND.");
        }
        fpc.addProfile(leftPath1, Robot.dt.getLeftTalon(), reverse);
        fpc.addProfile(rightPath1, Robot.dt.getRightTalon(), reverse);

        numPoints = (leftPath1.segments.length + rightPath1.segments.length)/2;
        fpc.setTotalTime(numPoints * RobotMap.TIME_PER_TRAJECTORY_POINT_MS);
        return fpc;
    }
}
