package Tomasulo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Parser {
	

	public Parser() {
		// TODO Auto-generated constructor stub
	}

	public Program parse(ArrayList<String> s) {
		// First pass: build label map and parse instructions
		Map<String, Integer> labelMap = new HashMap<>();
		ArrayList<Instruction> instructions = new ArrayList<>();
		
		for (int i = 0; i < s.size(); i++) {
			String line = s.get(i).trim();
			if (line.isEmpty()) continue;
			
			// Check if line has a label
			if (line.contains(":")) {
				String[] parts = line.split(":", 2);
				String label = parts[0].trim();
				String instructionPart = parts[1].trim();
				
				if (!instructionPart.isEmpty()) {
					// Label with instruction on same line
					labelMap.put(label, instructions.size());
					instructions.add(this.parseLine(instructionPart));
				} else {
					// Label on its own line - next instruction gets this label
					labelMap.put(label, instructions.size());
				}
			} else {
				instructions.add(this.parseLine(line));
			}
		}
		
		// Second pass: resolve branch targets
		Instruction[] res = instructions.toArray(new Instruction[instructions.size()]);
		for (int i = 0; i < res.length; i++) {
			if (res[i].getBranchLabel() != null) {
				Integer target = labelMap.get(res[i].getBranchLabel());
				if (target != null) {
					res[i].setBranchTarget(target);
				} else {
					// Try parsing as numeric offset
					try {
						int offset = Integer.parseInt(res[i].getBranchLabel());
						res[i].setBranchTarget(i + offset);
					} catch (NumberFormatException e) {
						System.out.println("Warning: Could not resolve branch label: " + res[i].getBranchLabel());
					}
				}
			}
		}

		return new Program(res);
	}

	public Instruction parseLine(String s) {
		s = s.trim();
		// Handle labels (e.g., "LOOP: L.D F0, 8(R1)")
		if (s.contains(":")) {
			String[] parts = s.split(":", 2);
			s = parts[1].trim();
		}
		
		// Split on commas with optional surrounding whitespace to be more tolerant
		String[] parameters = s.split("\\s*,\\s*");
		String firstPart = parameters[0].trim();
		String[] firstTokens = firstPart.split("\\s+");
		String op = firstTokens[0];
		InstructionType instructionType = null;
		String rd = firstTokens.length > 1 ? firstTokens[1] : null;
		String rs = null;
		String rt = null;
		int offset = -1;
		String branchLabel = null;
		
		// Floating point operations (support both .D and .S mnemonics)
		if (op.equals("DIV.D") || op.equals("DIV.S")) {
			instructionType = InstructionType.DIV;
			rs = parameters[1].trim();
			rt = parameters[2].trim();
		} else if (op.equals("ADD.D") || op.equals("ADD.S")) {
			instructionType = InstructionType.ADD;
			rs = parameters[1].trim();
			rt = parameters[2].trim();
		} else if (op.equals("SUB.D") || op.equals("SUB.S")) {
			instructionType = InstructionType.SUB;
			rs = parameters[1].trim();
			rt = parameters[2].trim();
		} else if (op.equals("MUL.D") || op.equals("MUL.S")) {
			instructionType = InstructionType.MUL;
			rs = parameters[1].trim();
			rt = parameters[2].trim();
		}
		// Load operations
		else if (op.equals("L.D")) {
			instructionType = InstructionType.L_D;
			String addrPart = parameters[1].trim();
			if (addrPart.contains("(")) {
				offset = Integer.parseInt(addrPart.split("\\(")[0]);
				rs = addrPart.split("\\(")[1].split("\\)")[0];
			} else {
				offset = Integer.parseInt(addrPart);
				rs = null;
			}
		} else if (op.equals("L.S")) {
			instructionType = InstructionType.L_S;
			String addrPart = parameters[1].trim();
			if (addrPart.contains("(")) {
				offset = Integer.parseInt(addrPart.split("\\(")[0]);
				rs = addrPart.split("\\(")[1].split("\\)")[0];
			} else {
				offset = Integer.parseInt(addrPart);
				rs = null;
			}
		} else if (op.equals("LW")) {
			instructionType = InstructionType.LW;
			String addrPart = parameters[1].trim();
			if (addrPart.contains("(")) {
				offset = Integer.parseInt(addrPart.split("\\(")[0]);
				rs = addrPart.split("\\(")[1].split("\\)")[0];
			} else {
				offset = Integer.parseInt(addrPart);
				rs = null;
			}
		} else if (op.equals("LD")) {
			instructionType = InstructionType.LD;
			String addrPart = parameters[1].trim();
			if (addrPart.contains("(")) {
				offset = Integer.parseInt(addrPart.split("\\(")[0]);
				rs = addrPart.split("\\(")[1].split("\\)")[0];
			} else {
				offset = Integer.parseInt(addrPart);
				rs = null;
			}
		}
		// Store operations
		else if (op.equals("S.D")) {
			instructionType = InstructionType.S_D;
			String addrPart = parameters[1].trim();
			if (addrPart.contains("(")) {
				offset = Integer.parseInt(addrPart.split("\\(")[0]);
				rs = addrPart.split("\\(")[1].split("\\)")[0];
			} else {
				offset = Integer.parseInt(addrPart);
				rs = null;
			}
		} else if (op.equals("S.S")) {
			instructionType = InstructionType.S_S;
			String addrPart = parameters[1].trim();
			if (addrPart.contains("(")) {
				offset = Integer.parseInt(addrPart.split("\\(")[0]);
				rs = addrPart.split("\\(")[1].split("\\)")[0];
			} else {
				offset = Integer.parseInt(addrPart);
				rs = null;
			}
		}
		// Integer operations (allow either comma-separated or space-separated forms)
		else if (op.equals("ADDI") || op.equals("SUBI") || op.equals("DADDI") || op.equals("DSUBI")) {
			instructionType = op.equals("ADDI") ? InstructionType.ADDI :
						  op.equals("SUBI") ? InstructionType.SUBI :
						  op.equals("DADDI") ? InstructionType.DADDI : InstructionType.DSUBI;

			// Collect operands after the opcode, tolerating mixed comma/space separators.
			ArrayList<String> ops = new ArrayList<>();
			// firstTokens already has op and maybe rd
			for (int i = 1; i < firstTokens.length; i++) if (!firstTokens[i].isEmpty()) ops.add(firstTokens[i].trim());
			for (int i = 1; i < parameters.length; i++) {
				String part = parameters[i].trim();
				if (!part.isEmpty()) {
					for (String tok : part.split("\\s+")) if (!tok.isEmpty()) ops.add(tok.trim());
				}
			}

			if (ops.size() >= 2) {
				rd = ops.get(0);
				rs = ops.get(1);
				rt = (ops.size() >= 3) ? ops.get(2) : "0"; // default immediate if omitted
			} else {
				throw new IllegalArgumentException("Invalid immediate instruction format (need dest, src, imm): " + s);
			}

			// try to store immediate also in offset when numeric
			try {
				offset = Integer.parseInt(rt);
			} catch (NumberFormatException ignore) {
				// keep offset = -1 when rt is not numeric
			}
		}
		// Branch operations
		else if (op.equals("BEQ")) {
			instructionType = InstructionType.BEQ;
			rs = parameters[0].split("\\s+")[1].trim();
			rt = parameters[1].trim();
			branchLabel = parameters[2].trim();
		} else if (op.equals("BNE")) {
			instructionType = InstructionType.BNE;
			rs = parameters[0].split("\\s+")[1].trim();
			rt = parameters[1].trim();
			branchLabel = parameters[2].trim();
		} else {
			System.out.println("Unknown instruction: " + op);
		}
		
		Instruction inst = new Instruction(instructionType, rs, rt, rd, offset);
		inst.setBranchLabel(branchLabel);
		return inst;
	}

	

	public ArrayList<String> readProgram(File program) throws FileNotFoundException {

		Scanner myReader = new Scanner(program);
		ArrayList<String> prog = new ArrayList<String>();
		while (myReader.hasNextLine()) {

			String data = myReader.nextLine();
			prog.add(data);

		}

		return prog;
	}

}
