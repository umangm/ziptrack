package print.engine;

import print.parse.rr.ParseRoadRunner;
import print.parse.rv.parserv.ParseRVPredict;
import print.parse.std.ParseStandard;
import print.parse.ParserType;
import print.event.Event;

public abstract class Engine<E extends Event> {
	protected ParserType parserType;
	protected ParseRVPredict rvParser;//RV
	protected ParseStandard stdParser; //STD
	protected ParseRoadRunner rrParser; //RR
	protected E handlerEvent;

	public Engine(ParserType pType){
		this.parserType = pType;
	}

	protected void initializeReader(String trace_folder) {
		if(this.parserType.isRV()){
			initializeReaderRV(trace_folder);
		}
		else if(this.parserType.isSTD()){
			initializeReaderSTD(trace_folder);
		}
		else if(this.parserType.isRR()){
			initializeReaderRR(trace_folder);
		}
	}

	protected void initializeReaderRV(String trace_folder){
		rvParser = new ParseRVPredict(trace_folder, null);
	}

	protected void initializeReaderSTD(String trace_file) {
		stdParser = new ParseStandard(trace_file);
	}
	
	protected void initializeReaderRR(String trace_file) {
		rrParser = new ParseRoadRunner(trace_file);
	}
}
