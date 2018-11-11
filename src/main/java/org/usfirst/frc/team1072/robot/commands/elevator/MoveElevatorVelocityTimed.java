package org.usfirst.frc.team1072.robot.commands.elevator;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import edu.wpi.first.wpilibj.command.TimedCommand;
import org.usfirst.frc.team1072.robot.subsystems.Elevator;

public class MoveElevatorVelocityTimed extends TimedCommand {

    private double output;
    /**
     * Moves the elevator for a certain amount of time.
     * @param time      amount of time the elevator will move
     * @param output    if positive, up; otherwise, down [-1, 1]
     */
    public MoveElevatorVelocityTimed(double time, double output) {
        super(time);
        this.output = output;
    }


    @Override
    public void execute() {
        Elevator.getInstance().moveElevatorVelocity(output);
    }
}
