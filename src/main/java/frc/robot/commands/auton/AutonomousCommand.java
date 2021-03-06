package frc.robot.commands.auton;

import java.io.File;
import java.io.FileNotFoundException;

import edu.wpi.first.wpilibj.command.Command;
import harkerrobolib.auto.SequentialCommandGroup;
import harkerrobolib.auto.AutoMode.Location;

import frc.robot.Robot;
import frc.robot.RobotMap;
import frc.robot.RobotMap.AutonomousConstants;
import frc.robot.auto.modes.BaselineMotionProfile;
import frc.robot.auto.paths.BaselinePath;
import frc.robot.commands.auton.PauseUntilPathBegins.PauseType;
import frc.robot.commands.drivetrain.*;
import frc.robot.commands.elevator.InitializeElevator;
import frc.robot.commands.elevator.MoveElevatorMotionMagic;
import frc.robot.commands.elevator.MoveElevatorVelocityTimed;
import frc.robot.commands.elevator.ZeroElevator;
import frc.robot.commands.intake.InitializeIntake;
import frc.robot.commands.intake.IntakeOuttakeTimed;
import frc.robot.commands.intake.SetSolenoid;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Elevator;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Intake.IntakeType;
import frc.robot.subsystems.Pneumatics;
import frc.robot.subsystems.Pneumatics.SolenoidDirection;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.Subsystem;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;

/**
 * A set of commands to be called during the autonomous period.
 * @author Finn Frankis
 * @version 6/20/18
 */
public class AutonomousCommand extends CommandGroup
{    
    public enum AutonType {
        BASELINE, CENTER_SWITCH, ONE_CUBE_CENTER, ONE_CUBE_SIDE, LEFT_SWITCH, RIGHT_SWITCH;
    }
    

    /**
     * Constructs a new command
     * @param subsystems the list of subsystems
     */
    public AutonomousCommand(Location location, Subsystem[] subsystems, String fieldData)
    {
        for (Subsystem s : subsystems)
            requires(s);
        System.out.println("ADDING BASELINE MOT PROF");
        addSequential(new FollowPathRio(Robot.baseline));
        //initSubsystems();
//        addSequential(new ZeroElevator());
//        addSequential(new DriveWithVelocityTimed(0.8, 1.8));

//        addSequential(new TurnToAngleTimed(0.22, Drivetrain.TurnDirection.LEFT));
//        FollowPathRio.setDefaultLeftTalon(Robot.dt.getLeftMaster());
//        FollowPathRio.setDefaultRightTalon(Robot.dt.getRightMaster());
        /*addSequential(new SequentialCommandGroup(new ZeroElevator(),
                new MoveElevatorMotionMagic(Elevator.SWITCH_HEIGHT_AUTON),
                    new DriveWithVelocityTimed(0.4, 5.75)
                                              ));
        if (location == RobotLocation.RIGHT && fieldData.substring(0, 1).equals("R")) {
            addSequential(new SequentialCommandGroup(new SetSolenoid(Pneumatics.SolenoidDirection.DECOMPRESS),
                    new IntakeOuttakeTimed(3.0, IntakeType.INTAKE, 0.5)));
        }
        else if (location == RobotLocation.LEFT && fieldData.substring(0, 1).equals("L")) {
            addSequential(new SequentialCommandGroup(new SetSolenoid(Pneumatics.SolenoidDirection.DECOMPRESS),
                    new IntakeOuttakeTimed(3.0, IntakeType.INTAKE, 0.5)));
        }*/

        /*if (location == RobotLocation.LEFT) {
            if (fieldData.equals("LLL"))
                sideScale(  );
            else if (fieldData.equals("RLR")) 
                sideScale(ON_LEFT);
            else if (fieldData.equals("RRR"))
                baseline();
            else if (fieldData.equals("LRL"))
                oneCubeSide(ON_LEFT);
        }
        else if (location == RobotLocation.CENTER) {
            if (fieldData.equals("LLL"))
                switchAuton(ON_LEFT);
            else if (fieldData.equals("RLR")) 
                oneCubeCenter(ON_RIGHT);
            else if (fieldData.equals("RRR"))
                oneCubeCenter(ON_RIGHT);
            else if (fieldData.equals("LRL"))
                switchAuton(ON_LEFT);
        }
        else if (location == RobotLocation.RIGHT) {
            if (fieldData.equals("LLL"))
                baseline();
            else if (fieldData.equals("RLR")) 
                oneCubeSide(ON_RIGHT);
            else if (fieldData.equals("RRR"))
                sideScale(ON_RIGHT);
            else if (fieldData.equals("LRL"))
                sideScale(ON_RIGHT);
        }*/
        //sideScale(ON_LEFT);
        //baseline();
        //sideScaleSneaky(ON_LEFT);
        //switchAuton(ON_LEFT);
        //oneCubeSide(ON_LEFT);
    }

    
    /**
     * Initializes subsystems in parallel.
     */
    private void initSubsystems()
    {
        CommandGroup initSubsystems = new CommandGroup();
            initSubsystems.addParallel(new InitializeDrivetrain());
            initSubsystems.addParallel(new InitializeElevator());
            initSubsystems.addParallel(new InitializeIntake());
        addSequential(initSubsystems);
    }
    
    private void sideScaleSneaky (boolean onLeft) {
        if (onLeft) {
            addSequential (setupPathFollowerArc(AutonomousConstants.LEFT_SCALE_SNEAKY_LEFT, AutonomousConstants.LEFT_SCALE_SNEAKY_RIGHT, 
            false, null).zeroPigeonAtStart(true).resetSensors(true));
            addSequential (new MoveElevatorMotionMagic(Elevator.SCALE_HIGH_HEIGHT));
            addSequential (new DriveToPosition(2.75));
            addSequential(new SetSolenoid (SolenoidDirection.DECOMPRESS));
            addSequential (new IntakeOuttakeTimed(AutonomousConstants.SCALE_OUTTAKE_TIME, IntakeType.OUTTAKE));
            addSequential(new Delay(1));
            addSequential (new DriveToPosition(-1.5));
        }
    }
    private void sideScale (boolean onLeft) {
        addSequential (new DriveToPosition(AutonomousConstants.SCALE_DISTANCE_FEET));
        addSequential(new TurnToAngle((onLeft ? 1 : -1) * 90));
        addSequential (new MoveElevatorMotionMagic(Elevator.SCALE_HIGH_HEIGHT));
        addSequential(new SetSolenoid (SolenoidDirection.DECOMPRESS));
        addSequential (new IntakeOuttakeTimed(AutonomousConstants.SCALE_OUTTAKE_TIME, IntakeType.OUTTAKE));
    }

    private void baseline () {
        addSequential (new DriveToPosition(AutonomousConstants.BASELINE_DISTANCE));
    }

    private void oneCubeSide (boolean onLeft) {
        PositionCommand driveToPosition = new DriveToPosition (AutonomousConstants.BASELINE_DISTANCE * 0.75);
            addSequential(driveToPosition);
            addSequential (new TurnToAngle ((onLeft ? -1 : 1) * 90));
            addSequential(new MoveElevatorMotionMagic(Elevator.SWITCH_HEIGHT_AUTON));
            addSequential(new SetSolenoid(SolenoidDirection.DECOMPRESS));
            addSequential(new IntakeOuttakeTimed(0.17, IntakeType.OUTTAKE));
    }
    private void oneCubeCenter (boolean onLeft) {
        FollowPath fpc1 = setupPathFollowerArc(AutonomousConstants.CLH_P1_LEFT, AutonomousConstants.CLH_P1_RIGHT, 
        false, null).zeroPigeonAtStart(false).resetSensors(true);
        CommandGroup firstCube = new CommandGroup();
            firstCube.addParallel(fpc1);
            CommandGroup raiseElevatorFirstCube = new CommandGroup();
                raiseElevatorFirstCube.addSequential(new PauseUntilPathBegins(fpc1, PauseType.END_OF_PATH, 1.9, fpc1.getTotalTime()));
                raiseElevatorFirstCube.addSequential(new MoveElevatorMotionMagic(Elevator.SWITCH_HEIGHT_AUTON));
            firstCube.addParallel(raiseElevatorFirstCube);
            CommandGroup outtakeFirstCube = new CommandGroup();
                outtakeFirstCube.addSequential(new PauseUntilPathBegins(fpc1, PauseType.END_OF_PATH, 0.15, fpc1.getTotalTime()));
                outtakeFirstCube.addSequential(new SetSolenoid(SolenoidDirection.DECOMPRESS));
                outtakeFirstCube.addSequential(new IntakeOuttakeTimed(0.17, IntakeType.OUTTAKE));
            firstCube.addParallel(outtakeFirstCube);
        addSequential(firstCube);
    }
    /**
     * The command to be performed for a switch autonomous.
     * @param onLeft true if the switch is on the left; false otherwise
     */
    private void switchAuton (boolean onLeft)
    {
        FollowPath fpc1, fpc2, fpc5, fpc6, fpc7, fpc8, fpc9;
        CombinedPositionAnglePID fpc3, fpc4;
        //DriveToPositionCommand fpc3;
        fpc1 = setupPathFollowerArc(AutonomousConstants.CLH_P1_LEFT, AutonomousConstants.CLH_P1_RIGHT, 
                false, null).zeroPigeonAtStart(false).resetSensors(true);
        fpc2 = setupPathFollowerArc(AutonomousConstants.CLH_P2_LEFT_REV, AutonomousConstants.CLH_P2_RIGHT_REV, true, 
                fpc1).zeroPigeonAtStart(false).resetSensors(false);
        fpc3 = new CombinedPositionAnglePID(2.95, 0);
        fpc4 = new CombinedPositionAnglePID(-3.25, 0);
        fpc5 = setupPathFollowerArc
                (AutonomousConstants.CLH_P5_LEFT, AutonomousConstants.CLH_P5_RIGHT, false, fpc2)
                .zeroPigeonAtStart(false).resetSensors(true);
        //addSequential (new PrintTimeToConsole());
        CommandGroup firstCube = new CommandGroup();
            CommandGroup firstPath = new CommandGroup();
                firstPath.addSequential(new PrebufferPathPoints(fpc1));
                CommandGroup preBufferAndPaths = new CommandGroup();
                    preBufferAndPaths.addParallel(fpc1);
                    CommandGroup preBuffer = new CommandGroup();
                        preBuffer.addSequential(new PrebufferPathPoints(fpc2));
                        preBuffer.addSequential(new PrebufferPathPoints(fpc5));
                    preBufferAndPaths.addParallel(preBuffer);
                firstPath.addSequential(preBufferAndPaths);
            firstCube.addParallel(firstPath);
            CommandGroup raiseElevatorFirstCube = new CommandGroup();
                raiseElevatorFirstCube.addSequential(new PauseUntilPathBegins(fpc1, PauseType.END_OF_PATH, 1.9, fpc1.getTotalTime()));
                raiseElevatorFirstCube.addSequential(new MoveElevatorMotionMagic(Elevator.SWITCH_HEIGHT_AUTON));
            firstCube.addParallel(raiseElevatorFirstCube);
            CommandGroup outtakeFirstCube = new CommandGroup();
                outtakeFirstCube.addSequential(new PauseUntilPathBegins(fpc1, PauseType.END_OF_PATH, 0.15, fpc1.getTotalTime()));
                outtakeFirstCube.addSequential(new SetSolenoid(SolenoidDirection.DECOMPRESS));
                outtakeFirstCube.addSequential(new IntakeOuttakeTimed(0.17, IntakeType.OUTTAKE));
            firstCube.addParallel(outtakeFirstCube);
        addSequential(firstCube);
        
        CommandGroup getSecondCube = new CommandGroup();
            CommandGroup pathGroupSecondCube = new CommandGroup();
                pathGroupSecondCube.addSequential(fpc2);
                CommandGroup startPath3LowerIntake = new CommandGroup();
                    startPath3LowerIntake.addParallel(fpc3);
                    startPath3LowerIntake.addParallel(new SetSolenoid(SolenoidDirection.DOWN));
                pathGroupSecondCube.addSequential(startPath3LowerIntake);
            getSecondCube.addParallel(pathGroupSecondCube);
            CommandGroup lowerElevatorSecondCube = new CommandGroup();
                lowerElevatorSecondCube.addSequential(
                        new PauseUntilPathBegins(fpc2, PauseType.START_OF_PATH, 0.5, fpc2.getTotalTime()));          
                lowerElevatorSecondCube.addSequential(new MoveElevatorMotionMagic
                        (Elevator.INTAKE_HEIGHT));
            getSecondCube.addParallel(lowerElevatorSecondCube);
            CommandGroup intakeSecondCube = new CommandGroup();
                intakeSecondCube.addSequential(
                        new PauseUntilReachingPosition(fpc3, 0.45));
                intakeSecondCube.addSequential(new IntakeOuttakeTimed(0.4, IntakeType.INTAKE));
           getSecondCube.addParallel(intakeSecondCube);
         addSequential(getSecondCube);

        CommandGroup scoreSecondCube = new CommandGroup();
            CommandGroup pathGroupOuttakeSecondCube = new CommandGroup();
                pathGroupOuttakeSecondCube.addSequential(fpc4);
                pathGroupOuttakeSecondCube.addSequential(fpc5);
            scoreSecondCube.addParallel(pathGroupOuttakeSecondCube);
            CommandGroup intakeSecondCubeDuringPath = new CommandGroup();
                intakeSecondCubeDuringPath.addSequential(new IntakeOuttakeTimed(0.4, IntakeType.INTAKE));
                intakeSecondCubeDuringPath.addSequential(new SetSolenoid(SolenoidDirection.COMPRESS));
            scoreSecondCube.addParallel(intakeSecondCubeDuringPath);
            CommandGroup outtakeSecondCube = new CommandGroup();
                outtakeSecondCube.addSequential(new PauseUntilPathBegins(fpc5, PauseType.END_OF_PATH, 
                        2, fpc5.getTotalTime()));
                outtakeSecondCube.addSequential(new SetSolenoid(SolenoidDirection.COMPRESS));
                outtakeSecondCube.addSequential(new SetSolenoid(SolenoidDirection.UP));
                outtakeSecondCube.addSequential(new MoveElevatorMotionMagic
                        (Elevator.SWITCH_HEIGHT_AUTON));
                outtakeSecondCube.addSequential(new SetSolenoid(SolenoidDirection.DECOMPRESS));
                outtakeSecondCube.addSequential(new IntakeOuttakeTimed(0.34, IntakeType.OUTTAKE));
            scoreSecondCube.addParallel(outtakeSecondCube);
        addSequential(scoreSecondCube); 
        //getThirdCube();
    }
    
    /**
     * Method to intake and score the third cube for a center switch autonomous.
     */
    private void getThirdCube()
    {
        PositionCommand fpc6 = new CombinedPositionAnglePID(-3.9, 0).setAllowableError(300), 
                fpc7 = new CombinedPositionAnglePID(5, -45).setAllowableError(300),
                fpc8 = new DriveToPosition(-1.5).setAllowableError(650), 
                fpc9 = new DriveToPosition(2.3).setAllowableError(1000);
        TurnToAngle turn6 = new TurnToAngle(-45, 0.8), turn8 = new TurnToAngle(20, 1.25);
        CommandGroup thirdCube = new CommandGroup();
            CommandGroup thirdCubePaths = new CommandGroup();
                thirdCubePaths.addSequential(fpc6);
                thirdCubePaths.addSequential(turn6);
                thirdCubePaths.addSequential(fpc7);
                thirdCubePaths.addSequential(new Delay(.5));
                thirdCubePaths.addSequential(fpc8);
                thirdCubePaths.addSequential(turn8);
                thirdCubePaths.addSequential(fpc9);
            thirdCube.addParallel(thirdCubePaths);
            CommandGroup raiseElevatorThirdCube = new CommandGroup();
                raiseElevatorThirdCube.addSequential(new SetSolenoid(SolenoidDirection.DOWN));
                raiseElevatorThirdCube.addSequential(new MoveElevatorMotionMagic(Elevator.INTAKE_HEIGHT));
                raiseElevatorThirdCube.addSequential(new PauseUntilReachingPosition(fpc7, 0.2));
                raiseElevatorThirdCube.addSequential(new IntakeOuttakeTimed(1.1, IntakeType.INTAKE));
                
                raiseElevatorThirdCube.addSequential(new PauseUntilReachingPosition(fpc8, .7));
                
                raiseElevatorThirdCube.addSequential(new SetSolenoid(SolenoidDirection.COMPRESS));
                raiseElevatorThirdCube.addSequential(new SetSolenoid(SolenoidDirection.UP));
                //raiseElevatorThirdCube.addSequential(new SetSolenoid(IntakeConstants.COMPRESSDECOMPRESS_KEY, IntakeConstants.COMPRESS));
                raiseElevatorThirdCube.addSequential(new SetSolenoid(SolenoidDirection.DECOMPRESS));
                CommandGroup raiseElevatorOuttake = new CommandGroup();
                    raiseElevatorOuttake.addParallel(new MoveElevatorMotionMagic
                            (Elevator.SWITCH_HEIGHT_THIRD_CUBE));
                    raiseElevatorOuttake.addParallel(new IntakeOuttakeTimed(.7, IntakeType.OUTTAKE));
                raiseElevatorThirdCube.addSequential(raiseElevatorOuttake);
            thirdCube.addParallel(raiseElevatorThirdCube);
           addSequential(thirdCube);
    }
    
    /**
     * Reads in a trajectory (from Jaci's pathfinder) given the filename as a CSV.
     * @param filename the file name
     * @return the Trajectory
     * @throws FileNotFoundException if the file is not successfully found
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
    
    /**
     * Sets up a motion profile arc path.
     * @param leftFileName the file name of the left path 
     * @param rightFileName the file name of the right path
     * @param reverse if true: perform the trajectory in reverse order; if false: perform it normally
     * @param prevPath the path which comes before this one (in a series of multiple paths)
     * @return the new follow path command
     */
    public static FollowPath setupPathFollowerArc(String leftFileName, String rightFileName, boolean reverse, FollowPath prevPath)
    {
        double endAngleLeft;
        double endAngleRight;
        if (prevPath == null)
        {
            endAngleLeft = 0;
            endAngleRight = 0;
        }
        else
        {
            endAngleLeft = prevPath.getControllerTrajectory
                    (Robot.dt.getLeftMaster()).segments[prevPath.getControllerTrajectory
                                                       (Robot.dt.getLeftMaster()).segments.length-1].heading;
            endAngleRight = prevPath.getControllerTrajectory
                    (Robot.dt.getRightMaster()).segments[prevPath.getControllerTrajectory
                                                       (Robot.dt.getLeftMaster()).segments.length-1].heading;                                                      
        }
        
        FollowPathArc fpc = new FollowPathArc();

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
            ;
        }
        fpc.addProfile(leftPath1, Robot.dt.getLeftMaster(), reverse, endAngleLeft);
        fpc.addProfile(rightPath1, Robot.dt.getRightMaster(), reverse, endAngleRight);

        int numPoints = (leftPath1.segments.length + rightPath1.segments.length)/2;
        fpc.setTotalTime(numPoints * RobotMap.TIME_PER_TRAJECTORY_POINT_MS);
        return fpc;
    }
}
