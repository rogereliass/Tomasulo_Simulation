package Tomasulo;

import java.util.ArrayList;
import java.util.Collections;

public class Processor {
	public Program program;
	public int cycle;
	public int pc;
	ReservationStation addStation;
	ReservationStation mulStation;
	ReservationStation integerStation; // For integer instructions
	LoadBuffers lb;
	StoreBuffers sb;
	RegisterFile rf;
	Bus bus;
	public Memory memory;
	Cache cache;
	String issueSummary = "issue: \n";
	String executeSummary = "execute: \n";
	String publishSummary = "publish: \n";
	String busSummary = "bus: \n";

	int addlat = 2;
	int mullat = 2;
	int sublat = 2;
	int divlat = 2;
	int ldlat = 2;
	int stlat = 2;
	int addilat = 1;
	int subilat = 1;
	int daddilat = 1;
	int dsubilat = 1;
	int branchlat = 1;

	// Branch state
	boolean branchPending = false;
	int branchTarget = -1;
	boolean branchTaken = false;
	int branchInstIndex = -1;

	public Processor(Program program) {
		this.program = program;
		bus = new Bus();
		cycle = 1;
		pc = 0;
		addStation = new ReservationStation(3, StationType.ADD, 3);
		mulStation = new ReservationStation(2, StationType.MUL, 3);
		// integer station default initialization
		integerStation = new ReservationStation(3, StationType.INTEGER, 1);
		sb = new StoreBuffers(3, 2); // We are assuming CACHE takes 2 cycles
		lb = new LoadBuffers(3, 2);
		rf = new RegisterFile();
		memory = new Memory(2048);
	}

	public Processor(Program program, int addsub, int muldiv, int ld, int str) {
		this.program = program;
		bus = new Bus();
		cycle = 1;
		pc = 0;
		addStation = new ReservationStation(3, StationType.ADD, addsub);
		mulStation = new ReservationStation(2, StationType.MUL, muldiv);
		// integer station default initialization
		integerStation = new ReservationStation(3, StationType.INTEGER, 1);
		sb = new StoreBuffers(3, str); // We are assuming CACHE takes 2 cycles
		lb = new LoadBuffers(3, ld);
		rf = new RegisterFile();
		memory = new Memory(2048);
		memory.arr[0]=1;
		memory.arr[8]=2;
		System.out.println("--------------------"+memory.arr[0]+"---------------------");
		System.out.println("--------------------"+memory.arr[8]+"---------------------");
	}

	public Processor(Program program, int addlat, int mullat, int sublat, int divlat, int ldlat, int stlat) {
		this(program, addlat, mullat, sublat, divlat, ldlat, stlat,
				3, 2, 3, 3, 3, // station sizes: add, mul, integer, load, store
				1024, 32, 1, 10, // cache: size, block size, hit latency, miss penalty
				2048); // memory size
		
	}

	// Full constructor with all configuration options
	public Processor(Program program, int addlat, int mullat, int sublat, int divlat, int ldlat, int stlat,
			int addStationSize, int mulStationSize, int integerStationSize, int loadBufferSize, int storeBufferSize,
			int cacheSize, int blockSize, int cacheHitLatency, int cacheMissPenalty, int memorySize) {
		this.program = program;
		bus = new Bus();
		cycle = 1;
		pc = 0;
		addStation = new ReservationStation(addStationSize, StationType.ADD, addlat);
		mulStation = new ReservationStation(mulStationSize, StationType.MUL, mullat);
		integerStation = new ReservationStation(integerStationSize, StationType.INTEGER, 1);
		sb = new StoreBuffers(storeBufferSize, stlat);
		lb = new LoadBuffers(loadBufferSize, ldlat);
		rf = new RegisterFile();
		memory = new Memory(memorySize);
		cache = new Cache(cacheSize, blockSize, cacheHitLatency, cacheMissPenalty, memory);

		this.addlat = addlat;
		this.mullat = mullat;
		this.sublat = sublat;
		this.divlat = divlat;
		this.ldlat = ldlat;
		this.stlat = stlat;
	
		System.out.println("--------------------"+memory.arr[0]+"---------------------");
		System.out.println("--------------------"+memory.arr[8]+"---------------------");
	}

	// Allow controller to configure integer instruction latencies
	public void setIntegerLatencies(int addi, int subi, int daddi, int dsubi, int branch) {
		this.addilat = addi;
		this.subilat = subi;
		this.daddilat = daddi;
		this.dsubilat = dsubi;
		this.branchlat = branch;
	}

	public void resetBus() {
		bus.sourceID = null;
	}

	public boolean next1() {

		issueSummary = "issue: \n";
		executeSummary = "execute: \n";
		publishSummary = "publish: \n";
		busSummary = "bus: \n";

		resetBus();

		if (pc >= program.getInstructionQueue().length && allStationsEmpty())
			return false;
		checkExecution();
		boolean issueSuccessful = tryIssue();
		checkPublish();
		checkBus();
		System.out.println(printCycle());
		return issueSuccessful;
	}

	public boolean next2(boolean issueSuccessful) {
		// Handle branch resolution
		if (branchPending) {
			if (branchTaken) {
				// Flush younger speculative instructions
				flushYoungerInstructions(branchInstIndex);
				pc = branchTarget;
			} else {
				pc++;
			}
			branchPending = false;
			branchTaken = false;
			branchTarget = -1;
			branchInstIndex = -1;
		} else if (issueSuccessful) {
			pc++;
		}
		cycle++;
		return true;
	}

	public boolean allStationsEmpty() {
		boolean ret = true;
		ret &= reservationEmpty(addStation);
		ret &= reservationEmpty(mulStation);
		ret &= reservationEmpty(integerStation);
		ret &= loadBuffersEmpty();
		ret &= storeBuffersEmpty();
		return ret;
	}

	public boolean storeBuffersEmpty() {
		int c = 0;
		for (StoreBuffer res : sb.getStation())
			if (res.isBusy())
				c++;
		return c == 0;
	}

	public boolean loadBuffersEmpty() {
		int c = 0;
		for (LoadBuffer res : lb.getStation())
			if (res.isBusy())
				c++;
		return c == 0;
	}

	public boolean reservationEmpty(ReservationStation station) {
		int c = 0;
		for (Reservation res : station.getStation())
			if (res.isBusy())
				c++;
		return c == 0;
	}

	// checks if any reservation needs data on bus and assign it
	public void checkBus() {
		// Check if any slot need any thing from the BUS
		if (bus.sourceID == null) {
			return;
		}

		ALUStationTakeBus(0);
		ALUStationTakeBus(1);
		ALUStationTakeBus(2); // Integer station
		StoreBufferTakeBus();
	}

	public void ALUStationTakeBus(int x) {
		ReservationStation curr;
		if (x == 0)
			curr = addStation;
		else if (x == 1)
			curr = mulStation;
		else
			curr = integerStation;

		for (Reservation res : curr.getStation()) {
			if (res.getQj() != null && res.getQj().equals(bus.sourceID)) {
				// Now i had something i need from the bus at Qj for this reservation
				res.setQj(null);
				res.setVj(bus.value);
			}
			if (res.getQk() != null && res.getQk().equals(bus.sourceID)) {
				// Now i had something i need from the bus at Qk for this reservation
				res.setQk(null);
				res.setVk(bus.value);
			}
		}
	}

	public void StoreBufferTakeBus() {
		for (StoreBuffer res : sb.getStation()) {
			if (res.getQ() != null && res.getQ().equals(bus.sourceID)) {
				// Now i had something i need from the bus at Q for this storeBuffer
				res.setQ(null);
				res.setV(bus.value);
			}
		}
	}

	public void getFinished(ArrayList<Object> finishedSlots, int x) // Load Add Mul Integer
	{
		ReservationStation curr = null;
		if (x == 0)
			curr = addStation;
		else if (x == 1)
			curr = mulStation;
		else if (x == 2)
			curr = integerStation;

		if (curr != null) {
			for (Reservation res : curr.getStation()) {
				int positionInProgram = res.index;
				if (positionInProgram == -1)
					continue;
				if (program.getInstructionQueue()[res.index].getEndExec() <= cycle && res.busy == true) {
					// This one can be considered as a finished instruction
					finishedSlots.add(res);
				}
			}
		} else if (x == 3) {
			// Load buffers
			for (LoadBuffer lodB : lb.getStation()) {
				int positionInProgram = lodB.index;
				if (positionInProgram == -1)
					continue;
				if (program.getInstructionQueue()[lodB.index].getEndExec() <= cycle && lodB.busy == true) {
					finishedSlots.add(lodB);
				}
			}
		}
	}

	// checks if any instruction finished execution and publishes result on bus
	public void checkPublish() {
		// Loop over all the Stations
		// If any instruction in any station has endExec == currCycle:
		// for each instruction loop over all other instructions(stations count
		// dependencies (store + add + mul) then get Max)
		checkFinishedStores();
		ArrayList<Object> finishedSlots = new ArrayList<Object>();
		for (int i = 0; i < 4; ++i) {
			getFinished(finishedSlots, i);// return arrayList of reservations (0=add, 1=mul, 2=integer, 3=load)
		}

		int max = Integer.MIN_VALUE; // mx
		ArrayList<Integer> dependencies = new ArrayList<Integer>();
		for (Object x : finishedSlots) // Count in the loop
		{
			if (x.getClass().getSimpleName().equals("Reservation")) // pc instruction has station of the this finiished
																	// ++
			{
				int cnt = 0;
				cnt = (countDependencies(((Reservation) x).getID()));
				// my type is Reservation
				Op operation = ((Reservation) x).getOp();
				if (pc < program.getInstructionQueue().length) {
					InstructionType needToBeAdded = program.getInstructionQueue()[pc].getInstructionType();
					if (needToBeAdded == InstructionType.ADD || needToBeAdded == InstructionType.SUB) {
						if (operation == Op.ADD || operation == Op.SUB) {
							// We are on the same type
							cnt += (addStation.size >= addStation.getStation().length) ? 1 : 0;
						}
					}
					if (needToBeAdded == InstructionType.MUL || needToBeAdded == InstructionType.DIV) {
						if (operation == Op.MUL || operation == Op.DIV) {
							// We are on the same type
							cnt += (mulStation.size >= mulStation.getStation().length) ? 1 : 0;
						}
					}
					if (needToBeAdded == InstructionType.ADDI || needToBeAdded == InstructionType.SUBI ||
							needToBeAdded == InstructionType.DADDI || needToBeAdded == InstructionType.DSUBI) {
						if (operation == Op.ADDI || operation == Op.SUBI || operation == Op.DADDI
								|| operation == Op.DSUBI) {
							// We are on the same type
							cnt += (integerStation.size >= integerStation.getStation().length) ? 1 : 0;
						}
					}
				}

				dependencies.add(cnt);
			} else {
				int cnt = (countDependencies(((LoadBuffer) x).getQ()));
				if (pc < program.getInstructionQueue().length) {
					InstructionType needToBeAdded = program.getInstructionQueue()[pc].getInstructionType();
					if (needToBeAdded == InstructionType.LOAD || needToBeAdded == InstructionType.L_D ||
							needToBeAdded == InstructionType.L_S || needToBeAdded == InstructionType.LW ||
							needToBeAdded == InstructionType.LD) {
						cnt += (lb.size >= lb.getStation().length) ? 1 : 0;
					}
				}
				dependencies.add(cnt);

			}

		}

		if (dependencies.isEmpty()) {

			// System.out.println("no finished slots");
			publishSummary += " No Instructions are publishing on the bus ";

			return;
		}

		// Get Max from the count
		Integer maxVal = Collections.max(dependencies);
		Integer maxIdx = dependencies.indexOf(maxVal);

		// Now iam Published

		publish(finishedSlots.get(maxIdx));

		publishSummary += "Dependencies " + maxVal + "\n";

	}

	public void checkFinishedStores() {
		for (StoreBuffer b : sb.getStation()) {
			if (program.getInstructionQueue()[b.index].endExec == cycle && b.isBusy()) {
				// Perform the store operation
				Instruction inst = program.getInstructionQueue()[b.index];
				int dataSize = getStoreDataSize(b.index);
				double[] data = new double[dataSize];
				for (int i = 0; i < dataSize; i++) {
					data[i] = b.v; // For simplicity, store same value
				}
				cache.store(b.getA(), data);
				b.setBusy(false);
			}
		}
	}

	public void publish(Object x) {
		String operation;
		if (x.getClass().getSimpleName().equals("Reservation")) {
			operation = ((Reservation) x).getOp() + "";
		} else {
			// Load buffer - determine operation from instruction type
			LoadBuffer lb = (LoadBuffer) x;
			Instruction inst = program.getInstructionQueue()[lb.index];
			if (inst.instructionType == InstructionType.L_S) {
				operation = "L.S";
			} else if (inst.instructionType == InstructionType.LW) {
				operation = "LW";
			} else if (inst.instructionType == InstructionType.LD) {
				operation = "LD";
			} else {
				operation = "L.D"; // Default for L.D and LOAD
			}
		}
		/*
		 * This One Made the 4 targets 1- Write on BUS (Value and ID) 2- Nullify the
		 * Register File if possible and write data there 3- Busy False 4- size--
		 * 
		 */
		getResult(operation, x);
	}

	public void getResult(String op, Object slot) {

		String rd = "";
		ReservationID id = null;
		double result = 0;
		boolean isInteger = false;
		Instruction inst = null;

		if (slot.getClass().getSimpleName().equals("Reservation")) {
			inst = program.getInstructionQueue()[((Reservation) slot).index];
		} else {
			inst = program.getInstructionQueue()[((LoadBuffer) slot).index];
		}

		switch (op) {
			case "LD":
			case "L.D":
			case "L.S":
			case "LW":
				// Load from cache
				int loadAddr = ((LoadBuffer) slot).getA();
				int dataSize = 8; // Default for L.D and LD
				if (op.equals("L.S") || op.equals("LW")) {
					dataSize = 4; // Single precision or word
				}

				Cache.CacheResult cacheResult = cache.load(loadAddr, dataSize);
				// For simplicity, take first value (in real system, would combine bytes)
				result = cacheResult.data[0];

				lb.size--;
				rd = inst.getRd();
				break;

			case "ADD":
			case "SUB":
				addStation.size--;
				if (op.equals("ADD")) {
					result = ((Reservation) slot).getVj() + ((Reservation) slot).getVk();
				} else {
					result = ((Reservation) slot).getVj() - ((Reservation) slot).getVk();
				}
				rd = inst.getRd();
				break;
			case "MUL":
			case "DIV":
				mulStation.size--;
				if (op.equals("MUL")) {
					result = ((Reservation) slot).getVj() * ((Reservation) slot).getVk();
				} else {
					result = ((Reservation) slot).getVj() / ((Reservation) slot).getVk();
				}
				rd = inst.getRd();
				break;
			case "ADDI":
			case "SUBI":
			case "DADDI":
			case "DSUBI":
				integerStation.size--;
				isInteger = true;
				if (op.equals("ADDI") || op.equals("DADDI")) {
					result = ((Reservation) slot).getVj() + ((Reservation) slot).getVk();
				} else {
					result = ((Reservation) slot).getVj() - ((Reservation) slot).getVk();
				}
				rd = inst.getRd();
				break;
		}

		// Make the slot busy back to false
		if (slot.getClass().getSimpleName().equals("Reservation") == true) {
			((Reservation) slot).setBusy(false);
			id = ((Reservation) slot).ID;
		} else {
			id = ((LoadBuffer) slot).Q;
			((LoadBuffer) slot).setBusy(false);
		}

		// Now i have the operation Result and the RD of the slot that will be removed

		// GET the registerIndex
		// GET the registerIndex and write result only if safe (prevent WAW)
		if (rd != null) {
			int targetIdx = getRegisterIndex(rd);
			// For integer destination registers
			if (isInteger || (rd != null && rd.charAt(0) == 'R')) {
				ReservationID regQi = rf.getQiInteger(targetIdx);
				// Only write if no outstanding tag or if tag matches this producer
				if (regQi == null || regQi.equals(id)) {
					rf.setValueInteger(targetIdx, result);
					if (regQi != null && regQi.equals(id))
						rf.setQiInteger(targetIdx, null);
				}
			} else {
				// Floating destination
				ReservationID regQi = rf.getQiFloating(targetIdx);
				if (regQi == null || regQi.equals(id)) {
					rf.setValueFloating(targetIdx, result);
					if (regQi != null && regQi.equals(id))
						rf.setQiFloating(targetIdx, null);
				}
			}
		}

		// FINALPUT ON THE BUS THE VALUE OF RESULT + ID OF SLOT
		bus.setValue(result);
		bus.sourceID = id;

		publishSummary += " instruction " + ((slot.getClass().getSimpleName().equals("Reservation") == true)
				? program.getInstructionQueue()[((Reservation) slot).index]
				: program.getInstructionQueue()[((LoadBuffer) slot).index]) + " started publishing on the bus ";

		return;
	}

	public Integer countDependencies(ReservationID id) {
		int ans = 0;
		for (Reservation res : addStation.getStation()) {
			if ((res.getQj() != null && res.getQj().equals(id)) || (res.getQk() != null && res.getQk().equals(id))) {
				ans++; // RAW Dependency
			}
		}
		for (Reservation res : mulStation.getStation()) {
			if ((res.getQj() != null && res.getQj().equals(id)) || (res.getQk() != null && res.getQk().equals(id))) {
				ans++; // RAW Dependency
			}
		}
		for (Reservation res : integerStation.getStation()) {
			if ((res.getQj() != null && res.getQj().equals(id)) || (res.getQk() != null && res.getQk().equals(id))) {
				ans++; // RAW Dependency
			}
		}
		for (StoreBuffer res : sb.getStation()) {
			if (res.getQ() != null && res.getQ().equals(id)) {
				ans++; // RAW Dependency
			}
		}

		return ans;
	}

	public void checkExecution() {
		// LOGIC loop over all stations if Vj and Vk available set sstartCycle with
		// current Cycle
		ALUCheckExecution(0);
		ALUCheckExecution(1);
		ALUCheckExecution(2); // Integer station
		storeBufferCheckExecution();
		loadBufferCheckExecution();
	}

	public void loadBufferCheckExecution() {
		for (LoadBuffer buff : lb.getStation()) {
			if (buff.busy) {
				int instructionLocation = buff.index;
				Instruction instruction = program.getInstructionQueue()[instructionLocation];
				if (instruction.startExec == -1) {
					// Check for address clashes with store buffers
					if (!hasAddressClash(buff.getA(), getDataSize(instruction.instructionType), true)) {
						instruction.setStartExec(cycle);
						// Determine latency from cache if available
						int dataSize = getDataSize(instruction.instructionType);
						int latency = (cache != null) ? cache.getLoadLatency(buff.getA(), dataSize)
								: getLatency(instruction.instructionType);
						instruction.setEndExec(cycle + latency);
					}
				}
			}
		}
	}

	private boolean hasAddressClash(int addr, int size, boolean isLoad) {
		// Check for address clashes between load and store buffers
		if (isLoad) {
			// Check against all store buffers
			for (StoreBuffer sb : this.sb.getStation()) {
				if (sb.isBusy() && sb.getQ() == null) {
					if (cache.addressesOverlap(addr, size, sb.getA(), getStoreDataSize(sb.index))) {
						return true;
					}
				}
			}
		} else {
			// Check against all load buffers
			for (LoadBuffer lb : this.lb.getStation()) {
				if (lb.isBusy() && lb.getQ() == null) {
					if (cache.addressesOverlap(addr, size, lb.getA(), getLoadDataSize(lb.index))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private int getDataSize(InstructionType type) {
		if (type == InstructionType.L_S || type == InstructionType.S_S || type == InstructionType.LW) {
			return 4;
		}
		return 8; // L.D, S.D, LD
	}

	private int getLoadDataSize(int index) {
		return getDataSize(program.getInstructionQueue()[index].instructionType);
	}

	private int getStoreDataSize(int index) {
		return getDataSize(program.getInstructionQueue()[index].instructionType);
	}

	public void storeBufferCheckExecution() {
		for (StoreBuffer buff : sb.getStation()) {
			if (buff.getQ() == null && buff.busy == true) {
				int instructionLocation = buff.index;
				Instruction instruction = program.getInstructionQueue()[instructionLocation];
				if (instruction.startExec == -1) {
					// Check for address clashes with load buffers
					int dataSize = getStoreDataSize(instructionLocation);
					if (!hasAddressClash(buff.getA(), dataSize, false)) {
						instruction.setStartExec(cycle);
						// Determine latency from cache if available
						int latency = (cache != null) ? cache.getStoreLatency(buff.getA(), new double[dataSize])
								: getLatency(instruction.instructionType);
						instruction.setEndExec(cycle + latency);
					}
				}
			}
		}
	}

	public void ALUCheckExecution(int i) {
		ReservationStation curr;
		if (i == 0)
			curr = addStation;
		else if (i == 1)
			curr = mulStation;
		else
			curr = integerStation;

		for (Reservation res : curr.getStation()) {
			if (res.getQj() == null && res.getQk() == null && res.busy == true) {
				// I don't have neither both so all is OK with my inputs
				// start EXEC
				Instruction instruction = program.getInstructionQueue()[res.index];
				if (instruction.startExec == -1) {
					instruction.setStartExec(cycle);
					instruction.setEndExec(cycle + getLatency(instruction.instructionType));
				}
			}
		}
	}

	public String executeSummary() {
		String res = "execute: \n";
		for (int i = 0; i < program.getInstructionQueue().length; i++) {
			Instruction inst = program.getInstructionQueue()[i];
			if (inst.getStartExec() == cycle) {
				res += "\nthe following instruction started execution: \n" + inst.toString() + "\n";
			}
			if (inst.getEndExec() == cycle) {
				res += "\nthe following instruction finished executing\n" + inst.toString() + "\n";
			}
		}
		return res + "\n";

	}

	public String busSummary() {
		String ret = "bus: ";
		if (bus.sourceID == null)
			ret += "empty \n";
		else
			ret += bus.toString() + "\n";
		return ret;

	}

	public String getCycleSummary() {
		String res = "";
		res += "cycle number " + (cycle) + ": \n"; // To serve GUI shifting bug, was cycle only
		res += issueSummary;
		res += executeSummary();
		res += publishSummary;
		res += busSummary();
		return res;
	}

	public String printCycle() {
		String res = "";
		res += getCycleSummary();
		res += printStation(addStation);
		res += printStation(mulStation);
		res += printStation(integerStation);
		res += printLoadBuffers();
		res += printStoreBuffers();
		res += printProgram();
		res += printRegisterFile();
		if (cache != null) {
			res += cache.getCacheStatus() + "\n";
		}
		return res;

	}

	public String printProgram() {
		return "program: \n" + program.toString();
	}

	public String printRegisterFile() {
		String ret = "Floating Register File: \n";

		for (int i = 0; i < 32; i++)
			ret += "F" + i + " " + rf.getFloating()[i] + "\n";
		// for (int i = 0; i < 32; i++)
		// ret += "R" + i + " " + rf.getInteger()[i];
		return ret;
	}

	public String printStoreBuffers() {
		String ret = "";
		ret += "Store Buffers: \n";
		for (int i = 0; i < sb.getStation().length; i++)
			ret += sb.getStation()[i] + "\n";
		return ret;
	}

	public String printLoadBuffers() {
		String ret = "";
		ret += "Load Buffers: \n";

		for (int i = 0; i < lb.getStation().length; i++)
			ret += lb.getStation()[i] + "\n";
		return ret;
	}

	public String printStation(ReservationStation station) {
		String ret = "";
		ret += station.type.toString() + " station: \n";
		for (Reservation res : station.getStation()) {
			ret += res.toString() + "\n";
		}
		return ret + "\n";

	}

	public boolean tryIssue() {
		if (pc >= program.getInstructionQueue().length) // No More to issue in the stations
		{
			issueSummary += "no more issues remaining\n";
			return false;
		}
		Instruction current = program.getInstructionQueue()[pc];
		boolean yes = false;

		// Floating point operations
		if (current.instructionType == InstructionType.ADD || current.instructionType == InstructionType.SUB) {
			Op op = current.instructionType == InstructionType.ADD ? Op.ADD : Op.SUB;
			yes = issueStation(addStation, op);
		} else if (current.instructionType == InstructionType.DIV || current.instructionType == InstructionType.MUL) {
			Op op = current.instructionType == InstructionType.DIV ? Op.DIV : Op.MUL;
			yes = issueStation(mulStation, op);
		}
		// Integer operations
		else if (current.instructionType == InstructionType.ADDI || current.instructionType == InstructionType.SUBI ||
				current.instructionType == InstructionType.DADDI || current.instructionType == InstructionType.DSUBI) {
			Op op;
			if (current.instructionType == InstructionType.ADDI)
				op = Op.ADDI;
			else if (current.instructionType == InstructionType.SUBI)
				op = Op.SUBI;
			else if (current.instructionType == InstructionType.DADDI)
				op = Op.DADDI;
			else
				op = Op.DSUBI;
			yes = issueIntegerStation(op);
		}
		// Load operations
		else if (current.instructionType == InstructionType.LOAD || current.instructionType == InstructionType.L_D ||
				current.instructionType == InstructionType.L_S || current.instructionType == InstructionType.LW ||
				current.instructionType == InstructionType.LD) {
			yes = issueLoadBuffer();
		}
		// Store operations
		else if (current.instructionType == InstructionType.STORE || current.instructionType == InstructionType.S_D ||
				current.instructionType == InstructionType.S_S) {
			yes = issueStoreBuffer();
		}
		// Branch operations
		else if (current.instructionType == InstructionType.BEQ || current.instructionType == InstructionType.BNE) {
			yes = issueBranch();
		}

		if (yes)
			issueSummary += " instruction " + current + " is issued\n";
		else
			issueSummary += " instruction " + current + " can not be issued\n";

		return yes;
	}

	public boolean issueStation(ReservationStation station, Op op) {
		for (int i = 0; i < station.getStation().length; i++) {
			Reservation res = station.getStation()[i];
			if (!res.busy) {
				res.setBusy(true);
				station.size++;
				res.op = op;
				prepareInstructionArithmetic(program.instructionQueue[pc], res);
				return true;

			}
		}
		return false;
	}

	public boolean issueIntegerStation(Op op) {
		for (int i = 0; i < integerStation.getStation().length; i++) {
			Reservation res = integerStation.getStation()[i];
			if (!res.busy) {
				res.setBusy(true);
				integerStation.size++;
				res.op = op;
				prepareInstructionInteger(program.instructionQueue[pc], res);
				return true;
			}
		}
		return false;
	}

	public void prepareInstructionInteger(Instruction instruction, Reservation res) {
		res.index = pc;
		String rs = instruction.getRs();
		int idx = getRegisterIndex(rs);
		if (rf.getInteger()[idx].Qi == null) {
			res.Vj = rf.getInteger()[idx].value;
		} else {
			res.Qj = rf.getInteger()[idx].Qi;
		}

		// For integer instructions, rt is an immediate value or register
		String rt = instruction.getRt();
		try {
			// Try parsing as immediate value
			double immediate = Double.parseDouble(rt);
			res.Vk = immediate;
		} catch (NumberFormatException e) {
			// It's a register
			idx = getRegisterIndex(rt);
			if (rf.getInteger()[idx].Qi == null) {
				res.Vk = rf.getInteger()[idx].value;
			} else {
				res.Qk = rf.getInteger()[idx].Qi;
			}
		}

		String rd = instruction.getRd();
		idx = getRegisterIndex(rd);
		rf.getInteger()[idx].Qi = res.ID;
	}

	public boolean issueBranch() {
		Instruction current = program.getInstructionQueue()[pc];
		// Check if source registers are ready
		int rsIdx = getRegisterIndex(current.getRs());
		int rtIdx = getRegisterIndex(current.getRt());

		// Branches need both registers to be ready (no renaming for branches)
		if (rf.getInteger()[rsIdx].Qi != null || rf.getInteger()[rtIdx].Qi != null) {
			return false; // Can't issue - registers not ready
		}

		// Issue branch - it will execute immediately
		current.setStartExec(cycle);
		current.setEndExec(cycle + branchlat);
		branchTarget = current.getBranchTarget();
		// Remember index of branch instruction to flush younger instructions if taken
		branchInstIndex = pc;

		// Evaluate branch condition
		double rsVal = rf.getInteger()[rsIdx].value;
		double rtVal = rf.getInteger()[rtIdx].value;

		if (current.instructionType == InstructionType.BEQ) {
			branchTaken = (rsVal == rtVal);
		} else if (current.instructionType == InstructionType.BNE) {
			branchTaken = (rsVal != rtVal);
		}

		branchPending = true;
		return true;
	}

	/**
	 * Flush any instructions (in reservation stations and buffers) that are younger
	 * than
	 * the branch instruction at index "branchIdx". Clears reservation busy flags
	 * and
	 * associated register rename tags when applicable.
	 */
	private void flushYoungerInstructions(int branchIdx) {
		// Flush reservation stations (add, mul, integer)
		flushReservationStation(addStation, branchIdx);
		flushReservationStation(mulStation, branchIdx);
		if (integerStation != null)
			flushReservationStation(integerStation, branchIdx);

		// Flush load buffers
		for (LoadBuffer lbuff : lb.getStation()) {
			if (lbuff.isBusy() && lbuff.index > branchIdx) {
				// Clear register Qi assigned to this load if still pointing to this buffer
				Instruction inst = program.getInstructionQueue()[lbuff.index];
				String rd = inst.getRd();
				if (rd != null) {
					int rIdx = getRegisterIndex(rd);
					if (rd.charAt(0) == 'R') {
						ReservationID q = rf.getQiInteger(rIdx);
						if (q != null && q.equals(lbuff.getQ()))
							rf.setQiInteger(rIdx, null);
					} else {
						ReservationID q = rf.getQiFloating(rIdx);
						if (q != null && q.equals(lbuff.getQ()))
							rf.setQiFloating(rIdx, null);
					}
				}
				lbuff.setBusy(false);
				lbuff.index = -1;
				lb.size = Math.max(0, lb.size - 1);
			}
		}

		// Flush store buffers (no register dest to clear, just free)
		for (StoreBuffer sbuf : sb.getStation()) {
			if (sbuf.isBusy() && sbuf.index > branchIdx) {
				sbuf.setBusy(false);
				sbuf.index = -1;
			}
		}
	}

	private void flushReservationStation(ReservationStation station, int branchIdx) {
		for (Reservation res : station.getStation()) {
			if (res.isBusy() && res.index > branchIdx) {
				// If this reservation was going to write to a register, clear its Qi if still
				// set
				Instruction inst = program.getInstructionQueue()[res.index];
				String rd = inst.getRd();
				if (rd != null) {
					int ridx = getRegisterIndex(rd);
					if (rd.charAt(0) == 'R') {
						ReservationID q = rf.getQiInteger(ridx);
						if (q != null && q.equals(res.ID))
							rf.setQiInteger(ridx, null);
					} else {
						ReservationID q = rf.getQiFloating(ridx);
						if (q != null && q.equals(res.ID))
							rf.setQiFloating(ridx, null);
					}
				}
				res.setBusy(false);
				res.index = -1;
				station.size = Math.max(0, station.size - 1);
				res.setQj(null);
				res.setQk(null);
			}
		}
	}

	public void prepareInstructionArithmetic(Instruction instruction, Reservation res) {
		res.index = pc;
		String rs = instruction.getRs();
		if (rs.charAt(0) == 'F') {
			int idx = getRegisterIndex(rs);
			if (rf.getFloating()[idx].Qi == null) {
				res.Vj = rf.getFloating()[idx].value;
			} else {
				res.Qj = rf.getFloating()[idx].Qi;
			}
		} else {
			int idx = getRegisterIndex(rs);
			if (rf.getInteger()[idx].Qi == null) {
				res.Vj = rf.getInteger()[idx].value;
			} else {
				res.Qj = rf.getInteger()[idx].Qi;
			}

		}
		String rt = instruction.getRt();
		if (rt.charAt(0) == 'F') {
			int idx = getRegisterIndex(rt);
			if (rf.getFloating()[idx].Qi == null) {
				res.Vk = rf.getFloating()[idx].value;
			} else {
				res.Qk = rf.getFloating()[idx].Qi;
			}
		} else {
			int idx = getRegisterIndex(rt);
			if (rf.getInteger()[idx].Qi == null) {
				res.Vk = rf.getInteger()[idx].value;
			} else {
				res.Qk = rf.getInteger()[idx].Qi;
			}

		}
		String rd = instruction.getRd();
		int idx = getRegisterIndex(rd);
		Register reg;
		if (rd.charAt(0) == 'F')
			reg = rf.getFloating()[idx];

		else
			reg = rf.getInteger()[idx];

		reg.Qi = res.ID;
	}

	public int getRegisterIndex(String r) {
		return Integer.parseInt(r.substring(1));
	}

	public boolean issueStoreBuffer() {
		for (int i = 0; i < lb.getStation().length; i++) {
			StoreBuffer buffer = sb.getStation()[i];
			if (!buffer.busy) {
				buffer.setBusy(true);
				prepareInstructionStore(program.instructionQueue[pc], buffer);
				return true;

			}
		}
		return false;

	}

	public void prepareInstructionStore(Instruction instruction, StoreBuffer buffer) {
		int rs = getRegisterIndex(instruction.rs);
		int A = rf.getValueInteger(rs) + instruction.offset;
		buffer.setA(A);
		String rd = instruction.getRd();
		if (rd.charAt(0) == 'F') {
			int idx = getRegisterIndex(rd);
			if (rf.getFloating()[idx].Qi == null) {
				buffer.v = rf.getFloating()[idx].value;
			} else {
				buffer.Q = rf.getFloating()[idx].Qi;
			}
		} else {
			int idx = getRegisterIndex(rd);
			if (rf.getInteger()[idx].Qi == null) {
				buffer.v = rf.getInteger()[idx].value;
			} else {
				buffer.Q = rf.getInteger()[idx].Qi;
			}

		}
		buffer.index = pc;

	}

	public boolean issueLoadBuffer() {
		for (int i = 0; i < lb.getStation().length; i++) {
			LoadBuffer buffer = lb.getStation()[i];
			if (!buffer.busy) {
				buffer.setBusy(true);
				lb.size++;
				prepareInstructionLoad(program.instructionQueue[pc], buffer);
				return true;

			}
		}
		return false;

	}

	public void prepareInstructionLoad(Instruction instruction, LoadBuffer buffer) {
		int rs = getRegisterIndex(instruction.rs);
		int A = rf.getValueInteger(rs) + instruction.offset;
		buffer.setA(A);

		// Determine if loading to integer or floating point register
		String rd = instruction.getRd();
		int rdIdx = getRegisterIndex(rd);
		if (rd != null && rd.charAt(0) == 'R') {
			// Integer register
			rf.setQiInteger(rdIdx, buffer.Q);
		} else {
			// Floating point register
			rf.setQiFloating(rdIdx, buffer.Q);
		}

		// Execution will start when address clash is resolved
		buffer.index = pc;
	}

	public ReservationStation getAddStation() {
		return addStation;
	}

	public ReservationStation getMulStation() {
		return mulStation;
	}

	public ReservationStation getIntegerStation() {
		return integerStation;
	}

	public LoadBuffers getLoadStation() {
		return lb;
	}

	public StoreBuffers getStoreStation() {
		return sb;
	}

	public RegisterFile getRegisterFile() {
		return rf;
	}

	public Cache getCache() {
		return cache;
	}

	public Memory getMemory() {
		return memory;
	}

	public int getLatency(InstructionType instructionType) {
		if (instructionType == InstructionType.ADD)
			return addlat;
		if (instructionType == InstructionType.SUB)
			return sublat;
		if (instructionType == InstructionType.MUL)
			return mullat;
		if (instructionType == InstructionType.DIV)
			return divlat;
		if (instructionType == InstructionType.LOAD || instructionType == InstructionType.L_D ||
				instructionType == InstructionType.L_S || instructionType == InstructionType.LW ||
				instructionType == InstructionType.LD)
			return ldlat;
		if (instructionType == InstructionType.STORE || instructionType == InstructionType.S_D ||
				instructionType == InstructionType.S_S)
			return stlat;
		if (instructionType == InstructionType.ADDI)
			return addilat;
		if (instructionType == InstructionType.SUBI)
			return subilat;
		if (instructionType == InstructionType.DADDI)
			return daddilat;
		if (instructionType == InstructionType.DSUBI)
			return dsubilat;
		if (instructionType == InstructionType.BEQ || instructionType == InstructionType.BNE)
			return branchlat;

		return 0;
	}

}
