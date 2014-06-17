package cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;

public interface IComputeCallback extends Remote{	
	
	/**
	 * Notify via message string.
	 * @param message
	 * @throws RemoteException
	 */
	public void progressNotify(String message) throws RemoteException;
	
	/**
	 * Set a predefined result table.
	 * @param table
	 * @param jobtype
	 * @throws RemoteException
	 */
	public void setResultTable(Hashtable<Integer, Double> table, int jobtype) throws RemoteException;
	
	/**
	 * Set a predefined result set.
	 * @param set
	 * @param jobtype
	 * @throws RemoteException
	 */
	public void setResultSet(HashSet<HashSet<Integer>> set, int jobtype) throws RemoteException;
	
	/**
	 * Set a modified matrix.
	 * @param matrix
	 * @throws RemoteException
	 */
	public void setResultMatrix(int[][] matrix) throws RemoteException;
}