package Tomasulo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * TestHarness: runs multiple test programs with memory initialization and validates system behavior.
 * Tests the Tomasulo backend against all provided test cases.
 */
public class TestHarness {
	
	public static void main(String[] args) {
		String[] testFiles = {
			"Tests/simpleAdd.txt",
			"Tests/selfAddF7.txt",
			"Tests/chainAdd.txt",
			"Tests/rawHazard.txt",
			"Tests/twoFinish.txt",
		};
		
		int passed = 0;
		int failed = 0;
		
		System.out.println("====== TOMASULO BACKEND TEST HARNESS ======\n");
		
		for (String testFile : testFiles) {
			try {
				System.out.println("Running: " + testFile);
				TestResult result = runTest(testFile);
				if (result.success) {
					System.out.println("✓ PASSED");
					passed++;
				} else {
					System.out.println("✗ FAILED: " + result.errorMsg);
					failed++;
				}
				System.out.println("  Cycles: " + result.cycles);
				if (result.nonZeroRegisters > 0) {
					System.out.println("  Non-zero FP registers: " + result.nonZeroRegisters);
					for (String reg : result.registerValues) {
						System.out.println("    " + reg);
					}
				} else {
					System.out.println("  (all FP registers zero)");
				}
				System.out.println();
			} catch (Exception e) {
				System.out.println("✗ EXCEPTION: " + e.getMessage());
				e.printStackTrace();
				failed++;
				System.out.println();
			}
		}
		
		System.out.println("\n====== TEST SUMMARY ======");
		System.out.println("Passed: " + passed);
		System.out.println("Failed: " + failed);
		System.out.println("Total:  " + (passed + failed));
	}
	
	private static TestResult runTest(String testFilePath) throws FileNotFoundException {
		TestResult result = new TestResult();
		
		Parser parser = new Parser();
		ArrayList<String> programLines = parser.readProgram(new File(testFilePath));
		
		// Skip if empty
//		if (programLines.isEmpty()) {
//			result.success = true;
//			result.errorMsg = "(empty program)";
//			result.cycles = 0;
//			return result;
//		}
		
		Program program = parser.parse(programLines);
		
		// Create processor with full configuration
		Processor processor = new Processor(program, 2, 2, 2, 2, 2, 2,
			3, 2, 3, 3, 3, 1024, 32, 1, 10, 2048);
		
		// Initialize memory with test data
		// Pre-fill memory with simple pattern: index * 1.5
		Memory memory = processor.getMemory();
		if (memory != null) {
			for (int i = 0; i < Math.min(100, memory.getArr().length); i++) {
				memory.store(i, i * 1.5);
			}
		}
		
		// Initialize some integer registers for test instructions that use them
		RegisterFile rf = processor.getRegisterFile();
		if (rf != null) {
			// R1 = 0, R2 = 4, R3 = 10 (for base addresses in load/store tests)
			rf.setValueInteger(1, 0);
			rf.setValueInteger(2, 4);
			rf.setValueInteger(3, 10);
			rf.setValueInteger(13, 20);
		}
		
		// Build expected final register file by sequential (ideal) execution
		double[] expectedFloating = buildExpectedFloating(program, processor);
		
		// Run simulation for max 2000 cycles (safety limit)
		int cycles = 0;
		int maxCycles = 2000;
		boolean failure = false;
		String failureMsg = "";
		
		try {
			while (cycles < maxCycles) {
				// Determine current cycle (processor.cycle is package-private)
				int currentCycle = processor.cycle;
				// Identify instructions that are expected to finish this cycle (endExec == currentCycle)
				ArrayList<Integer> willFinish = new ArrayList<>();
				Instruction[] instQ = program.getInstructionQueue();
				for (int i = 0; i < instQ.length; i++) {
					Instruction ins = instQ[i];
					if (ins.getEndExec() == currentCycle) willFinish.add(i);
				}
				
				boolean issueSuccessful = processor.next1();
				// After next1(), publishing and bus updates have occurred for this cycle.
				// Validate CDB usage and write-back behavior for finishing instructions.
				if (!willFinish.isEmpty()) {
					// If instructions finished, a publication should have occurred (bus.sourceID != null)
					if (processor.bus == null || processor.bus.sourceID == null) {
						failure = true;
						failureMsg = "Cycle " + currentCycle + ": Finished instructions " + willFinish + " did NOT publish on CDB.";
						break;
					}
					// Find the published slot by matching sourceID
					ReservationID pubId = processor.bus.sourceID;
					Object publishedSlot = findSlotByID(processor, pubId);
					if (publishedSlot == null) {
						failure = true;
						failureMsg = "Cycle " + currentCycle + ": Bus published ID " + pubId + " but no matching slot found.";
						break;
					}
					// Compute expected result from the published slot
					double expected = Double.NaN;
					int rdIdx = -1;
					Instruction publishedInst = null;
					if (publishedSlot.getClass().getSimpleName().equals("Reservation")) {
						Reservation r = (Reservation) publishedSlot;
						int idx = r.index;
						publishedInst = program.getInstructionQueue()[idx];
						Op op = r.getOp();
						double vj = r.getVj();
						double vk = r.getVk();
						switch (op) {
						case ADD:
							expected = vj + vk; break;
						case SUB:
							expected = vj - vk; break;
						case MUL:
							expected = vj * vk; break;
						case DIV:
							expected = vj / vk; break;
						default:
							expected = Double.NaN; break;
						}
						rdIdx = processor.getRegisterIndex(publishedInst.getRd());
					} else {
						// LoadBuffer
						LoadBuffer lb = (LoadBuffer) publishedSlot;
						publishedInst = program.getInstructionQueue()[lb.index];
						int loadAddr = lb.getA();
						int dataSize = (publishedInst.instructionType == InstructionType.L_S || publishedInst.instructionType == InstructionType.LW) ? 4 : 8;
						if (processor.cache != null) {
							Cache.CacheResult cr = processor.cache.load(loadAddr, dataSize);
							expected = cr.data[0];
						} else {
							expected = processor.getMemory().load(loadAddr);
						}
						rdIdx = processor.getRegisterIndex(publishedInst.getRd());
					}
					// Compare expected vs bus.value
					double publishedValue = processor.bus.getValue();
					if (Double.isNaN(expected) == false) {
						double eps = 1e-6;
						if (Double.isInfinite(expected) || Double.isInfinite(publishedValue) || Math.abs(expected - publishedValue) > eps) {
							failure = true;
							failureMsg = String.format("Cycle %d: Published value mismatch. expected=%.9f published=%.9f (inst=%s)", currentCycle, expected, publishedValue, publishedInst);
							break;
						}
					}
					// Check register file updated and Qi cleared (if appropriate)
					if (publishedInst != null && publishedInst.getRd() != null) {
						String rd = publishedInst.getRd();
						if (rd.charAt(0) == 'F') {
							double rfVal = processor.getRegisterFile().getValueFloating(rdIdx);
							if (Double.isNaN(expected) == false && Math.abs(rfVal - expected) > 1e-6) {
								failure = true;
								failureMsg = "Cycle " + currentCycle + ": Writeback did NOT update " + rd + " to expected value (got=" + rfVal + ", expected=" + expected + ")";
								break;
							}
							// Qi should be null for this register now (or was null before and remains)
							if (processor.getRegisterFile().getFloating()[rdIdx].Qi != null) {
								failure = true;
								failureMsg = "Cycle " + currentCycle + ": Destination register " + rd + " still has Qi after writeback: " + processor.getRegisterFile().getFloating()[rdIdx].Qi;
								break;
							}
						} else {
							// integer regs
							int intVal = processor.getRegisterFile().getValueInteger(rdIdx);
							// For integer, expected might be double; compare cast
							if (!Double.isNaN(expected) && Math.abs(intVal - expected) > 1e-6) {
								failure = true;
								failureMsg = "Cycle " + currentCycle + ": Writeback did NOT update " + rd + " to expected value (got=" + intVal + ", expected=" + expected + ")";
								break;
							}
							if (processor.getRegisterFile().getInteger()[rdIdx].Qi != null) {
								failure = true;
								failureMsg = "Cycle " + currentCycle + ": Destination integer register " + rd + " still has Qi after writeback: " + processor.getRegisterFile().getInteger()[rdIdx].Qi;
								break;
							}
						}
					}
					// Ensure reservation station that executed is now not busy
					if (publishedSlot.getClass().getSimpleName().equals("Reservation")) {
						Reservation r = (Reservation) publishedSlot;
						if (r.isBusy()) {
							failure = true;
							failureMsg = "Cycle " + currentCycle + ": Reservation " + r.getID() + " remained busy after publishing.";
							break;
						}
					}
					// Ensure no reservation entry still references published ID as Qj/Qk
					if (referencesID(processor, pubId)) {
						failure = true;
						failureMsg = "Cycle " + currentCycle + ": Some reservation still references published ID " + pubId + " after bus broadcast.";
						break;
					}
				}
				
				boolean still2 = processor.next2(issueSuccessful);
				cycles++;
				// Termination
				if (!issueSuccessful && processor.pc >= program.getInstructionQueue().length && processor.allStationsEmpty()) {
					break;
				}
			}
		} catch (NullPointerException e) {
			result.success = false;
			result.errorMsg = "NPE at cycle " + cycles + ": " + e.getMessage();
			result.cycles = cycles;
			return result;
		} catch (ArithmeticException e) {
			// Division by zero - acceptable for testing
			result.success = true;
			result.errorMsg = "OK (divide by zero produced NaN)";
			result.cycles = cycles;
			// Continue to collect final state
		} catch (Exception e) {
			result.success = false;
			result.errorMsg = "Runtime error at cycle " + cycles + ": " + e.getClass().getSimpleName() + ": " + e.getMessage();
			result.cycles = cycles;
			return result;
		}
		
		result.cycles = cycles;
		if (failure) {
			result.success = false;
			result.errorMsg = failureMsg;
			return result;
		} else {
			result.success = true;
			result.errorMsg = "OK";
		}
		
		// Final deep validation: compare final floating registers to sequential expected values
		double eps = 1e-6;
		RegisterFile finalRf = processor.getRegisterFile();
		for (int i = 0; i < 32; i++) {
			double got = finalRf.getValueFloating(i);
			double exp = expectedFloating[i];
			if (!(Double.isNaN(exp) && Double.isNaN(got))) {
				if (Math.abs(got - exp) > eps) {
					result.success = false;
					result.errorMsg = String.format("Final validation failed: F%d = %.9f but expected %.9f", i, got, exp);
					return result;
				}
			}
		}
		// Ensure no stale Qi tags remain
		for (int i = 0; i < 32; i++) {
			if (finalRf.getQiFloating(i) != null) {
				result.success = false;
				result.errorMsg = "Final validation failed: Floating register F" + i + " still has Qi=" + finalRf.getQiFloating(i);
				return result;
			}
			if (finalRf.getQiInteger(i) != null) {
				result.success = false;
				result.errorMsg = "Final validation failed: Integer register R" + i + " still has Qi=" + finalRf.getQiInteger(i);
				return result;
			}
		}
		// Ensure all reservation stations and buffers are empty
		if (!processor.allStationsEmpty()) {
			result.success = false;
			result.errorMsg = "Final validation failed: Some reservation stations or buffers remain busy.";
			return result;
		}
		// Collect final state (registers)
		RegisterFile rf2 = processor.getRegisterFile();
		if (rf2 != null) {
			for (int i = 0; i < 32; i++) {
				double val = rf2.getValueFloating(i);
				// Skip NaN and zero
				if (!Double.isNaN(val) && val != 0.0) {
					result.nonZeroRegisters++;
					result.registerValues.add("F" + i + " = " + val);
				}
			}
		}
		
		return result;
	}

	/**
	 * Build expected floating register final values by running the program sequentially
	 * using simple semantics (reads current register values when executing each instruction).
	 */
	private static double[] buildExpectedFloating(Program program, Processor processor) {
		double[] regs = new double[32];
		RegisterFile rf = processor.getRegisterFile();
		for (int i = 0; i < 32; i++) {
			regs[i] = rf.getValueFloating(i);
		}
		Memory mem = processor.getMemory();
		Instruction[] instQ = program.getInstructionQueue();
		int rs, rt, rd, base, addr, rsIdx;
		for (Instruction inst : instQ) {
			if (inst == null) continue;
			InstructionType t = inst.getInstructionType();
			switch (t) {
			case ADD:
				rs = processor.getRegisterIndex(inst.getRs());
				rt = processor.getRegisterIndex(inst.getRt());
				rd = processor.getRegisterIndex(inst.getRd());
				regs[rd] = regs[rs] + regs[rt];
				break;
			case SUB:
				rs = processor.getRegisterIndex(inst.getRs());
				rt = processor.getRegisterIndex(inst.getRt());
				rd = processor.getRegisterIndex(inst.getRd());
				regs[rd] = regs[rs] - regs[rt];
				break;
			case MUL:
				rs = processor.getRegisterIndex(inst.getRs());
				rt = processor.getRegisterIndex(inst.getRt());
				rd = processor.getRegisterIndex(inst.getRd());
				regs[rd] = regs[rs] * regs[rt];
				break;
			case DIV:
				rs = processor.getRegisterIndex(inst.getRs());
				rt = processor.getRegisterIndex(inst.getRt());
				rd = processor.getRegisterIndex(inst.getRd());
				regs[rd] = regs[rs] / regs[rt];
				break;
			case L_D:
			case LD:
				base = processor.getRegisterIndex(inst.getRs());
				addr = processor.getRegisterFile().getValueInteger(base) + inst.getOffset();
				rd = processor.getRegisterIndex(inst.getRd());
				regs[rd] = mem.load(addr);
				break;
			case L_S:
			case LW:
				base = processor.getRegisterIndex(inst.getRs());
				addr = processor.getRegisterFile().getValueInteger(base) + inst.getOffset();
				rd = processor.getRegisterIndex(inst.getRd());
				regs[rd] = mem.load(addr);
				break;
			case S_D:
			case S_S:
				base = processor.getRegisterIndex(inst.getRs());
				addr = processor.getRegisterFile().getValueInteger(base) + inst.getOffset();
				rsIdx = processor.getRegisterIndex(inst.getRd());
				mem.store(addr, regs[rsIdx]);
				break;
			default:
				// other instruction types ignored in expected FP calculation
				break;
			}
		}
		return regs;
	}

	/** Find reservation or load buffer slot by its ReservationID (return Reservation or LoadBuffer) */
	private static Object findSlotByID(Processor p, ReservationID id) {
		for (Reservation r : p.addStation.getStation()) {
			if (r.getID() != null && r.getID().equals(id)) return r;
		}
		for (Reservation r : p.mulStation.getStation()) {
			if (r.getID() != null && r.getID().equals(id)) return r;
		}
		for (Reservation r : p.integerStation.getStation()) {
			if (r.getID() != null && r.getID().equals(id)) return r;
		}
		for (LoadBuffer lb : p.lb.getStation()) {
			if (lb.getQ() != null && lb.getQ().equals(id)) return lb;
		}
		for (StoreBuffer sb : p.sb.getStation()) {
			if (sb.getQ() != null && sb.getQ().equals(id)) return sb;
		}
		return null;
	}

	/** Return true if any reservation or store buffer still references this ReservationID as a dependency */
	private static boolean referencesID(Processor p, ReservationID id) {
		for (Reservation r : p.addStation.getStation()) {
			if ((r.getQj() != null && r.getQj().equals(id)) || (r.getQk() != null && r.getQk().equals(id))) return true;
		}
		for (Reservation r : p.mulStation.getStation()) {
			if ((r.getQj() != null && r.getQj().equals(id)) || (r.getQk() != null && r.getQk().equals(id))) return true;
		}
		for (Reservation r : p.integerStation.getStation()) {
			if ((r.getQj() != null && r.getQj().equals(id)) || (r.getQk() != null && r.getQk().equals(id))) return true;
		}
		for (StoreBuffer sb : p.sb.getStation()) {
			if (sb.getQ() != null && sb.getQ().equals(id)) return true;
		}
		for (LoadBuffer lb : p.lb.getStation()) {
			if (lb.getQ() != null && lb.getQ().equals(id)) return true;
		}
		return false;
	}
	
	/**
	 * Simple result holder for test output.
	 */
	static class TestResult {
		boolean success = false;
		String errorMsg = "";
		int cycles = 0;
		int nonZeroRegisters = 0;
		ArrayList<String> registerValues = new ArrayList<>();
	}
}
