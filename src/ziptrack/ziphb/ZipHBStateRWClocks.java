package ziptrack.ziphb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ziptrack.util.VectorClock;

public class ZipHBStateRWClocks extends ZipHBState {


	private HashMap<Integer, Integer> variableToIndex;
	private int numVariables;

	// Data used for algorithm
	public ArrayList<VectorClock> readVariable;
	public ArrayList<VectorClock> writeVariable;

	public ZipHBStateRWClocks(HashSet<Integer> tSet) {
		super(tSet);
	}

	@Override
	protected void initInternalData(HashSet<Integer> tSet) {
		super.initInternalData(tSet);
		this.variableToIndex = new HashMap<Integer, Integer>();
		this.numVariables = 0;
	}

	@Override
	public void initData(HashSet<Integer> tSet) {
		super.initData(tSet);

		// initialize readVariable
		this.readVariable = new ArrayList<VectorClock>();

		// initialize writeVariable
		this.writeVariable = new ArrayList<VectorClock>();
	}

	private int checkAndAddVariable(int v){
		if(!variableToIndex.containsKey(v)){
			variableToIndex.put(v, this.numVariables);
			this.numVariables ++;
			readVariable	.add(new VectorClock(this.numThreads));
			writeVariable	.add(new VectorClock(this.numThreads));
		}
		return variableToIndex.get(v);
	}

	public VectorClock getVectorClock_Variable(ArrayList<VectorClock> arr, int v) {
		int vIndex = checkAndAddVariable(v);
		return getVectorClockFrom1DArray(arr, vIndex);
	}

	@Override
	public boolean HandleSubRead(int thread, int var) {
		boolean raceDetected = false;
		VectorClock C_t = generateVectorClockFromClockThread(thread);
		VectorClock R_v = getVectorClock_Variable(readVariable, var);
		VectorClock W_v = getVectorClock_Variable(writeVariable, var);
		
		checkAndPutFirstVC(thread, C_t);
		
		checkAndPutVC_t_R(thread, var, C_t);
		
		if (!(W_v.isLessThanOrEqual(C_t))) {
			raceDetected = true;
		}
		R_v.updateWithMax(R_v, C_t);

		return raceDetected;
	}

	@Override
	public boolean HandleSubWrite(int thread, int var) {
		boolean raceDetected = false;
		VectorClock C_t = generateVectorClockFromClockThread(thread);
		VectorClock R_v = getVectorClock_Variable(readVariable, var);
		VectorClock W_v = getVectorClock_Variable(writeVariable, var);
		
		checkAndPutFirstVC(thread, C_t);
		
		checkAndPutVC_W(var, C_t);

		if (!(R_v.isLessThanOrEqual(C_t))) {
			raceDetected = true;
		}
		if (!(W_v.isLessThanOrEqual(C_t))) {
			raceDetected = true;
		}

		W_v.updateWithMax(W_v, C_t);
		return raceDetected;
	}
}