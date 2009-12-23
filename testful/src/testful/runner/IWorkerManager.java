package testful.runner;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IWorkerManager extends Remote {

	public void addTestRepository(ITestRepository rep) throws RemoteException;

	public void addTestRepository(String rep) throws RemoteException, MalformedURLException, NotBoundException;
}
