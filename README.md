# Tomasulo Algorithm Simulator

A comprehensive JavaFX-based simulator for the Tomasulo algorithm, implementing out-of-order execution with reservation stations, load/store buffers, and cache memory system.

## Overview

This simulator demonstrates the Tomasulo algorithm for dynamic instruction scheduling in a pipelined processor. It supports floating-point and integer operations, load/store instructions, branches, and includes a configurable cache system with hit/miss detection.

## Features

### ✅ Core Features

- **Tomasulo Algorithm Implementation**
  - Reservation stations for floating-point operations (ADD, SUB, MUL, DIV)
  - Integer reservation station for integer operations (ADDI, SUBI, DADDI, DSUBI)
  - Load and Store buffers with address clash detection
  - Register renaming to handle WAR and WAW hazards
  - Common data bus for result broadcasting

- **Instruction Support**
  - Floating-point operations: `ADD.D`, `SUB.D`, `MUL.D`, `DIV.D`
  - Integer operations: `ADDI`, `SUBI`, `DADDI`, `DSUBI`
  - Load instructions: `L.D`, `L.S`, `LW`, `LD`
  - Store instructions: `S.D`, `S.S`
  - Branch instructions: `BEQ`, `BNE` (with label support)

- **Cache System**
  - Configurable cache size and block size
  - Cache hit latency and miss penalty
  - Block-based memory addressing
  - Write-through cache policy
  - Address clash detection between load and store buffers

- **GUI Features**
  - Cycle-by-cycle execution visualization
  - Real-time display of reservation stations
  - Load/Store buffer status
  - Register file (floating-point and integer)
  - Cache status information
  - Instruction latency configuration

## Project Structure

```
TomasuloSimulation/
├── src/
│   ├── application/
│   │   ├── Main.java                 # JavaFX application entry point
│   │   ├── MainScene.fxml            # GUI layout
│   │   └── MainSceneController.java  # GUI controller
│   └── Tomasulo/
│       ├── Processor.java            # Main processor logic
│       ├── Cache.java                # Cache implementation
│       ├── Memory.java               # Main memory
│       ├── RegisterFile.java         # Register file (FP + Integer)
│       ├── ReservationStation.java   # Reservation station
│       ├── LoadBuffers.java          # Load buffer manager
│       ├── StoreBuffers.java         # Store buffer manager
│       ├── Bus.java                  # Common data bus
│       ├── Instruction.java          # Instruction representation
│       ├── Parser.java               # Instruction parser
│       ├── Program.java              # Program container
│       ├── InstructionType.java      # Instruction type enum
│       ├── Op.java                   # Operation enum
│       ├── StationType.java          # Station type enum
│       └── ReservationID.java       # Reservation identifier
├── Tests/                            # Test program files
└── README.md                         # This file
```

## Building and Running

### Prerequisites

- Java JDK 11 or higher
- JavaFX SDK (included in JDK 11+ or download separately)

### Compilation

```bash
# Compile the project
javac --module-path /path/to/javafx/lib --add-modules javafx.controls src/application/*.java src/Tomasulo/*.java -d bin/

# Or use your IDE's build system
```

### Running

```bash
# Run the application
java --module-path /path/to/javafx/lib --add-modules javafx.controls -cp bin application.Main

# Or run from your IDE
```

## Usage

### 1. Configure Instruction Latencies

Before starting simulation, enter the latency (in cycles) for each instruction type:
- **Add Latency**: Cycles for ADD.D operations
- **Sub Latency**: Cycles for SUB.D operations
- **Multiply Latency**: Cycles for MUL.D operations
- **Divide Latency**: Cycles for DIV.D operations
- **Load Latency**: Cycles for load operations (includes cache access)
- **Store Latency**: Cycles for store operations (includes cache access)

### 2. Load a Program

1. Click **"Load File"** button
2. Select a text file containing MIPS assembly instructions
3. Instructions should be in the format:
   ```
   L.D F0, 8(R1)
   ADD.D F2, F0, F4
   MUL.D F6, F2, F8
   S.D F6, 16(R1)
   ```

### 3. Start Simulation

1. Click **"Start"** button to initialize the processor
2. Click **"Next Cycle"** to advance simulation one cycle at a time
3. Observe the changes in:
   - Reservation stations (Add, Multiply, Integer)
   - Load/Store buffers
   - Register file values
   - Cycle summary

### 4. Instruction Format

#### Floating-Point Operations
```
ADD.D Fd, Fs, Ft    # Fd = Fs + Ft
SUB.D Fd, Fs, Ft    # Fd = Fs - Ft
MUL.D Fd, Fs, Ft    # Fd = Fs * Ft
DIV.D Fd, Fs, Ft    # Fd = Fs / Ft
```

#### Integer Operations
```
ADDI Rd, Rs, imm    # Rd = Rs + immediate
SUBI Rd, Rs, imm    # Rd = Rs - immediate
DADDI Rd, Rs, imm   # Rd = Rs + immediate (double)
DSUBI Rd, Rs, imm   # Rd = Rs - immediate (double)
```

#### Load Instructions
```
L.D Fd, offset(Rs)  # Load double to Fd from address offset+Rs
L.S Fd, offset(Rs)  # Load single to Fd from address offset+Rs
LW Rd, offset(Rs)   # Load word to Rd from address offset+Rs
LD Rd, offset(Rs)   # Load double to Rd from address offset+Rs
```

#### Store Instructions
```
S.D Fs, offset(Rd)  # Store Fs to address offset+Rd
S.S Fs, offset(Rd)  # Store single Fs to address offset+Rd
```

#### Branch Instructions
```
BEQ Rs, Rt, label   # Branch if Rs == Rt
BNE Rs, Rt, label   # Branch if Rs != Rt
```

#### Labels
```
LOOP: L.D F0, 8(R1)
      MUL.D F4, F0, F2
      S.D F4, 8(R1)
      DSUBI R1, R1, 8
      BNE R1, R2, LOOP
```

## Test Cases

The `Tests/` directory contains several test programs:

- `sampleProgram.txt` - Basic floating-point operations
- `sampleProgram2.txt` - Simple load and multiply
- `sampleProgram3.txt` - Sequential code with dependencies
- `sampleProgram4.txt` - Complex dependency chain
- `oneAdd.txt`, `oneSub.txt`, `oneMul.txt`, `oneDiv.txt` - Single operation tests
- `oneLoad.txt`, `oneStore.txt` - Single memory operation tests
- `latencyTest1.txt`, `latencyTest2.txt` - Latency testing
- `dependencyTest1.txt` - Dependency testing
- `StationTest.txt` - Station capacity testing

### Example Test Case 1: Sequential Code
```
L.D F6, 0(R2)
L.D F2, 8(R2)
MUL.D F0, F2, F4
SUB.D F8, F2, F6
DIV.D F10, F0, F6
ADD.D F6, F8, F2
S.D F6, 8(R2)
```

### Example Test Case 2: Loop Code
```
DADDI R1, R1, 24
DADDI R2, R2, 0
LOOP: L.D F0, 8(R1)
      MUL.D F4, F0, F2
      S.D F4, 8(R1)
      DSUBI R1, R1, 8
      BNE R1, R2, LOOP
```

## Architecture Details

### Reservation Stations

- **Add Station**: Handles ADD.D and SUB.D operations
- **Multiply Station**: Handles MUL.D and DIV.D operations
- **Integer Station**: Handles ADDI, SUBI, DADDI, DSUBI operations

Each station contains:
- **ID**: Reservation identifier
- **Busy**: Whether the station is occupied
- **Op**: Operation to perform
- **Vj, Vk**: Source operand values
- **Qj, Qk**: Source reservation IDs (if operands not ready)

### Load/Store Buffers

- **Load Buffers**: Track pending load operations
- **Store Buffers**: Track pending store operations
- **Address Clash Detection**: Prevents execution when addresses overlap

### Cache System

- **Block-based addressing**: Memory divided into blocks
- **Hit/Miss detection**: Determines cache access latency
- **Write-through policy**: Stores immediately write to memory
- **Configurable parameters**:
  - Cache size (bytes)
  - Block size (bytes)
  - Hit latency (cycles)
  - Miss penalty (cycles)

### Register File

- **Floating-Point Registers**: F0-F31
- **Integer Registers**: R0-R31
- **Register Renaming**: Qi field tracks which reservation station will write to the register

### Common Data Bus

- Single bus for result broadcasting
- Conflict resolution: Instruction with most dependencies publishes first
- All stations monitor the bus for needed values

## Requirements Satisfied

✅ GUI simulator with JavaFX and tables  
✅ Accepts input from text files  
✅ Step-by-step (cycle-by-cycle) execution  
✅ Shows reservation stations, buffers, register file, cache, and queue  
✅ Handles all code types with/without loops  
✅ Handles RAW, WAR, and WAW hazards  
✅ ALU ops (FP add, sub, multiply, divide)  
✅ Integer operations (ADDI, SUBI for loops)  
✅ Loads and stores (LW, LD, L.S, L.D, S.D, S.S)  
✅ User-configurable instruction latencies  
✅ Cache with hit latency and miss penalty  
✅ Cache block size and cache size configuration  
✅ Address clash detection  
✅ Branch instructions (BEQ, BNE)  
✅ No branch prediction  
✅ Configurable station and buffer sizes  
✅ Register file with integer and floating-point registers  
✅ Bus conflict resolution (dependency-based priority)

## Limitations and Notes

1. **Cache Implementation**: Uses a simple FIFO eviction policy. More sophisticated policies (LRU, etc.) can be added.

2. **Memory Addressing**: Memory is byte-addressable. Load/store operations handle 4-byte (word/single) and 8-byte (double) data.

3. **Branch Prediction**: No branch prediction is implemented (as per requirements). Branches wait for register values.

4. **GUI Configuration**: Cache and station sizes are currently hardcoded in the controller. Full GUI configuration can be added.

5. **Register Pre-loading**: Register file structure supports pre-loading, but GUI interface is not yet implemented.

## Troubleshooting

### Common Issues

1. **"Unknown instruction" error**: Check instruction format matches the supported syntax
2. **Branch not working**: Ensure branch labels are defined before use
3. **Cache not updating**: Check that load/store operations are completing execution
4. **Address clash**: Loads and stores to overlapping addresses will stall until resolved

## Future Enhancements

- [ ] Full GUI configuration for cache and station sizes
- [ ] Register file pre-loading interface
- [ ] Instruction construction via GUI (select lists)
- [ ] More sophisticated cache replacement policies
- [ ] Performance statistics (CPI, cache hit rate, etc.)
- [ ] Export cycle-by-cycle trace to file

## Authors

Tomasulo Algorithm Simulator - Group Project

## License

This project is for educational purposes.

