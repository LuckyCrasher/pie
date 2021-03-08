package prj.test.framework;

import jdk.nashorn.internal.objects.annotations.Function;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class ClsMain {

	// FRAMEWORK RELATED CONSTANTS AND VARIABLES

	private static final HashMap<Integer, int[][]> aiGrauWert = new HashMap<>();
	private static final HashMap<Integer, HashMap<String, Object>> hmLayer = new HashMap<>();
	private static int iCurrLayer = 0;
	private static int iSizeX;
	private static int iSizeY;
	private static boolean bFileSaved = true;
	private static String sFileNameSave = "";
	private static String sFileNameLoad = "";
	private static boolean bFileLoaded = false;
	private static String sInputOpt = "";
	private static String sLastAction = "";
	private static boolean useVT100 = false;
	private static final Scanner sc = new Scanner(System.in);
	private static final String sFsSeparator = System.getProperty("file.separator");

	// FRAMEWORK RELATED CONSTANTS AND VARIABLES ------------- END
	// FRAMEWORK RELATED METHODS

	private static void fnShowImage(){
		JFrame frame = new JFrame("Image Viewer");
		frame.setSize(iSizeX, iSizeY);

		Image img = fnGetImage();

		ImageIcon imgIcon = new ImageIcon(img);
		JLabel lbl = new JLabel();
		lbl.setIcon(imgIcon);
		frame.getContentPane().add(lbl, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		sLastAction += "Bild gezeigt";
	}

	private static String fnFileDialog(boolean mode){
		final JDialog jd = new JDialog();
		jd.setModal(true);
		jd.setAlwaysOnTop(true);
		jd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		final JFileChooser fc = new JFileChooser("."+ sFsSeparator);
		jd.setVisible(false);
		int userSelection;

		FileFilter filter = new FileFilter() {
			public String getDescription() {
				return "Portable GreyMap (*.pgm)";
			}
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".pgm");
				}
			}
		};
		fc.setFileFilter(filter);

		if (mode) {
			fc.setDialogTitle("Save File");
			userSelection = fc.showSaveDialog(jd);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File filePath = fc.getSelectedFile();
				String path = filePath.getAbsolutePath();
				System.out.println(path);
				if (!path.endsWith(".pgm")){
					path += ".pgm";
				}
				return path;
			}

		} else {
			fc.setDialogTitle("Open File");
			userSelection = fc.showOpenDialog(jd);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File filePath = fc.getSelectedFile();
				String path = filePath.getAbsolutePath();
				System.out.println(path);
				if (!path.endsWith(".pgm")){
					return null;
				}
				return path;
			}
		}
		return null;
	}

	private static Image fnGetImage() {
		BufferedImage image = new BufferedImage(iSizeX, iSizeY, BufferedImage.TYPE_INT_RGB);
		for (int y=0; y<=iSizeY-1; y++) {
			for (int x=0; x<=iSizeX-1; x++){
				Color color = new Color(aiGrauWert.get(iCurrLayer)[y][x],
						aiGrauWert.get(iCurrLayer)[y][x],
						aiGrauWert.get(iCurrLayer)[y][x]);
				image.setRGB(x, y, color.getRGB());
			}
		}
		return image;
	}

	private static void fnSaveToFile(){
		String sFileNamePath = fnFileDialog(true);

		if (sFileNamePath == null){
			sLastAction += "Abgebrochen?";
			return;
		}

		System.out.printf("Datei wird gespeichert nach %s %n", sFileNamePath);
		try {
			File saveFile = new File(sFileNamePath);
			BufferedWriter w = new BufferedWriter(new PrintWriter(saveFile));

			w.write("P2" + System.lineSeparator());
			w.write("# CREATOR: The GIMP's PNM Filter Version 1.0" + System.lineSeparator());

			w.write("400 300" + System.lineSeparator());
			w.write("255" + System.lineSeparator());

			for (int[] line : aiGrauWert.get(iCurrLayer)) {
				for (int val : line) {
					w.write(val + System.lineSeparator());
				}
			}
			w.flush();
			w.close();
			sFileNameSave = sFileNamePath;
			sLastAction = "Bild gespeichert";
			bFileSaved = true;
		} catch (IOException e) {
			sLastAction += "Beim schreiben der Datei ist ein fehler aufgetreten\n";
			//sLastAction += Arrays.toString(e.getStackTrace());
			sLastAction += "Vielleicht war der Name der Datei fehlerhaft\n";
		}
	}

	private static void fnLoadFromFile(String sFilePath) {
		String sFileNamePath;
		if (sFilePath != null){
			sFileNamePath = sFilePath;
		} else {
			sFileNamePath = fnFileDialog(false);
			if (sFileNamePath == null) {
				sLastAction = "Abgebrochen?";
				return;
			}
		}

		String sTemp;
		String line;
		try {
			File file = new File(sFileNamePath);
			System.out.printf("Reading %s %n", file);
			BufferedReader reader = new BufferedReader(new FileReader(file));

			String[] asHeader = new String[4];
			for (int i = 0; i < 4; i++) {
				line = reader.readLine();
				asHeader[i] = line;
				//System.out.println(line);
			}

			sTemp = asHeader[asHeader.length - 2];
			asHeader = sTemp.split(" ");
			iSizeX = Integer.parseInt(asHeader[0]);
			iSizeY = Integer.parseInt(asHeader[1]);
			sLastAction += String.format("Größe des Bildes: X: %d Y: %d %n", iSizeX, iSizeY);
			aiGrauWert.put(iCurrLayer, new int[iSizeY][iSizeX]);

			for (int y = 0; y <= iSizeY - 1; y++) {
				for (int x = 0; x <= iSizeX - 1; x++) {
					line = reader.readLine();
					aiGrauWert.get(iCurrLayer)[y][x] = Integer.parseInt(line);
				}
			}
			reader.close();

			HashMap<String, Object> hmTemp = new HashMap<>();
			hmTemp.put("iSizeX", iSizeX);
			hmTemp.put("iSizeY", iSizeY);
			hmTemp.put("bFileSaved", true);
			hmTemp.put("sFileNameSave", "");
			hmTemp.put("sFileNameLoad", "");
			hmTemp.put("bFileLoaded", false);
			hmTemp.put("iPosArray", iCurrLayer);
			hmLayer.put(iCurrLayer, hmTemp);

			sFileNameLoad = sFileNamePath;
			bFileLoaded = true;
			sLastAction = "Bild geladen";
		} catch (IOException e) {
			sLastAction += "Beim lesen der Datei ist ein fehler aufgetreten";
			//sLastAction += Arrays.toString(e.getStackTrace());
			sLastAction += "Vielleicht war der Name der Datei fehlerhaft";
		}
	}

	private static void fnClear() {
		if (useVT100) {
			char escCode = 0x1B;
			int row = 0;
			int column = 0;
			System.out.printf("%c[%d;%df", escCode, row, column);
			System.out.printf("%c[2%c", escCode, 'J');
		} else {
			for (int i = 0; i < 25*2; i++) {
				System.out.println();
			}
		}
	}

	// FRAMEWORK RELATED METHODS ------------- END
	// INTERFACE RELATED METHODS

	private static void fnTitle(){
		int iRand = ThreadLocalRandom.current().nextInt(1,4);
		String title1 = ""+
				"     ###### #### ###### \n" +
				"     ##   ## ##  ##       \n" +
				"     ######  ##  ####   \n" +
				"     ##      ##  ##       \n" +
				"     ##     #### ###### ";

		String title2 = ""+
				"     ██████╗  ██╗ ███████╗\n" +
				"     ██╔══██╗ ██║ ██╔════╝\n" +
				"     ██████╔╝ ██║ █████╗  \n" +
				"     ██╔═══╝  ██║ ██╔══╝  \n" +
				"     ██║      ██║ ███████╗\n" +
				"     ╚═╝      ╚═╝ ╚══════╝";

		String title3 = ""+
				"       _____ _____ ______ \n" +
				"      |  __ \\\\_   _|  ____|\n" +
				"      | |__) || | | |__   \n" +
				"      |  ___/ | | |  __|  \n" +
				"      | |    _| |_| |____ \n" +
				"      |_|   |_____|______|";
		switch (iRand) {
			case 1: System.out.println(title1);break;
			case 2: System.out.println(title2);break;
			case 3: System.out.println(title3);break;
		}
	}

	private static int[][] fnMenuArea(int[] newPoint0, int[] newPoint1) {
		int[] oldPoint0 = newPoint0;
		int[] oldPoint1 = newPoint1;

		sLastAction = "Stelle den Bereich ein";
		while(true) {
			String[][] asActions = new String[][]{
					{"Um Punkt A zu ändern drücke:", "A"},
					{"Um Punkt B zu ändern drücke:", "B"},
					{"Um Das ganze Bild zu wählen drücke:", "C"},
					{"", ""},
					{"Zum bestätigen drücke", "J"},
					{"Zurück:", "Z"}
			};
			String[][] asTooltip = {
					{"Bereich (X):", Arrays.toString(newPoint0)},
					{"Bereich (Y):", Arrays.toString(newPoint1)}
			};
			fnDrawMenu(asActions, true, asTooltip);
			char cOption = fnUserInput();
			switch (cOption) {
				case 'a':
					sLastAction = "Gib neue Werte für Punkt A an:";
					sInputOpt = "(0,0 - 400,300) A:";
					fnDrawMenu(asActions, true, asTooltip);
					String sPointA = sc.nextLine();
					if(!Character.isDigit(sPointA.charAt(0))|!sPointA.contains(",")) break;
					newPoint0 = fnGetPtFromStr(sPointA);
					break;
				case 'b':
					sLastAction = "Gib neue Werte für Punkt B an:";
					sInputOpt = "(0,0 - 400,300) B:";
					fnDrawMenu(asActions, true, asTooltip);
					String sPointB = sc.nextLine();
					if(!Character.isDigit(sPointB.charAt(0))|!sPointB.contains(",")) break;
					newPoint1 = fnGetPtFromStr(sPointB);
					break;
				case 'c':
					sLastAction = "Ganze Bild Gewählt";
					newPoint0 = new int[]{0, 0};
					newPoint1 = new int[]{iSizeX, iSizeY};
					break;
				case 'z':
					return new int[][]{oldPoint0, oldPoint1};
				case 'j':
					return new int[][]{newPoint0, newPoint1};
				default:
					sLastAction = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static void fnMenuLighten(){
		int iAmountL = 0;
		int [] point0; int[] point1;
		point0 = new int[]{0, 0};
		point1 = new int[]{iSizeX,iSizeY};

		while (true) {
			String[][] asActions = {
					{"Um die Helligkeit einzustellen drücke:", "H"},
					{"Um den Bereich zu Ändern drücke:", "A"},
					{"", ""},
					{"Um zu bestätigen drücke:", "J"},
					{"Zurück", "Z"},
			};

			String[][] asTooltip = {
					{"Neue Helligkeit:", iAmountL + "%"},
					{"Bereich (X):", Arrays.toString(point0)},
					{"Bereich (Y):", Arrays.toString(point1)}
			};

			fnDrawMenu(asActions, true, asTooltip);
			char cLightenOpt = fnUserInput();
			switch (cLightenOpt){
				case 'h':
					sLastAction = "Ändere die Helligkeit";
					asActions = new String[][]{
							{"Gib eine Prozentzahl ein:", "-100% - 100%"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Helligkeit:", iAmountL+"%"}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUi = sc.nextLine().toLowerCase(Locale.ROOT);
					if (sUi.equals("")) break;
					if (sUi.contains("%")) sUi = sUi.replace("%", "");
					if (!Character.isDigit(sUi.charAt(0))) break;
					iAmountL = Integer.parseInt(sUi);
					break;
				case 'a':
					int[][] points = fnMenuArea(point0, point1);
					point0 = points[0];
					point1 = points[1];
					break;
				case 'z':
					return;
				case 'b':
				case 'j':
					fnLighten(iAmountL, point0, point1);
					sLastAction += "Bild Erhellt";
					return;
				default:
					sLastAction = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static void fnMenuShowInfo() {
		if (bFileLoaded) {
			StringBuilder sTmp = new StringBuilder();
			sTmp.append(String.format("Size:     %dx%d\n", iSizeX, iSizeY));
			sTmp.append(String.format("Original Image Path:%n %s\n", sFileNameLoad));
			if (!sFileNameSave.equals("")) sTmp.append(String.format("Saved to: %s\n", sFileNameSave));
			sTmp.append("Bild Infos gezeigt");
			sLastAction += sTmp.toString();
		} else {
			sLastAction += "Du musst zuerst eine Datei laden!!!";
		}
	}

	private static void fnCreateMaskString(double[][] mask){
		int iSize = mask.length;
		if (iSize >17){
			sLastAction = "Maske zu groß zum Anzeigen";
			String[][] asActions = new String[][]{
					{"Dennoch fortfahren", "J"},
					{"Abbrechen", "A"}
			};
			fnDrawMenu(asActions, false, null);
			char cOption = sc.nextLine().charAt(0);
			if (cOption == 'a') return;
		}
		StringBuilder sMask = new StringBuilder();

		sLastAction = "";
		//Top border
		sMask.append("  ┏");
		char c = 65;
		for (int i=1; i<=iSize-1; i++,c++) {
			sMask.append(String.format("━%c━",c));
			sMask.append("┯");
		}
		sMask.append(String.format("━%c━", c));
		sMask.append("┓\n");

		//Middle Fill
		c = 65;
		sMask.append(String.format(" %c┃",c)); c++;
		for (int i=0;i<=iSize-2;i++){sMask.append("   │");}
		sMask.append("   ┃\n");
		for (int i=0;i<=iSize-2;i++,c++) {
			sMask.append("  ┣");
			for (int j = 0; j <= iSize-2; j++) sMask.append("───┼");
			sMask.append(String.format("───┨\n %c┃", c));
			for (int j = 0; j <= iSize - 2; j++) sMask.append("   │");
			sMask.append("   ┃\n");
		}

		//Bottom border
		sMask.append("  ┗");
		for (int i=1; i<=iSize-1; i++) {
			sMask.append("━━━");
			sMask.append("┷");
		}
		sMask.append("━━━");
		sMask.append("┛\n");

		//replace all empty space with NUMBERS
		int ind;
		for (double[] line: mask){
			for (double val: line){
				ind = sMask.indexOf("   ");
				sMask.replace(ind, ind+3, String.format("%3.1f", val));
			}
		}


		System.out.println(sMask.toString());
		sLastAction += sMask.toString();
	}

	private static void fnMenuBlur() {
		int iAmountB = 1;
		int iSizeB = 3;
		int[] point0 = {0,0};
		int[] point1 = {iSizeX, iSizeY};
		String sBlurFunc = "Mean Blur";
		while (true){
			String[][] asActions = {
					{"Um die \"Blur Function\" zu ändern drücke", "F"},
					{"Um die Blur stärke einzustellen drücke", "W"},
					{"Um die größe der Maske zu verändern drücke", "M"},
					{"Um einen Bereich zu wählen drücke", "A"},
					{"", ""},
					{"Um zu Bestätigen drücke", "J"},
					{"Zurück", "Z"},
			};

			String[][] asTooltip = {
					{"Blur stärke:", String.valueOf(iAmountB)},
					{"Blur Box Größe:", String.format("%dx%d", iSizeB, iSizeB)},
					{"Blur Funktion:", sBlurFunc},
					{"Bereich (X):", Arrays.toString(point0)},
					{"Bereich (Y):", Arrays.toString(point1)}
			};

			fnDrawMenu(asActions, true, asTooltip);
			char cBlurOpt = fnUserInput();
			switch (cBlurOpt){
				case 'f':
					sLastAction = "Wähle eine Funktion";
					boolean subMenF = true;
					while(subMenF) {
						asActions = new String[][]{
								{"Für einen \"Gaussian Blur\" drücke", "G"},
								{"Für einen \"Mean (Box) Blur\" drücke", "M"}
						};
						fnDrawMenu(asActions, false, null);
						char cBlurFn = fnUserInput();
						switch (cBlurFn) {
							case 'g':
								sBlurFunc = "Gaussian Blur";
								subMenF = false;
								break;
							case 'm':
								sBlurFunc = "Mean Blur";
								subMenF = false;
								break;
							default:
								sLastAction = "Du musst schon eine Funktion auswählen";
						}
					}
					break;
				case 'm':
					sLastAction = "Gib eine Größe ein";
					sInputOpt = "0 - 100";
					asActions = new String[][]{
							{"Gib eine Ganzzahl ein:", "1 - 100"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Größe:", iSizeB+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUiS = sc.nextLine().toLowerCase(Locale.ROOT);
					if (!Character.isDigit(sUiS.charAt(0))) break;
					iSizeB = Integer.parseInt(sUiS);
					break;
				case 'w':
					sLastAction = "Gib eine Stärke ein";
					sInputOpt = "0 - 100";
					asActions = new String[][]{
							{"Gib eine Ganzzahl ein:", "1 - 100"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Stärke:", iAmountB+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUiA = sc.nextLine().toLowerCase(Locale.ROOT);
					if (!Character.isDigit(sUiA.charAt(0))) break;
					iAmountB = Integer.parseInt(sUiA);
					break;
				case 'a':
					int[][] points = fnMenuArea(point0, point1);
					point0 = points[0];
					point1 = points[1];
					break;
				case 'z':
					return;
				case 'b':
				case 'j':
					if (sBlurFunc.equals("Mean Blur")){ fnBlurMean(iAmountB, iSizeB, point0, point1);}
					else fnBlurGaussian(iAmountB, iSizeB);
					sLastAction += "Bild verwischt";
					return;
			}

		}
	}

	private static void fnMenuInvert() {
		String[][] asActions = {
				{"Um einen Bereich auszuwählen drücke", "A"},
				{"Bestätigen", "J"},
				{"Zurück", "Z"}
		};
		int [] point0; int[] point1;
		point0 = new int[]{0, 0};
		point1 = new int[]{iSizeX,iSizeY};
		sLastAction = "Bild Invertieren";
		while (true) {
			String[][] asTooltip = {
					{"Bereich (X):", Arrays.toString(point0)},
					{"Bereich (Y):", Arrays.toString(point1)}
			};
			fnDrawMenu(asActions, true, asTooltip);
			char cOption = fnUserInput();
			switch (cOption) {
				case 'a':
					int[][] points = fnMenuArea(point0, point1);
					point0 = points[0];
					point1 = points[1];
					break;
				case 'z':
					return;
				case 'b':
				case 'j':
					fnInvert(point0, point1);
					sLastAction = "Bild Invertiert";
					return;
				default:
					sLastAction = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static boolean fnMenuQuit() {
		if (bFileSaved) {
			return true;
		}
		sLastAction = "Du hast noch nicht gespeichert!!";
		String[][] asActions = {
				{"Um zu speichern drücke", "S"},
				{"Um Abzubrechen drücke", "A"},
				{"Um trotzdem zu schließen drücke", "N"},
		};
		while (true) {
			fnDrawMenu(asActions, false, null);
			char ans = fnUserInput();
			switch (ans) {
				case 's': {
					fnSaveToFile();
					return true;
				}
				case 'n': {
					return true;
				}
				case 'a': {
					return false;
				}
				default: {
					sLastAction = "Du musst schon Ja, Nein oder Abbrechen antworten";
				}
			}
		}
	}

	private static void fnDrawMenu(String[][] asActions, boolean bShowTooltip, String[][] asTooltip) {
		fnClear();
		fnTitle();
		StringBuilder sOut = new StringBuilder();
		int columns = 80;

		// Top Line
		sOut.append("┏");
		for (int width=1;width<=10;width++) sOut.append("━");
		sOut.append("(PGM IMAGE EDITOR)"); // 18 chars
		if (bShowTooltip) {
			//TODO add Tooltip title
			for (int width=28;width<=33;width++) sOut.append("━");
			sOut.append("┯");
			for (int width=35;width<=columns-1;width++) sOut.append("━");
		} else {
			for (int width=28;width<=columns-1;width++) sOut.append("━");
		}
		sOut.append("┓\n");

		String[] sStatus = sLastAction.split("\n");
		if (bShowTooltip) {
			int lenTooltip = asTooltip.length;
			for (int line=0;line<=lenTooltip-1;line++) {
				sOut.append("┃");
				if (!sStatus[0].equals("")&&!(line>sStatus.length-1)) {
					sOut.append(String.format(" %-33s│", sStatus[line]));
				} else {
					sOut.append(String.format(" %-33s│", " "));
				}
				sOut.append(String.format(" %-24s", asTooltip[line][0]));

				sOut.append(String.format(" %-19s", asTooltip[line][1]));
				sOut.append("┃\n");
			}
		} else {
			for (String line : sStatus) {
				sOut.append("┃");
				sOut.append(String.format(" %-79s┃%n", line));
			}
		}
		sOut.append("┠");
		if (bShowTooltip){
			for (int width=1;width<=34;width++) sOut.append("─");
			sOut.append("┴");
			for (int width=35;width<=columns-1;width++) sOut.append("─");
		} else {
			for (int width=1;width<=columns;width++) sOut.append("─");
		}

		sOut.append("┨").append("\n");

		for (String[] sAction : asActions){
			sOut.append("┃");
			if (sAction[0].equals("")) {
				for (int k=0;k<=columns-1;k++) sOut.append(" ");
				sOut.append("┃\n");
			} else {
				sOut.append(String.format(" %-56s %21s ┃%n", sAction[0], "'"+sAction[1]+"'"));
			}
		}

		sOut.append("┗");
		for (int width=1;width<=columns;width++) sOut.append("━");
		sOut.append("┛\n");

		System.out.print(sOut.toString());
		System.out.print(sInputOpt+" ->");
		sInputOpt = "";
	}

	private static void fnMenuSettings(){
		sLastAction = "Einstellungen werden nicht bis zum nächsten start gespeichert!";
		String sVt100;
		while (true) {

			if (useVT100) {
				sVt100 = "C: [AN]/AUS";
			} else {
				sVt100 = "C: AN/[AUS]";
			}
			String[][] asActions = new String[][]{
					{"\"VT100 Control chars\" benutzen", sVt100},
					{"", ""},
					{"Zurück", "Z"}
			};
			fnDrawMenu(asActions, false, null);
			char cOption = fnUserInput();
			switch (cOption) {
				case 'c':
					if (useVT100) {
						sLastAction = "VT100 Control chars ausgeschaltet";
						useVT100 = false;
					} else {
						sLastAction = "VT100 Control chars angeschaltet";
						useVT100 = true;
					}
					break;
				case 'z':
					return;
				default:
					sLastAction = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static void fnMenuLayer(){
		while (true) {
			HashMap<String, Object> hmCurr = hmLayer.get(iCurrLayer);
			int iLaySizX = Integer.parseInt(String.valueOf(hmCurr.get("iSizeX")));
			int iLaySizY = Integer.parseInt(String.valueOf(hmCurr.get("iSizeY")));

			String[][] asActions = {
					{"Um den Layer zu wechseln drücke", "W"},
					{"Um einen layer zu erstellen drücke", "N"},
					{"", ""},
					{"Zurück", "Z"}
			};
			String[][] asTooltip = {
					{"Layer Nummer:", String.valueOf(iCurrLayer)},
					{"Layer Größe:", String.format("%dx%d", iLaySizX, iLaySizY)},
					{"Geladen von:", (String) hmCurr.get("sFileNameLoad")},
					{"Speicher Ort:", (String) hmCurr.get("sFileNameSave")},

			};
			fnDrawMenu(asActions, true, asTooltip);
			char cOption = fnUserInput();
			switch (cOption) {
				case 'w':
					sLastAction = "Gib die Zahl des Layers ein";
					for (int key: hmLayer.keySet()){
						sInputOpt = sInputOpt.concat(String.format("%d,", key));
					}
					fnDrawMenu(asActions, true, asTooltip);
					int iLayer = sc.nextInt();sc.nextLine();
					if (iLayer<0|iLayer>7) {sLastAction="Layer nicht vorhanden"; break;}
					iCurrLayer = iLayer;
					break;
				case 'n':
					sLastAction = "Gib an welche Nummer\nder layer haben soll";
					sInputOpt="(Ganzzahl größer als 0)";
					fnDrawMenu(asActions, true, asTooltip);
					int iNewLayer = sc.nextInt();sc.nextLine();
					sLastAction = "Gib an wie Groß der Layer sein\n" +
							"soll. Diese Zahl wird beim Laden\n" +
							"eines Bildes auf den Layer\nverändert";
					sInputOpt = "z.B. 300x400";
					fnDrawMenu(asActions, true, asTooltip);
					String sSize = sc.nextLine();
					if (!sSize.contains("x")){sLastAction="Du musst schon eine valide größe eingeben"; break;}
					String[] asSize = sSize.split("x");
					int iNewSizeX = Integer.parseInt(asSize[0]);
					int iNewSizeY = Integer.parseInt(asSize[1]);
					fnCreateLayer(iNewLayer, iNewSizeY, iNewSizeX);
					sLastAction = "Layer erstellt";
					break;
				case 'z':
					return;
			}

		}
	}

	private static char fnUserInput() {
		String ui = sc.nextLine().toLowerCase(Locale.ROOT);
		if (ui.equals("")) {
			return '²';
		}
		return ui.charAt(0);
	}

	// INTERFACE RELATED METHODS ------------- END
	// IMAGE RELATED METHODS

	private static void fnCreateLayer(int iNewLayer, int iNewSizeY, int iNewSizeX){
		aiGrauWert.put(iNewLayer, new int[iNewSizeY][iNewSizeX]);
		iSizeY = iNewSizeY;
		iSizeX = iNewSizeX;
		iCurrLayer = iNewLayer;
		fnFillZeros();
		HashMap<String, Object> hmTemp = new HashMap<>();
		hmTemp.put("iSizeX", iNewSizeX);
		hmTemp.put("iSizeY", iNewSizeY);
		hmTemp.put("bFileSaved", false);
		hmTemp.put("sFileNameSave", "");
		hmTemp.put("sFileNameLoad", "");
		hmTemp.put("bFileLoaded", false);
		hmLayer.put(iCurrLayer, hmTemp);
	}

	private static void fnFillZeros(){
		for (int[] line : aiGrauWert.get(iCurrLayer)){
			Arrays.fill(line, 0);
		}
	}

	private static int[] fnGetPtFromStr(String in) {
		String[] sPoint = in.split(",");
		int x = Integer.parseInt(sPoint[0]);
		int y = Integer.parseInt(sPoint[1]);
		return new int[]{x,y};
	}

	private static void fnLighten(int iAmount, int[] point0, int[] point1) {
		iAmount = (int) (iAmount * 2.55);
		int iVal;
		for (int y=point0[1]; y <= point1[1] - 1; y++) {
			for (int x=point0[0]; x <= point1[0] - 1; x++) {
				iVal = aiGrauWert.get(iCurrLayer)[y][x] + iAmount;
				if (iVal < 0) {
					iVal = 0;
				}
				if (iVal > 255){
					iVal = 255;
				}
				aiGrauWert.get(iCurrLayer)[y][x] = iVal;
			}
		}
		sLastAction += "Bild Erhellt";
	}

	private static void fnInvert(int[] point0, int[] point1) {
		for (int y=point0[1]; y<=point1[1]-1;y++) {
			for (int x=point0[0]; x<=point1[0]-1; x++){
				aiGrauWert.get(iCurrLayer)[y][x] = aiGrauWert.get(iCurrLayer)[y][x]*-1 + 255;
			}
		}
	}

	private static int[][] fnRunMask(double[][] aiMask, int iMaskSize, int[] point0, int[] point1) {
		int[][] aiOut; // New temporary array for image
		int[][] aiTemp = new int[iSizeY][iSizeX]; // New temporary array for image
		int iTmpVal;
		int iSteps;
		int iNewPosX;
		int iNewPosY;
		iMaskSize /= 2;

		// Looping over the image array
		for (int yImg=point0[1]; yImg<=point1[1]-1; yImg++) {
			for (int xImg=point0[0]; xImg<=point1[0]-1; xImg++){
				// Looping over mask
				iTmpVal = 0;
				iSteps = 0;

				for (int yMask=-iMaskSize, iMaskIndY = 0; yMask<=iMaskSize-1; yMask++, iMaskIndY++){
					for (int xMask=-iMaskSize, iMaskIndX=0; xMask<=iMaskSize-1; xMask++, iMaskIndX++){
						// Y calculations for pixel
						iNewPosY = yImg + yMask;
						if (iNewPosY < 0){continue;}
						if (iNewPosY >= iSizeY){continue;}

						// X calculations for pixel
						iNewPosX = xImg + xMask;
						if (iNewPosX < 0){continue;}
						if (iNewPosX >= iSizeX){continue;}

						iTmpVal += aiGrauWert.get(iCurrLayer)[iNewPosY][iNewPosX] * aiMask[iMaskIndY][iMaskIndX];
						iSteps += aiMask[iMaskIndY][iMaskIndX];
					}
				}
				iTmpVal /= iSteps;

				if (iTmpVal < 0) {iTmpVal =0;}
				if (iTmpVal > 255) {iTmpVal=255;}
				aiTemp[yImg][xImg] = iTmpVal;
			}
		}
		aiOut = aiGrauWert.get(iCurrLayer);
		for (int y=point0[1]; y<=point1[1]-1; y++){
			if (point1[0] - point0[0] >= 0)
				System.arraycopy(aiTemp[y], point0[0], aiOut[y], point0[0], point1[0] - point0[0]);
		}
		return aiOut;
	}

	private static void fnBlurMean(int iAmount, int iSize, int[] position0, int[] position1) {
		// Generate Mask
		if (iSize % 2 == 0) iSize++;
		double[][] mask = new double[iSize][iSize];
		for (int i=0; i<=iSize-1; i++){
			for (int j=0; j<=iSize-1; j++){
				mask[i][j] = iAmount;
			}
		}
		fnCreateMaskString(mask);
		int[][] aiTemp = fnRunMask(mask, iSize, position0, position1);
		for (int y=0; y<=iSizeY-1; y++){
			if (iSizeX - 1 + 1 >= 0) System.arraycopy(aiTemp[y], 0, aiGrauWert.get(iCurrLayer)[y], 0, iSizeX);
		}
	}

	private static void fnBlurGaussian(int iAmount, int iSize) {
		double coeff = iAmount;
		double denom = -0.012;
		double sig_1 = 0.15;
		double sig_2 = 0.15;
		double my_1 = 0;
		double my_2 = 0;
		double p = 0;
		sLastAction = "Stelle deine Gaußglocke selbst\nein oder verwende das\nEingestellte.\n\nUm Mathematische Fehler" +
				" zu\nvermeiden wird die Glocke\nAutomatisch um Eins gehoben.";
		while (true) {
			String[][] asActions = new String[][]{
					{"Um Sigmoid 1 zu ändern drücke:", "1"},
					{"Um Sigmoid 2 zu ändern drücke:", "2"},
					{"Um Mikro 1 zu ändern drücke:", "3"},
					{"Um Mikro 2 zu ändern drücke:", "4"},
					{"Um P zu ändern drücke:", "5"},
					{"Um Koeffizient zu ändern drücke:", "6"},
					{"Um Denominator zu ändern drücke:", "7"},
					{"", ""},
					{"Bestätigen:", "J"},
					{"Zurück:", "Z"}
			};
			String[][] asTooltip = new String[][]{
					{"Sigmoid 1:", sig_1+""},
					{"Sigmoid 2:", sig_2+""},
					{"Mikro 1:", my_1+""},
					{"Mikro 2:", my_2+""},
					{"P:", p+""},
					{"Coeffizient:", coeff+""},
					{"Denominator:", denom+""},
					{"Kernel Größe:", iSize+""},
			};
			fnDrawMenu(asActions, true, asTooltip);
			char cOption = fnUserInput();
			switch (cOption) {
				case '1':
					sLastAction = "Gib eine Kommazahl ein";
					sInputOpt = "0.001 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "0.001 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", sig_1+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUi1 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi1.charAt(0))&&sUi1.charAt(0) == '-') break;
					sig_1 = Double.parseDouble(sUi1);
					break;
				case '2':
					sLastAction = "Gib eine Kommazahl ein";
					sInputOpt = "0.001 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "0.001 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", sig_2+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUi2 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi2.charAt(0))&&sUi2.charAt(0) == '-') break;
					sig_2 = Double.parseDouble(sUi2);
					break;
				case '3':
					sLastAction = "Gib eine Kommazahl ein";
					sInputOpt = "-1 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-1 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", my_1+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUi3 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi3.charAt(0))&&sUi3.charAt(0) != '-') break;
					my_1 = Double.parseDouble(sUi3);
					break;
				case '4':
					sLastAction = "Gib eine Kommazahl ein";
					sInputOpt = "-1 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-1 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", my_2+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUi4 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi4.charAt(0))&&sUi4.charAt(0) != '-') break;
					my_2 = Double.parseDouble(sUi4);
					break;
				case '5':
					sLastAction = "Gib eine Kommazahl ein";
					sInputOpt = "-1 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-1 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", p+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUi5 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi5.charAt(0))&&sUi5.charAt(0) != '-') break;
					p = Double.parseDouble(sUi5);
					break;
				case '6':
					sLastAction = "Gib eine Kommazahl ein";
					sInputOpt = "-5 - 5";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-5 - 5"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", coeff+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUi6 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi6.charAt(0))&&sUi6.charAt(0) != '-') break;
					coeff = Double.parseDouble(sUi6);
					break;
				case '7':
					sLastAction = "Gib eine Kommazahl ein";
					sInputOpt = "-1 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-1 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", denom+""}};
					fnDrawMenu(asActions, true, asTooltip);
					String sUi7 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi7.charAt(0))&&sUi7.charAt(0) != '-') break;
					System.out.println(sUi7);
					denom = Double.parseDouble(sUi7);
					break;
				case 'z':
					return;
				case 'j':
				case 'b':
					double[][] mask = new double[iSize][iSize];
					if (iSize % 2 == 0) iSize++;
					iSize /= 2;

					for (int y = -iSize, yMask = 0; y <= iSize-1; y++, yMask++) {
						for (int x = -iSize, xMask = 0; x <= iSize-1; x++, xMask++) {
							mask[yMask][xMask] = fnGaussianNormal(x, y, p, my_1, my_2, sig_1, sig_2, coeff, denom);
						}
					}
					fnCreateMaskString(mask);
					int[] point0 = {0, 0};
					int[] point1 = {iSizeX, iSizeY};
					try {
						int[][] aiTemp = fnRunMask(mask, iSize, point0, point1);
						for (int y = 0; y <= iSizeY - 1; y++) {
							if (iSizeX - 1 + 1 >= 0) System.arraycopy(aiTemp[y], 0, aiGrauWert.get(iCurrLayer)[y], 0, iSizeX);
						}
					} catch (ArithmeticException e) {
						sLastAction = "Mathematischer fehlschlag";
						//fnDrawStatus();
						break;
					}
					return;
				default:
					sLastAction = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static double fnGaussianNormal(int x, int y, double p, double my_1, double my_2,
										   double sig_1, double sig_2, double coeff, double denom) {
		double e = Math.E;

		//a(x,y)=Coeff ℯ^(Denom (((x-μ_{1})^(2))/(σ_{1}^(2))-2 p (x-μ_{1}) (y-μ_{2})/(σ_{1} σ_{2})+((y-μ_{2})^(2))/(σ_{2}^(2))))
		double pt1 = (Math.pow((x-p),2))/Math.pow(sig_1, 2);
		double pt2 = 2*p*(x-my_1)*((y-my_2)/(sig_1*sig_2));
		double pt3 = (Math.pow((y-my_2), 2))/(Math.pow(sig_2, 2));

		return (coeff * Math.pow(e, ((denom)/10)*(pt1-pt2+pt3)))+1;
	}

	// IMAGE RELATED METHODS ----------------- END

	public static void main (String[] args) {
		fnCreateLayer(0, 300, 400);
		fnFillZeros();
		if (args.length > 1){
			String file = args[1];
			fnLoadFromFile(file);
		} else {
			sLastAction = "Kein Bild geladen !!!";
		}
		boolean running = true;
		String[][] asActions = {
				{"Bild anzeigen", "D"},
				{"Informationen zum Bild zeigen", "I"},
				{"Bild Invertieren","N"},
				{"Helligkeit verändern", "H"},
				{"Bild Verwischen", "B"},
				{"Bild speichern", "S"},
				{"Bild öffnen", "O"},
				{"Layer", "L"},
				{"Einstellungen", "E"},
				{"Um das Programm zu beenden drücke", "Q"}
		};


		while (running) {
			fnDrawMenu(asActions, false, null);
			sLastAction = "";
			char cOption = fnUserInput();
			switch (cOption) {
				case 'd':
					fnShowImage();
					break;
				case 'h':
					fnMenuLighten();
					bFileSaved = false;
					break;
				case 'n':
					fnMenuInvert();
					bFileSaved = false;
					break;
				case 'b':
					fnMenuBlur();
					bFileSaved = false;
					break;
				case 's':
					fnSaveToFile();
					bFileSaved = true;
					break;
				case 'q':
					if (fnMenuQuit()){
						running = false;
					}
					break;
				case 'i':
					fnMenuShowInfo();
					break;
				case 'l':
					fnMenuLayer();
					break;
				case 'o':
					fnLoadFromFile(null);
					bFileSaved = false;
					break;
				case 'e':
					fnMenuSettings();
					break;
				default:
					sLastAction = "Deine eingabe war fehlerhaft!!!";
			}
		}
		System.out.println("Bye");
		System.exit(0);
	}
}
