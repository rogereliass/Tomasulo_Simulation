package Tomasulo;

public enum InstructionType {
	LOAD, STORE, ADD, DIV, SUB, MUL,
	L_D, L_S, S_D, S_S, LW, LD, // Load/Store variants
	ADDI, SUBI, DADDI, DSUBI, // Integer instructions
	BEQ, BNE // Branch instructions
}


