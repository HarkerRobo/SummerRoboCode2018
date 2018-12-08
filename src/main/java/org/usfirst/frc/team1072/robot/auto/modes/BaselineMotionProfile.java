package org.usfirst.frc.team1072.robot.auto.modes;

import org.usfirst.frc.team1072.robot.auto.paths.BaselinePath;
import org.usfirst.frc.team1072.robot.commands.auton.FollowPathRio;

import edu.wpi.first.wpilibj.command.Command;
import harkerrobolib.auto.AutoMode;

public class BaselineMotionProfile extends AutoMode {
    public BaselineMotionProfile (Location loc) {
        super(loc, loc);
    }

    @Override
	public Command getLeftCommands(Location endLoc) {
		return new FollowPathRio(new BaselinePath());
	}

	@Override
	public Command getRightCommands(Location endLoc) {
		return new FollowPathRio(new BaselinePath());
	}
}