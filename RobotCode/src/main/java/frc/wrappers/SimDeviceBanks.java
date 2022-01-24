package frc.wrappers;

import java.util.HashMap;

/**
 * Implements a CAN device which can be accessed in simulation. Used for wrappering
 * devices which don't directly support WPIlib's HAL layer simulation style.
 * Enforces a unique CAN ID per device - this isn't actually required on a robot,
 * but is a best-practice we'll use on our team.
 */
public class SimDeviceBanks {

    // CAN DEVICES
    private static HashMap<Integer, Object> CANBank = new HashMap<Integer, Object>();

    public static void addCANDevice(Object newDevice, int id){

        if(CANBank.containsKey(id)){
            throw new IllegalStateException("CAN ID " + id + " has already been allocated!");
        }

        CANBank.put(id, newDevice);
    }

    public static Object getCANDevice(int id){
        if(!CANBank.containsKey(id)){
            throw new IllegalStateException("CAN ID " + id + " is not a device on the CAN bus!");
        }

        return CANBank.get(id);
    }

    //Digital Input Devices (Encoders)
    private static HashMap<Integer, Object> DIBank = new HashMap<Integer, Object>();

    public static void addDIDevice(Object newDevice, int port){

        if(DIBank.containsKey(port)){
            throw new IllegalStateException("Digital Input Port " + port + " has already been allocated!");
        }

        DIBank.put(port, newDevice);
    }

    public static Object getDIDevice(int port){
        if(!DIBank.containsKey(port)){
            throw new IllegalStateException("Digital Input Port " + port + " is not a connected device!");
        }

        return DIBank.get(port);
    }


    //SPI Devices (Gyros, etc.)
    //Digital Input Devices (Encoders)
    private static HashMap<Integer, Object> SPIBank = new HashMap<Integer, Object>();

    public static void addSPIDevice(Object newDevice, int cs){

        if(SPIBank.containsKey(cs)){
            throw new IllegalStateException("SPI cs " + cs + " has already been allocated!");
        }

        SPIBank.put(cs, newDevice);
    }

    public static Object getSPIDevice(int cs){
        if(!SPIBank.containsKey(cs)){
            throw new IllegalStateException("SPI cs " + cs + " is not a connected device!");
        }

        return SPIBank.get(cs);
    }

    public static void clearAllBanks(){
        DIBank.clear();
        SPIBank.clear();
        CANBank.clear();
    }

}
