package testful.runner;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Allows one to retrieve the bytecode of a class.
 * 
 * @author matteo
 */
public interface ClassFinder extends Remote {

	public byte[] getClass(String name) throws ClassNotFoundException, RemoteException;

	public String getKey() throws RemoteException;
}
