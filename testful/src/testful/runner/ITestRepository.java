package testful.runner;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ITestRepository extends Remote {

	public String getName() throws RemoteException;
	
	public Context<?, ?> getTest() throws RemoteException;

	public void putResult(String key, byte[] result) throws RemoteException;

	public void putException(String key, byte[] exception) throws RemoteException;
}
