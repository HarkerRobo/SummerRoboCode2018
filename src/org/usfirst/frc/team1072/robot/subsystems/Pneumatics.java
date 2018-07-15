package org.usfirst.frc.team1072.robot.subsystems;

import java.util.HashMap;
import java.util.Map;

import org.usfirst.frc.team1072.robot.RobotMap.IntakeConstants;
import org.usfirst.frc.team1072.robot.commands.intake.SetCompressorCommand;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * Represents a pneumatics subsystem with full control over the pneumatic systems.
 * @author Finn Frankis
 * @version 6/14/18
 */
public class Pneumatics extends Subsystem
{
    private Compressor c;
    private static Pneumatics pn;
    private Map<String, DoubleSolenoid> solMap;
    private DoubleSolenoid intake_updown;
    private DoubleSolenoid intake_compressdecompress;
    
    public Pneumatics ()
    {
        c = new Compressor(IntakeConstants.COMPRESSOR_PORT);
        // stores all the solenoids by a String key representing their purpose (in RobotMap)
        solMap = new HashMap<String, DoubleSolenoid>();
        intake_updown = new DoubleSolenoid(/*RobotMap.FIRST_PCM_ID, */
                IntakeConstants.INTAKE_UP_SOL, IntakeConstants.INTAKE_DOWN_SOL);
        intake_compressdecompress = new DoubleSolenoid (/*RobotMap.FIRST_PCM_ID,*/
                IntakeConstants.INTAKE_COMPRESS_SOL, IntakeConstants.INTAKE_DECOMPRESS_SOL); 
        solMap.put(IntakeConstants.UPDOWN_KEY, intake_updown);
        solMap.put(IntakeConstants.COMPRESSDECOMPRESS_KEY, intake_compressdecompress);
    }
    /**
     * Initializes the command, setting up all the objects and the map of solenoids.
     */
    protected void initDefaultCommand()
    {
        setDefaultCommand(new SetCompressorCommand(true));
    }
    
    /**
     * Gets a solenoid given the key in the map.
     * @param key the key in the map
     * @return the solenoid stored by the given key
     */
    public DoubleSolenoid getSolenoid(String key)
    {
        return solMap.get(key);
    }
    
    /**
     * Sets the compressor given a state.
     * @param state the state to which the compressor will be set
     */
    public void setCompressor(boolean state)
    {
        c.setClosedLoopControl(state);
    }
    
    /**
     * Gets the compressor.
     * @return the compressor
     */
    public Compressor getCompressor()
    {
        return c;
    }

    /**
     * Gets the instance of this Pneumatics, creating a new one if necessary,
     * @return the instance of Pneumatics
     */
    public static Pneumatics getInstance()
    {
        if (pn == null)
            pn = new Pneumatics();
        return pn;
    }
}
