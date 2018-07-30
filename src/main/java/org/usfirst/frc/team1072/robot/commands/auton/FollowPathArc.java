package org.usfirst.frc.team1072.robot.commands.auton;

import org.usfirst.frc.team1072.robot.RobotMap.DrivetrainConstants;

/**
 * Follows a motion profile in an arc-like manner.
 * @author Finn Frankis
 * @version 6/28/18
 */
public class FollowPathArc extends FollowPath
{
    /**
     * Constructs a new motion profile arc.
     */
    public FollowPathArc()
    {
        super(DrivetrainConstants.ANGLE_PID);
    }
}
