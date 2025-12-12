package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import Tomasulo.LoadBuffer;
import Tomasulo.Parser;
import Tomasulo.Processor;
import Tomasulo.Program;
import Tomasulo.Register;
import Tomasulo.Reservation;
import Tomasulo.StoreBuffer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;


public class MainSceneController {
	
	static Processor processor;
	static String filePath;
	
	 	@FXML
	    private TextField addLatency;
	 	
	 	@FXML
	    private Button nextButton;
	 	
	 	@FXML
	    private TextField subLatency;
	 	
	 	@FXML
	    private TextField mulLatency;
	 	
	 	@FXML
	    private TextField divLatency;
	 	
	 	@FXML
	    private TextField storeLatency;
	 	
	 	@FXML
	    private TextField loadLatency;
    
	 	@FXML
	    private TextArea summary;

	    @FXML
	    private TextArea addReservationStation;

	    @FXML
	    private TextArea mulReservationStation;

		@FXML
		private Label resIntID1;

		@FXML
		private Label resIntID2;

		@FXML
		private Label resIntID3;

		@FXML
		private Label resIntBusy1;

		@FXML
		private Label resIntBusy2;

		@FXML
		private Label resIntBusy3;

		@FXML
		private Label resIntOp1;

		@FXML
		private Label resIntOp2;

		@FXML
		private Label resIntOp3;

		@FXML
		private Label resIntVj1;

		@FXML
		private Label resIntVj2;

		@FXML
		private Label resIntVj3;

		@FXML
		private Label resIntVk1;

		@FXML
		private Label resIntVk2;

		@FXML
		private Label resIntVk3;

		@FXML
		private Label resIntQj1;

		@FXML
		private Label resIntQj2;

		@FXML
		private Label resIntQj3;

		@FXML
		private Label resIntQk1;

		@FXML
		private Label resIntQk2;

		@FXML
		private Label resIntQk3;

	    @FXML
	    private TextArea storeBuffer;

	    @FXML
	    private TextArea loadBuffer;

	    @FXML
	    private TextArea registerFile;

		@FXML
		private TextField intAddLatency;

		@FXML
		private TextField intSubLatency;

		@FXML
		private TextField daddLatency;

		@FXML
		private TextField dsubLatency;

		@FXML
		private TextField branchLatency;

		@FXML
		private TextField addStationSize;

		@FXML
		private TextField mulStationSize;

		@FXML
		private TextField intStationSize;

		@FXML
		private TextField loadBufferSize;

		@FXML
		private TextField storeBufferSize;

		@FXML
		private TextField cacheSize;

		@FXML
		private TextField blockSize;

		@FXML
		private TextField cacheHit;

		@FXML
		private TextField cacheMiss;

		@FXML
		private TextField memorySize;

		@FXML
		private ChoiceBox<String> opChoice;

		@FXML
		private ChoiceBox<String> rdChoice;

		@FXML
		private ChoiceBox<String> rsChoice;

		@FXML
		private ChoiceBox<String> rtChoice;

		@FXML
		private TextField addrOffset;

		@FXML
		private TextArea programArea;

		@FXML
		private ChoiceBox<String> regChoice;

		@FXML
		private TextField regValue;

		@FXML
		private TextArea seedArea;

		@FXML
		private RadioButton autofillRadio;

		@FXML
		private RadioButton clearRadio;

		@FXML
		private Label cycleLabel;

	    @FXML
	    private Label resAddID1;

	    @FXML
	    private Label resAddID2;

	    @FXML
	    private Label resAddID3;

	    @FXML
	    private Label resMulID1;

	    @FXML
	    private Label resMulID2;

	    @FXML
	    private Label resMulBusy1;

	    @FXML
	    private Label resMulBusy2;

	    @FXML
	    private Label resMulOp1;

	    @FXML
	    private Label resMulOp2;

	    @FXML
	    private Label resMulVj1;

	    @FXML
	    private Label resMulVj2;

	    @FXML
	    private Label resMulVk1;

	    @FXML
	    private Label resMulVk2;

	    @FXML
	    private Label resMulQj1;

	    @FXML
	    private Label resMulQk1;

	    @FXML
	    private Label resMulQk2;

	    @FXML
	    private Label resMulQj2;

	    @FXML
	    private Label resAddBusy1;

	    @FXML
	    private Label resAddBusy2;

	    @FXML
	    private Label resAddBusy3;

	    @FXML
	    private Label resAddOp1;

	    @FXML
	    private Label resAddOp2;

	    @FXML
	    private Label resAddOp3;

	    @FXML
	    private Label resAddVj1;

	    @FXML
	    private Label resAddVj2;

	    @FXML
	    private Label resAddVj3;

	    @FXML
	    private Label resAddVk1;

	    @FXML
	    private Label resAddVk2;

	    @FXML
	    private Label resAddVk3;

	    @FXML
	    private Label resAddQj1;

	    @FXML
	    private Label resAddQj2;

	    @FXML
	    private Label resAddQj3;

	    @FXML
	    private Label resAddQk1;

	    @FXML
	    private Label resAddQk2;

	    @FXML
	    private Label resAddQk3;

	    @FXML
	    private Label F0;

	    @FXML
	    private Label F1;

	    @FXML
	    private Label F2;

	    @FXML
	    private Label F3;

	    @FXML
	    private Label F4;

	    @FXML
	    private Label F5;

	    @FXML
	    private Label F6;

	    @FXML
	    private Label F7;

	    @FXML
	    private Label F8;

	    @FXML
	    private Label F9;

	    @FXML
	    private Label F10;

	    @FXML
	    private Label F11;

	    @FXML
	    private Label F12;

	    @FXML
	    private Label F13;

	    @FXML
	    private Label F14;

	    @FXML
	    private Label F15;

	    @FXML
	    private Label F16;

	    @FXML
	    private Label F17;

	    @FXML
	    private Label F18;

	    @FXML
	    private Label F19;

	    @FXML
	    private Label F20;

	    @FXML
	    private Label F21;

	    @FXML
	    private Label F22;

	    @FXML
	    private Label F23;

	    @FXML
	    private Label F24;

	    @FXML
	    private Label F25;

	    @FXML
	    private Label F26;

	    @FXML
	    private Label F27;

	    @FXML
	    private Label F28;

	    @FXML
	    private Label F29;

	    @FXML
	    private Label F30;

	    @FXML
	    private Label F31;

	    @FXML
	    private Label F32;

	    @FXML
	    private Label loadID1;

	    @FXML
	    private Label loadID2;

	    @FXML
	    private Label loadID3;

	    @FXML
	    private Label loadBusy1;

	    @FXML
	    private Label loadBusy2;

	    @FXML
	    private Label loadAddr1;

	    @FXML
	    private Label loadAddr2;

	    @FXML
	    private Label loadAddr3;

	    @FXML
	    private Label loadBusy3;

	    @FXML
	    private Label loadQj1;

	    @FXML
	    private Label loadQj2;

	    @FXML
	    private Label loadQj3;

	    @FXML
	    private Label storeID1;

	    @FXML
	    private Label storeID2;

	    @FXML
	    private Label storeID3;

	    @FXML
	    private Label storeBusy1;

	    @FXML
	    private Label storeBusy2;

	    @FXML
	    private Label storeBusy3;

	    @FXML
	    private Label storeQj1;

	    @FXML
	    private Label storeQj2;

	    @FXML
	    private Label storeQj3;

	    @FXML
	    private Label storeAddr1;

	    @FXML
	    private Label storeAddr2;

	    @FXML
	    private Label storeAddr3;

	    @FXML
	    private Label storeVal1;

	    @FXML
	    private Label storeVal2;

	    @FXML
	    private Label storeVal3;
	
	String readFile(String filePath) {
		String fileText = "";
		try {
		      File myObj = new File(filePath);
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        fileText += myReader.nextLine() + "\n";
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		return fileText;
	}
	


    @FXML
    void onLoad(ActionEvent event) {
    	FileChooser fc = new FileChooser();
    	fc.getExtensionFilters().add(new ExtensionFilter("TXT File", "*.txt"));
    	File f = fc.showOpenDialog(null);
    	if(f != null) {
    		filePath = f.toString();
			// Inform user which file was selected and show file size
			if (summary != null) {
				summary.setText("Loaded file: " + filePath + "\n");
				summary.appendText("File size: " + f.length() + " bytes\n");
			}
    	}
    	
    }

	@FXML
	void onAddInstruction(ActionEvent event) {
		if (opChoice == null || programArea == null) return;
		String op = opChoice.getValue();
		if (op == null || op.isEmpty()) return;

		String line = null;
		String rd = rdChoice == null ? null : rdChoice.getValue();
		String rs = rsChoice == null ? null : rsChoice.getValue();
		String rt = rtChoice == null ? null : rtChoice.getValue();
		String off = addrOffset == null ? null : addrOffset.getText();

		// Build instruction string per parser expectations
		if (op.startsWith("ADD") || op.startsWith("SUB") || op.startsWith("MUL") || op.startsWith("DIV")) {
			// Format: OP RD, RS, RT
			if (rd == null || rs == null || rt == null) {
				Alert a = new Alert(AlertType.WARNING, "Please select destination and two sources.");
				a.showAndWait();
				return;
			}
			line = String.format("%s %s, %s, %s", op, rd, rs, rt);
		} else if (op.equals("L.D") || op.equals("L.S") || op.equals("LD") || op.equals("LW")) {
			// Format: L.D RD, offset(base)
			if (rd == null) { rd = "F0"; }
			if (off == null || off.isEmpty()) {
				// offset only form
				line = String.format("%s %s, %s", op, rd, rs == null ? "0" : rs);
			} else {
				String base = rs == null ? "R0" : rs;
				line = String.format("%s %s, %s(%s)", op, rd, off, base);
			}
		} else if (op.equals("S.D") || op.equals("S.S") || op.equals("SW") ) {
			// Format: S.D RS, offset(base)
			if (rs == null) rs = "F0";
			if (off == null || off.isEmpty()) {
				line = String.format("%s %s, %s", op, rs, rt == null ? "0" : rt);
			} else {
				String base = rt == null ? "R0" : rt;
				line = String.format("%s %s, %s(%s)", op, rs, off, base);
			}
		} else if (op.equals("ADDI") || op.equals("SUBI") || op.equals("DADDI") || op.equals("DSUBI")) {
			// Integer immediate: OP RD, RS, IMM (IMM from offset box if provided, else rtChoice, else 0)
			if (rd == null || rs == null) {
				Alert a = new Alert(AlertType.WARNING, "Please select destination and source register for the immediate instruction.");
				a.showAndWait();
				return;
			}
			String imm = (off != null && !off.isEmpty()) ? off : (rt == null ? "0" : rt);
			line = String.format("%s %s, %s, %s", op, rd, rs, imm);
		} else if (op.equals("BEQ") || op.equals("BNE")) {
			if (rs == null || rt == null) {
				Alert a = new Alert(AlertType.WARNING, "Please select two registers for branch comparison and a label in the dst field.");
				a.showAndWait();
				return;
			}
			String label = rd == null ? "0" : rd;
			line = String.format("%s %s, %s, %s", op, rs, rt, label);
		} else {
			// fallback simple two-operand form
			if (rd == null || rs == null) {
				Alert a = new Alert(AlertType.WARNING, "Please select registers for the instruction.");
				a.showAndWait();
				return;
			}
			line = String.format("%s %s, %s", op, rd, rs);
		}

		// Append to program area
		String existing = programArea.getText();
		if (existing == null || existing.isEmpty()) programArea.setText(line + "\n");
		else programArea.setText(existing + line + "\n");
		if (summary != null) summary.appendText("Added instruction: " + line + "\n");
	}

	@FXML
	void onClear(ActionEvent event) {
		Alert confirm = new Alert(AlertType.CONFIRMATION, "Reset will clear the current simulation and cannot be undone. Continue?");
		confirm.setHeaderText("Confirm Reset");
		ButtonType ok = ButtonType.OK;
		ButtonType cancel = ButtonType.CANCEL;
		confirm.getButtonTypes().setAll(ok, cancel);
		confirm.initOwner(null);
		java.util.Optional<ButtonType> res = confirm.showAndWait();
		if (!res.isPresent() || res.get() != ok) {
			return; // user cancelled
		}

		// Reset processor and file path
		processor = null;
		filePath = null;

		// Clear summary and other text areas
		if (summary != null) summary.setText("");
		if (registerFile != null) registerFile.setText("");
		if (addReservationStation != null) addReservationStation.setText("");
		if (mulReservationStation != null) mulReservationStation.setText("");
		if (storeBuffer != null) storeBuffer.setText("");
		if (loadBuffer != null) loadBuffer.setText("");
		// Clear the instruction builder area as well
		if (programArea != null) programArea.setText("");

		// Reset cycle label
		if (cycleLabel != null) cycleLabel.setText("Cycle: 0");

		// Reset reservation station labels
		Label[] resLabels = new Label[] {
			resAddID1, resAddID2, resAddID3,
			resMulID1, resMulID2,
			resAddBusy1, resAddBusy2, resAddBusy3,
			resMulBusy1, resMulBusy2,
			resAddOp1, resAddOp2, resAddOp3,
			resMulOp1, resMulOp2,
			resAddVj1, resAddVj2, resAddVj3,
			resMulVj1, resMulVj2,
			resAddVk1, resAddVk2, resAddVk3,
			resMulVk1, resMulVk2,
			resAddQj1, resAddQj2, resAddQj3,
			resAddQk1, resAddQk2, resAddQk3,
			resMulQj1, resMulQj2, resMulQk1, resMulQk2
			,resIntID1, resIntID2, resIntID3,
			resIntBusy1, resIntBusy2, resIntBusy3,
			resIntOp1, resIntOp2, resIntOp3,
			resIntVj1, resIntVj2, resIntVj3,
			resIntVk1, resIntVk2, resIntVk3,
			resIntQj1, resIntQj2, resIntQj3,
			resIntQk1, resIntQk2, resIntQk3
		};
		for (Label l : resLabels) {
			if (l != null) l.setText("");
		}

		// Reset load/store labels
		Label[] bufLabels = new Label[] { loadID1, loadID2, loadID3, loadBusy1, loadBusy2, loadBusy3,
				loadAddr1, loadAddr2, loadAddr3,
				storeID1, storeID2, storeID3, storeBusy1, storeBusy2, storeBusy3,
				storeQj1, storeQj2, storeQj3,
				storeAddr1, storeAddr2, storeAddr3,
				storeVal1, storeVal2, storeVal3 };
		for (Label l : bufLabels) if (l != null) l.setText("");

		// Reset register labels F0..F31
		Label[] regs = new Label[] { F0,F1,F2,F3,F4,F5,F6,F7,F8,F9,F10,F11,F12,F13,F14,F15,F16,F17,F18,F19,F20,F21,F22,F23,F24,F25,F26,F27,F28,F29,F30,F31 };
		for (Label l : regs) if (l != null) l.setText("0");

		// Disable next button until a program is started
		if (nextButton != null) nextButton.setDisable(true);

		// Clear filePath display in summary (if any)
		if (summary != null) summary.appendText("System reset. Load a program to start.\n");
	}
    
    void updateRegisterInfo() {
    	if (processor == null) return;

    	Register[] rf = processor.getRegisterFile().getFloating();
    	
    	// if(rf[0].getQ() != null) {
		// 	F0.setText(rf[0].getQ().toString());
		// }
    	// else {
    	// 	F0.setText(rf[0].getValue() + "");
    	// }
    	
    	// if(rf[1].getQ() != null) {
		// 	F1.setText(rf[1].getQ().toString());
		// }
    	// else {
    	// 	F1.setText(rf[1].getValue() + "");
    	// }
    	
    	// if(rf[2].getQ() != null) {
		// 	F2.setText(rf[2].getQ().toString());
		// }
    	// else {
    	// 	F2.setText(rf[2].getValue() + "");
    	// }
    	
    	// if(rf[3].getQ() != null) {
		// 	F3.setText(rf[3].getQ().toString());
		// }
    	// else {
    	// 	F3.setText(rf[3].getValue() + "");
    	// }
    	
    	// if(rf[4].getQ() != null) {
		// 	F4.setText(rf[4].getQ().toString());
		// }
    	// else {
    	// 	F4.setText(rf[4].getValue() + "");
    	// }
    	
    	// if(rf[5].getQ() != null) {
		// 	F5.setText(rf[5].getQ().toString());
		// }
    	// else {
    	// 	F5.setText(rf[5].getValue() + "");
    	// }
    	
    	// if(rf[6].getQ() != null) {
		// 	F6.setText(rf[6].getQ().toString());
		// }
    	// else {
    	// 	F6.setText(rf[6].getValue() + "");
    	// }
    	
    	// if(rf[7].getQ() != null) {
		// 	F7.setText(rf[7].getQ().toString());
		// }
    	// else {
    	// 	F7.setText(rf[7].getValue() + "");
    	// }
    	
    	// if(rf[8].getQ() != null) {
		// 	F8.setText(rf[8].getQ().toString());
		// }
    	// else {
    	// 	F8.setText(rf[8].getValue() + "");
    	// }
    	
    	// if(rf[9].getQ() != null) {
		// 	F9.setText(rf[9].getQ().toString());
		// }
    	// else {
    	// 	F9.setText(rf[9].getValue() + "");
    	// }
    	
    	// if(rf[10].getQ() != null) {
		// 	F10.setText(rf[10].getQ().toString());
		// }
    	// else {
    	// 	F10.setText(rf[10].getValue() + "");
    	// }
    	
    	// if(rf[11].getQ() != null) {
		// 	F11.setText(rf[11].getQ().toString());
		// }
    	// else {
    	// 	F11.setText(rf[11].getValue() + "");
    	// }
    	
    	// if(rf[12].getQ() != null) {
		// 	F12.setText(rf[12].getQ().toString());
		// }
    	// else {
    	// 	F12.setText(rf[12].getValue() + "");
    	// }
    	
    	// if(rf[13].getQ() != null) {
		// 	F13.setText(rf[13].getQ().toString());
		// }
    	// else {
    	// 	F13.setText(rf[13].getValue() + "");
    	// }
    	
    	// if(rf[14].getQ() != null) {
		// 	F14.setText(rf[14].getQ().toString());
		// }
    	// else {
    	// 	F14.setText(rf[14].getValue() + "");
    	// }
    	
    	// if(rf[15].getQ() != null) {
		// 	F15.setText(rf[15].getQ().toString());
		// }
    	// else {
    	// 	F15.setText(rf[15].getValue() + "");
    	// }
    	
    	// if(rf[16].getQ() != null) {
		// 	F16.setText(rf[16].getQ().toString());
		// }
    	// else {
    	// 	F16.setText(rf[16].getValue() + "");
    	// }
    	
    	// if(rf[17].getQ() != null) {
		// 	F17.setText(rf[17].getQ().toString());
		// }
    	// else {
    	// 	F17.setText(rf[17].getValue() + "");
    	// }
    	
    	// if(rf[18].getQ() != null) {
		// 	F18.setText(rf[18].getQ().toString());
		// }
    	// else {
    	// 	F18.setText(rf[18].getValue() + "");
    	// }
    	
    	// if(rf[19].getQ() != null) {
		// 	F19.setText(rf[19].getQ().toString());
		// }
    	// else {
    	// 	F19.setText(rf[19].getValue() + "");
    	// }
    	
    	// if(rf[20].getQ() != null) {
		// 	F20.setText(rf[20].getQ().toString());
		// }
    	// else {
    	// 	F20.setText(rf[20].getValue() + "");
    	// }
    	
    	// if(rf[21].getQ() != null) {
		// 	F21.setText(rf[21].getQ().toString());
		// }
    	// else {
    	// 	F21.setText(rf[21].getValue() + "");
    	// }
    	
    	// if(rf[22].getQ() != null) {
		// 	F22.setText(rf[22].getQ().toString());
		// }
    	// else {
    	// 	F22.setText(rf[22].getValue() + "");
    	// }
    	
    	// if(rf[23].getQ() != null) {
		// 	F23.setText(rf[23].getQ().toString());
		// }
    	// else {
    	// 	F23.setText(rf[23].getValue() + "");
    	// }
    	
    	// if(rf[24].getQ() != null) {
		// 	F24.setText(rf[24].getQ().toString());
		// }
    	// else {
    	// 	F24.setText(rf[24].getValue() + "");
    	// }
    	
    	// if(rf[25].getQ() != null) {
		// 	F25.setText(rf[25].getQ().toString());
		// }
    	// else {
    	// 	F25.setText(rf[25].getValue() + "");
    	// }
    	
    	// if(rf[26].getQ() != null) {
		// 	F26.setText(rf[26].getQ().toString());
		// }
    	// else {
    	// 	F26.setText(rf[26].getValue() + "");
    	// }
    	
    	// if(rf[27].getQ() != null) {
		// 	F27.setText(rf[27].getQ().toString());
		// }
    	// else {
    	// 	F27.setText(rf[27].getValue() + "");
    	// }
    	
    	// if(rf[28].getQ() != null) {
		// 	F28.setText(rf[28].getQ().toString());
		// }
    	// else {
    	// 	F28.setText(rf[28].getValue() + "");
    	// }
    	
    	// if(rf[29].getQ() != null) {
		// 	F29.setText(rf[29].getQ().toString());
		// }
    	// else {
    	// 	F29.setText(rf[29].getValue() + "");
    	// }
    	
    	// if(rf[30].getQ() != null) {
		// 	F30.setText(rf[30].getQ().toString());
		// }
    	// else {
    	// 	F30.setText(rf[30].getValue() + "");
    	// }
    	
    	// if(rf[31].getQ() != null) {
		// 	F31.setText(rf[31].getQ().toString());
		// }
    	// else {
    	// 	F31.setText(rf[31].getValue() + "");
    	// }
		// Also write a compact, monospaced register table to the registerFile TextArea
		writeRegisterTable();
	}

	/**
	 * Write a compact 8x4 register table into the `registerFile` TextArea
	 * Uses a monospace font and aligns name:value pairs for readability.
	 */
	/**
	 * Write a compact register table showing both floating (F) and integer (R)
	 * registers into the `registerFile` TextArea. Shows tag (Qi) when present
	 * otherwise shows the numeric value.
	 */
	private void writeRegisterTable() {
		if (registerFile == null || processor == null) return;
		Register[] fr = processor.getRegisterFile().getFloating();
		Register[] ir = processor.getRegisterFile().getInteger();
		registerFile.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 12;");
		StringBuilder sb = new StringBuilder();
		// Header
		sb.append(String.format("%-10s %-12s %-10s %-12s\n", "F-Reg", "Value/Tag", "R-Reg", "Value/Tag"));
		// Print 32 rows with F and R side-by-side
		for (int i = 0; i < 32; i++) {
			String fname = String.format("F%02d:", i);
			String fval = (fr[i].getQ() != null) ? fr[i].getQ().toString() : String.format("%.3f", fr[i].getValue());
			String rname = String.format("R%02d:", i);
			String rval;
			if (ir[i].getQ() != null) {
				rval = ir[i].getQ().toString();
			} else {
				// Display integer registers as integers (visually) even though stored as double
				int intVal = processor.getRegisterFile().getValueInteger(i);
				rval = String.format("%d", intVal);
			}
			sb.append(String.format("%-10s %-12s| %-10s %-12s\n", fname, fval, rname, rval));
		}
		registerFile.setText(sb.toString());
	}
    
    void updateInfo() {
    	if (processor == null) {
    		summary.setText("No program loaded. Please load a file and click Start.\n");
    		return;
    	}

		// Update cycle label
		if (cycleLabel != null) {
			cycleLabel.setText("Cycle: " + processor.cycle);
		}
    	updateRegisterInfo();

    	System.out.println(processor.printCycle());
   	summary.setText(summary.getText() + processor.printCycle() + "\n" + "\n");
    	
    	Reservation[] addReservationStation = processor.getAddStation().getStation();
    	Reservation[] mulReservationStation = processor.getMulStation().getStation();

    	
		LoadBuffer[] loadBuffers = processor.getLoadStation().getStation();
		StoreBuffer[] storeBuffers = processor.getStoreStation().getStation();
    	
    	for (int i = 0; i < addReservationStation.length; i++) {
    		if(i == 0) {
	    		if(addReservationStation[i].getID() != null)
	    		resAddID1.setText(addReservationStation[i].getID().toString());
	    		resAddBusy1.setText(addReservationStation[i].isBusy() + "");
	    		if(addReservationStation[i].getOp() != null)
	    		resAddOp1.setText(addReservationStation[i].getOp().toString());
	    		resAddVj1.setText(addReservationStation[i].getVj() + "");
	    		resAddVk1.setText(addReservationStation[i].getVk() + "");
	    		if(addReservationStation[i].getQj() != null)
	    		resAddQj1.setText(addReservationStation[i].getQj().toString());
	    		else {
	    			resAddQj1.setText("");
	    		}
	    		if(addReservationStation[i].getQk() != null)
	    		resAddQk1.setText(addReservationStation[i].getQk().toString());
	    		else {
	    			resAddQj1.setText("");
	    		}
    		}
    		
    		if(i == 1) {
	        	if(addReservationStation[i].getID() != null)
	    		resAddID2.setText(addReservationStation[i].getID().toString());
	    		resAddBusy2.setText(addReservationStation[i].isBusy() + "");
	    		if(addReservationStation[i].getOp() != null)
	    		resAddOp2.setText(addReservationStation[i].getOp().toString());
	    		resAddVj2.setText(addReservationStation[i].getVj() + "");
	    		resAddVk2.setText(addReservationStation[i].getVk() + "");
	    		if(addReservationStation[i].getQj() != null)
	    		resAddQj2.setText(addReservationStation[i].getQj().toString());
	    		else {
	    			resAddQj2.setText("");
	    		}
	    		if(addReservationStation[i].getQk() != null)
	    		resAddQk2.setText(addReservationStation[i].getQk().toString());
	    		else {
	    			resAddQk2.setText("");
	    		}
    		}
    		
    		if(i == 2) {
	            if(addReservationStation[i].getID() != null)
	    		resAddID3.setText(addReservationStation[i].getID().toString());
	    		resAddBusy3.setText(addReservationStation[i].isBusy() + "");
	    		if(addReservationStation[i].getOp() != null)
	    		resAddOp3.setText(addReservationStation[i].getOp().toString());
	    		resAddVj3.setText(addReservationStation[i].getVj() + "");
	    		resAddVk3.setText(addReservationStation[i].getVk() + "");
	    		if(addReservationStation[i].getQj() != null)
	    		resAddQj3.setText(addReservationStation[i].getQj().toString());
	    		else {
	    			resAddQj3.setText("");
	    		}
	    		if(addReservationStation[i].getQk() != null)
	    		resAddQk3.setText(addReservationStation[i].getQk().toString());
    		}
    		
		}
    	
    	for (int i = 0; i < mulReservationStation.length; i++) {
    		if(i == 0) {
		        if(mulReservationStation[i].getID() != null)
		        resMulID1.setText(mulReservationStation[i].getID().toString());
	    		resMulBusy1.setText(mulReservationStation[i].isBusy() + "");
	            if(mulReservationStation[i].getOp() != null)
	    		resMulOp1.setText(mulReservationStation[i].getOp().toString());
	    		resMulVj1.setText(mulReservationStation[i].getVj() + "");
	    		resMulVk1.setText(mulReservationStation[i].getVk() + "");
	            if(mulReservationStation[i].getQj() != null)
	    		resMulQj1.setText(mulReservationStation[i].getQj().toString());
	            else {
	            	resMulQj1.setText("");
	            }
	            if(mulReservationStation[i].getQk() != null)
	    		resMulQk1.setText(mulReservationStation[i].getQk().toString());
	            else {
	            	resMulQk1.setText("");
	            }
    		}
    		
	    		if(i == 1) {
		            if(mulReservationStation[i].getID() != null)
		    		resMulID2.setText(mulReservationStation[i].getID().toString());
		    		resMulBusy2.setText(mulReservationStation[i].isBusy() + "");
		            if(mulReservationStation[i].getOp() != null)
		    		resMulOp2.setText(mulReservationStation[i].getOp().toString());
		    		resMulVj2.setText(mulReservationStation[i].getVj() + "");
		    		resMulVk2.setText(mulReservationStation[i].getVk() + "");
		            if(mulReservationStation[i].getQj() != null)
		    		resMulQj2.setText(mulReservationStation[i].getQj().toString());
		            else {
		            	resMulQj2.setText("");
		            }
		            if(mulReservationStation[i].getQk() != null)
		    		resMulQk2.setText(mulReservationStation[i].getQk().toString());
		            else {
		            	resMulQk2.setText("");
		            }
    		}
    		
		}
		// Integer reservation station (per-slot labels)
		Reservation[] intReservationStation = processor.getIntegerStation().getStation();
		for (int i = 0; i < intReservationStation.length; i++) {
			if (i == 0) {
				if (intReservationStation[i].getID() != null)
					resIntID1.setText(intReservationStation[i].getID().toString());
				resIntBusy1.setText(intReservationStation[i].isBusy() + "");
				if (intReservationStation[i].getOp() != null)
					resIntOp1.setText(intReservationStation[i].getOp().toString());
				resIntVj1.setText(intReservationStation[i].getVj() + "");
				resIntVk1.setText(intReservationStation[i].getVk() + "");
				if (intReservationStation[i].getQj() != null)
					resIntQj1.setText(intReservationStation[i].getQj().toString());
				else resIntQj1.setText("");
				if (intReservationStation[i].getQk() != null)
					resIntQk1.setText(intReservationStation[i].getQk().toString());
				else resIntQk1.setText("");
			}
			if (i == 1) {
				if (intReservationStation[i].getID() != null)
					resIntID2.setText(intReservationStation[i].getID().toString());
				resIntBusy2.setText(intReservationStation[i].isBusy() + "");
				if (intReservationStation[i].getOp() != null)
					resIntOp2.setText(intReservationStation[i].getOp().toString());
				resIntVj2.setText(intReservationStation[i].getVj() + "");
				resIntVk2.setText(intReservationStation[i].getVk() + "");
				if (intReservationStation[i].getQj() != null)
					resIntQj2.setText(intReservationStation[i].getQj().toString());
				else resIntQj2.setText("");
				if (intReservationStation[i].getQk() != null)
					resIntQk2.setText(intReservationStation[i].getQk().toString());
				else resIntQk2.setText("");
			}
			if (i == 2) {
				if (intReservationStation[i].getID() != null)
					resIntID3.setText(intReservationStation[i].getID().toString());
				resIntBusy3.setText(intReservationStation[i].isBusy() + "");
				if (intReservationStation[i].getOp() != null)
					resIntOp3.setText(intReservationStation[i].getOp().toString());
				resIntVj3.setText(intReservationStation[i].getVj() + "");
				resIntVk3.setText(intReservationStation[i].getVk() + "");
				if (intReservationStation[i].getQj() != null)
					resIntQj3.setText(intReservationStation[i].getQj().toString());
				else resIntQj3.setText("");
				if (intReservationStation[i].getQk() != null)
					resIntQk3.setText(intReservationStation[i].getQk().toString());
				else resIntQk3.setText("");
			}
		}
    	
    	
		// Load buffer columns (ID, Busy, Add.) using existing labels
		for (int i = 0; i < loadBuffers.length; i++) {
			LoadBuffer lbuff = loadBuffers[i];
			String id = (lbuff.getQ() != null) ? lbuff.getQ().toString() : "L" + (i + 1);
			String busy = Boolean.toString(lbuff.isBusy());
			String addr = lbuff.isBusy() ? Integer.toString(lbuff.getA()) : "";

			if (i == 0) { if (loadID1 != null) loadID1.setText(id); if (loadBusy1 != null) loadBusy1.setText(busy); if (loadAddr1 != null) loadAddr1.setText(addr); }
			if (i == 1) { if (loadID2 != null) loadID2.setText(id); if (loadBusy2 != null) loadBusy2.setText(busy); if (loadAddr2 != null) loadAddr2.setText(addr); }
			if (i == 2) { if (loadID3 != null) loadID3.setText(id); if (loadBusy3 != null) loadBusy3.setText(busy); if (loadAddr3 != null) loadAddr3.setText(addr); }
		}
		
		// Store buffer columns (ID, Busy, Add., Val., Q) using existing labels
		for (int i = 0; i < storeBuffers.length; i++) {
			StoreBuffer sbuff = storeBuffers[i];
			String id = "S" + (i + 1);
			String busy = Boolean.toString(sbuff.isBusy());
			String addr = sbuff.isBusy() ? Integer.toString(sbuff.getA()) : "";
			String val = (sbuff.isBusy() && sbuff.getQ() == null) ? Double.toString(sbuff.getV()) : "";
			String qTag = (sbuff.getQ() != null) ? sbuff.getQ().toString() : "";
			String valOrQ = qTag.isEmpty() ? val : qTag;

			if (i == 0) { if (storeID1 != null) storeID1.setText(id); if (storeBusy1 != null) storeBusy1.setText(busy); if (storeAddr1 != null) storeAddr1.setText(addr); if (storeVal1 != null) storeVal1.setText(val); if (storeQj1 != null) storeQj1.setText(valOrQ); }
			if (i == 1) { if (storeID2 != null) storeID2.setText(id); if (storeBusy2 != null) storeBusy2.setText(busy); if (storeAddr2 != null) storeAddr2.setText(addr); if (storeVal2 != null) storeVal2.setText(val); if (storeQj2 != null) storeQj2.setText(valOrQ); }
			if (i == 2) { if (storeID3 != null) storeID3.setText(id); if (storeBusy3 != null) storeBusy3.setText(busy); if (storeAddr3 != null) storeAddr3.setText(addr); if (storeVal3 != null) storeVal3.setText(val); if (storeQj3 != null) storeQj3.setText(valOrQ); }
		}
    	
    }


    @FXML
    void nextCycleClick(MouseEvent event) {
    	if (processor == null) {
    		Alert a = new Alert(AlertType.WARNING, "No program loaded. Please load a file and click Start.");
    		a.showAndWait();
    		return;
    	}

    	boolean issueSuccessful = processor.next1();
    	updateInfo();
   	processor.next2(issueSuccessful);

		// Log cycle advancement to GUI summary
		if (summary != null) {
			summary.appendText("Cycle advanced. Issue successful: " + issueSuccessful + "\n");
			summary.appendText(processor.printCycle() + "\n");
		}
    	if (processor.pc >= processor.program.getInstructionQueue().length && processor.allStationsEmpty()) {
			nextButton.setDisable(true);
			if (summary != null) summary.appendText("Simulation finished.\n");
    	}
    		
    }
    

    @FXML
    void onStart(ActionEvent event) throws FileNotFoundException {
		Parser p = new Parser();
		try {
			ArrayList<String> arr;
			if (filePath != null && !filePath.isEmpty()) {
				arr = p.readProgram(new File(filePath));
			} else if (programArea != null && !programArea.getText().trim().isEmpty()) {
				String text = programArea.getText();
				List<String> lines = Arrays.asList(text.split("\\r?\\n"));
				arr = new ArrayList<String>(lines);
			} else {
				Alert a = new Alert(AlertType.WARNING, "No program provided. Load a file or build instructions first.");
				a.showAndWait();
				return;
			}
			Program program = p.parse(arr);

			    int addLat = Integer.parseInt(addLatency.getText());
			    int subLat = Integer.parseInt(subLatency.getText());
			    int mulLat = Integer.parseInt(mulLatency.getText());
			    int divLat = Integer.parseInt(divLatency.getText());
			    int ldLat = Integer.parseInt(loadLatency.getText());
			    int stLat = Integer.parseInt(storeLatency.getText());

			    int iAdd = Integer.parseInt(intAddLatency.getText());
			    int iSub = Integer.parseInt(intSubLatency.getText());
			    int iDAdd = Integer.parseInt(daddLatency.getText());
			    int iDSub = Integer.parseInt(dsubLatency.getText());
			    int iBranch = Integer.parseInt(branchLatency.getText());

			    int addSize = Integer.parseInt(addStationSize.getText());
			    int mulSize = Integer.parseInt(mulStationSize.getText());
			    int intSize = Integer.parseInt(intStationSize.getText());
			    int ldSize = Integer.parseInt(loadBufferSize.getText());
			    int stSize = Integer.parseInt(storeBufferSize.getText());

			    int cacheSz = Integer.parseInt(cacheSize.getText());
			    int blkSz = Integer.parseInt(blockSize.getText());
			    int cacheHitLat = Integer.parseInt(cacheHit.getText());
			    int cacheMissPen = Integer.parseInt(cacheMiss.getText());
			    int memSz = Integer.parseInt(memorySize.getText());

			    // Use full constructor with UI-configured values
			    processor = new Processor(program, addLat, mulLat, subLat, divLat, ldLat, stLat,
				    addSize, mulSize, intSize, ldSize, stSize,
				    cacheSz, blkSz, cacheHitLat, cacheMissPen,
				    memSz);

			    // Apply integer instruction latencies
			    processor.setIntegerLatencies(iAdd, iSub, iDAdd, iDSub, iBranch);

				// If user selected autofill mode, ensure integer registers are populated
				if (autofillRadio != null && autofillRadio.isSelected()) {
					for (int i = 0; i < 32; i++) {
						processor.getRegisterFile().setValueInteger(i, i);
						processor.getRegisterFile().setQiInteger(i, null);
					}
					if (summary != null) summary.appendText("Autofill: Integer registers populated with default indices.\n");
				}

				// Apply register seeds if provided in the seed area (format: F0=1.23 or R3=10)
				if (seedArea != null && !seedArea.getText().trim().isEmpty()) {
					// If user chose "Use Clear values", zero all registers first (then apply seeds)
					if (clearRadio != null && clearRadio.isSelected()) {
						// Zero floating and integer registers and clear Qi tags
						for (int i = 0; i < 32; i++) {
							processor.getRegisterFile().setValueFloating(i, 0.0);
							processor.getRegisterFile().setQiFloating(i, null);
							processor.getRegisterFile().setValueInteger(i, 0.0);
							processor.getRegisterFile().setQiInteger(i, null);
						}
						if (summary != null) summary.appendText("Seed mode: Clear all registers to 0 before applying seeds.\n");
					} else {
						if (summary != null) summary.appendText("Seed mode: Autofill defaults (keep populated values)\n");
					}
					String[] seedLines = seedArea.getText().split("\\r?\\n");
					for (String ln : seedLines) {
						if (ln == null) continue;
						ln = ln.trim();
						if (ln.isEmpty()) continue;
						String[] parts = ln.split("=");
						if (parts.length != 2) {
							if (summary != null) summary.appendText("Ignoring invalid seed line: " + ln + "\n");
							continue;
						}
						String reg = parts[0].trim();
						String sval = parts[1].trim();
						try {
							double v = Double.parseDouble(sval);
							if (reg.length() >= 2 && (reg.charAt(0) == 'R' || reg.charAt(0) == 'r')) {
								int idx = Integer.parseInt(reg.substring(1));
								processor.getRegisterFile().setValueInteger(idx, v);
								processor.getRegisterFile().setQiInteger(idx, null);
								if (summary != null) summary.appendText(String.format("Seed applied: %s = %d\n", reg, (int)v));
							} else if (reg.length() >= 2 && (reg.charAt(0) == 'F' || reg.charAt(0) == 'f')) {
								int idx = Integer.parseInt(reg.substring(1));
								processor.getRegisterFile().setValueFloating(idx, v);
								processor.getRegisterFile().setQiFloating(idx, null);
								if (summary != null) summary.appendText(String.format("Seed applied: %s = %f\n", reg, v));
							} else {
								if (summary != null) summary.appendText("Unknown register in seed: " + reg + "\n");
							}
						} catch (NumberFormatException nfe) {
							if (summary != null) summary.appendText("Invalid seed value for " + reg + ": " + sval + "\n");
						}
					}
				}

				// Write useful startup info into the GUI summary area
			if (summary != null) {
				summary.appendText("Program loaded: " + program.getInstructionQueue().length + " instructions\n");
				summary.appendText(String.format("Latencies: ADD=%d SUB=%d MUL=%d DIV=%d LOAD=%d STORE=%d\n", addLat, subLat, mulLat, divLat, ldLat, stLat));
				// Report the actual cache/memory values read from the UI (not hard-coded)
				summary.appendText(String.format("Cache: size=%d block=%d hitLatency=%d missPenalty=%d\n", cacheSz, blkSz, cacheHitLat, cacheMissPen));
				summary.appendText("Memory size: " + memSz + "\n");
				if (processor.getCache() != null) {
					summary.appendText(processor.getCache().getCacheStatus() + "\n");
				}
				summary.appendText("Processor initialized. Ready.\n\n");
			}
			// Update cycle label at start
			if (cycleLabel != null) cycleLabel.setText("Cycle: " + processor.cycle);
			updateInfo();
			// Re-enable stepping now that a program is initialized
			if (nextButton != null) nextButton.setDisable(false);
		} catch (FileNotFoundException fnf) {
			Alert a = new Alert(AlertType.ERROR, "Program file not found: " + filePath);
			a.showAndWait();
			if (summary != null) summary.appendText("Program file not found: " + filePath + "\n");
		} catch (NumberFormatException nfe) {
			Alert a = new Alert(AlertType.ERROR, "Invalid latency value. Please enter integer latencies.");
			a.showAndWait();
			if (summary != null) summary.appendText("Invalid latency value: " + nfe.getMessage() + "\n");
		} catch (Exception e) {
			Alert a = new Alert(AlertType.ERROR, "Error while loading program: " + e.getMessage());
			a.showAndWait();
			e.printStackTrace();
			if (summary != null) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				summary.appendText("Error while loading program: " + e.getMessage() + "\n");
				summary.appendText("Stacktrace:\n" + sw.toString() + "\n");
			}
		}

	}

	@FXML
	public void initialize() {
		// Populate operation choices
		if (opChoice != null) {
			opChoice.getItems().addAll(
				"ADD.D", "SUB.D", "MUL.D", "DIV.D",
				"L.D", "S.D", "LD", "LW", "S.S", "L.S",
				"ADDI", "SUBI", "DADDI", "DSUBI", "BEQ", "BNE"
			);
		}
	
		// Populate register choices (floating and integer names)
		if (rdChoice != null) {
			for (int i = 0; i < 32; i++) rdChoice.getItems().add("F" + i);
			for (int i = 0; i < 32; i++) rdChoice.getItems().add("R" + i);
		}
		if (rsChoice != null) {
			for (int i = 0; i < 32; i++) rsChoice.getItems().add("F" + i);
			for (int i = 0; i < 32; i++) rsChoice.getItems().add("R" + i);
		}
		if (rtChoice != null) {
			for (int i = 0; i < 32; i++) rtChoice.getItems().add("F" + i);
			for (int i = 0; i < 32; i++) rtChoice.getItems().add("R" + i);
		}
	
		// sensible defaults
		if (opChoice != null) opChoice.setValue("ADD.D");
		if (rdChoice != null) rdChoice.setValue("F0");
		if (rsChoice != null) rsChoice.setValue("F1");
		if (rtChoice != null) rtChoice.setValue("F2");

		// Populate register seed choice (F0..F31, R0..R31)
		if (regChoice != null) {
			for (int i = 0; i < 32; i++) regChoice.getItems().add("F" + i);
			for (int i = 0; i < 32; i++) regChoice.getItems().add("R" + i);
			regChoice.setValue("F0");
		}

		// Wire up radio buttons for seed behavior and default selection
		if (autofillRadio != null && clearRadio != null) {
			ToggleGroup tg = new ToggleGroup();
			autofillRadio.setToggleGroup(tg);
			clearRadio.setToggleGroup(tg);
			autofillRadio.setSelected(true);
		}

		// defaults for new inputs
		if (intAddLatency != null) intAddLatency.setText("1");
		if (intSubLatency != null) intSubLatency.setText("1");
		if (daddLatency != null) daddLatency.setText("1");
		if (dsubLatency != null) dsubLatency.setText("1");
		if (branchLatency != null) branchLatency.setText("1");

		if (addStationSize != null) addStationSize.setText("3");
		if (mulStationSize != null) mulStationSize.setText("2");
		if (intStationSize != null) intStationSize.setText("3");
		if (loadBufferSize != null) loadBufferSize.setText("3");
		if (storeBufferSize != null) storeBufferSize.setText("3");

		if (cacheSize != null) cacheSize.setText("1024");
		if (blockSize != null) blockSize.setText("32");
		if (cacheHit != null) cacheHit.setText("1");
		if (cacheMiss != null) cacheMiss.setText("10");
		if (memorySize != null) memorySize.setText("2048");

		// seed area initially empty
		if (seedArea != null) seedArea.setText("");

		// Hide original small register labels (we replaced that area with Integer station)
		try {
			if (F0 != null) {
				Label[] smallF = new Label[] { F0,F1,F2,F3,F4,F5,F6,F7,F8,F9,F10,F11,F12,F13,F14,F15,F16,F17,F18,F19,F20,F21,F22,F23,F24,F25,F26,F27,F28,F29,F30,F31 };
				for (Label l : smallF) if (l != null) l.setVisible(false);
			} 
		} catch (Exception e) {
			// ignore if labels not present
		}
	}


	@FXML
	void onAddSeed(ActionEvent event) {
		if (regChoice == null || regValue == null || seedArea == null) return;
		String reg = regChoice.getValue();
		String val = regValue.getText();
		if (reg == null || reg.isEmpty() || val == null || val.isEmpty()) {
			Alert a = new Alert(AlertType.WARNING, "Select a register and enter a value.");
			a.showAndWait();
			return;
		}
		// Append to seed area as REG=VALUE
		seedArea.appendText(reg + "=" + val + "\n");
		if (summary != null) summary.appendText("Seed added: " + reg + " = " + val + "\n");
		regValue.setText("");
	}

	@FXML
	void onClearSeeds(ActionEvent event) {
		if (seedArea != null) seedArea.setText("");
		if (summary != null) summary.appendText("Seeds cleared.\n");
	}

}

