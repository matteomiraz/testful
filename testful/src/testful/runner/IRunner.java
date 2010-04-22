package testful.runner;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface IRunner {

	public <T extends Serializable> Future<T> execute(Context<T, ? extends ExecutionManager<T>> ctx);

	public boolean addRemoteWorker(String rmiAddress);
}
