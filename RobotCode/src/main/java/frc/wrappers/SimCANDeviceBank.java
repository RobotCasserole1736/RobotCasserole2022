package frc.wrappers;

import java.util.HashMap;

/**
 * Implements a CAN device which can be accessed in simulation. Used for wrappering
 * devices which don't directly support WPIlib's HAL layer simulation style.
 * Enforces a unique CAN ID per device - this isn't actually required on a robot,
 * but is a best-practice we'll use on our team.
 */
public class SimCANDeviceBank {
    private static HashMap<Integer, SimCANDevice> bank = new HashMap<Integer, SimCANDevice>();

    public static void add(SimCANDevice newDevice){
        int id = newDevice.can_id;

        if(bank.containsKey(id)){
            throw new IllegalStateException("CAN ID " + id + " has already been allocated!");
        }

        bank.put(id, newDevice);
    }

    public static SimCANDevice get(int id){
        if(!bank.containsKey(id)){
            throw new IllegalStateException("CAN ID " + id + " is not a device on the CAN bus!");
        }

        return bank.get(id);
    }
}
