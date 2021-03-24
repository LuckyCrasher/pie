package prj.test.framework;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.concurrent.ThreadLocalRandom;

public class ClsMain {

	// FRAMEWORK RELATED CONSTANTS AND VARIABLES

	//Implementation for Layers. Works like a dynamik list with Integer arrays of different sizes
	//Imnformation about a layer is stored in {layer_num: {key_string: Information}}
	private static final HashMap<Integer, int[][]> hiaiImage = new HashMap<>();
	private static final HashMap<Integer, HashMap<String, Object>> hihsoLayerInfo = new HashMap<>();
	private static int iCurrLayer = 0;
	private static int iSizeX;
	private static int iSizeY;

	//Chars for menu drawing
	private static char cTopRightCornerBold = '+';
	private static char cTopLeftCornerBold = '+';
	private static char cLeftBarBold = '+';
	private static char cRightBarBold = '+';
	private static char cBottomRightCornerBold = '+';
	private static char cBottomCrossBold = '+';
	private static char cHorBarBold = '-';
	private static char cBottomLeftCornerBold = '+';
	private static char cVertBar = '|';
	private static char cVertBarBold = '|';
	private static char cHorBar = '-';
	private static char cTopCross = '+';
	private static char cCross = '+';
	private static char cTopCrossBold = '+';

	//information will be later replaced by Information in LayerInfo
	private static boolean bFileSaved = true;
	private static String sFileNameSave = "";
	private static String sFileNameLoad = "";
	private static boolean bFileLoaded = false;

	//Global config values fo later use
	private static String sInputOpt = "";
	private static String sLastStatus = "";
	private static boolean useVT100 = false;
	private static boolean useBoxChars = false;
	private static final Scanner sc = new Scanner(System.in);
	private static final String sFsSeparator = System.getProperty("file.separator");


	// FRAMEWORK RELATED CONSTANTS AND VARIABLES ------------- END
	// FRAMEWORK RELATED METHODS

	private static void fnShowImage(){
		//
		// Opens The Image in a small viewing window
		//

		JFrame frame;
		Image img;
		ImageIcon imgIcon;
		JLabel lbl;

		if (!bFileLoaded) { // check if a file is already loaded
			sLastStatus = "Du musst vorher ein Bild Laden!!!";
			return;
		}
		frame = new JFrame("Image Viewer");
		frame.setSize(iSizeX, iSizeY);
		//convert image in aiGrauWert to usable image
		img = fnGetImage();
		//Show it
		imgIcon = new ImageIcon(img);
		lbl = new JLabel();
		lbl.setIcon(imgIcon);
		frame.getContentPane().add(lbl, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		sLastStatus += "Bild gezeigt";
	}

	private static String fnFileDialog(boolean mode){
		//
		// Opens a file dialog to save or open
		// @param boolean mode True = Open Save dialog;
		// 					   False = Open Open dialog
		//

		final JDialog jd;
		final JFileChooser fc;
		int userSelection;
		String path;
		File filePath;

		FileFilter filter;

		jd = new JDialog();
		fc = new JFileChooser("."+ sFsSeparator);
		filter = new FileFilter() {
			public String getDescription() {
				return "Portable Graymap (*.pgm)";

			}
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".pgm");
				}
			}
		};
		jd.setModal(true);
		jd.setAlwaysOnTop(true);
		jd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		//use the right file system separator
		jd.setVisible(false);
		//add a file Filter and set it as default

		fc.setFileFilter(filter);

		//Open Save or Open dialog
		if (mode) {
			fc.setDialogTitle("Save File");
			userSelection = fc.showSaveDialog(jd);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				filePath = fc.getSelectedFile();
				path = filePath.getAbsolutePath();
				System.out.println(path);
				//check if path ends with .pgm
				if (!path.endsWith(".pgm")){
					path += ".pgm";
				}
				return path;
			}

		} else {
			fc.setDialogTitle("Open File");
			userSelection = fc.showOpenDialog(jd);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				filePath = fc.getSelectedFile();
				path = filePath.getAbsolutePath();
				System.out.println(path);
				//check if path ends with .pgm
				if (!path.endsWith(".pgm")){
					return null;
				}
				return path;
			}
		}
		return null;
	}

	private static Image fnGetImage() {
		//
		//Converts the current loaded image to Usable image
		//

		BufferedImage image = new BufferedImage(iSizeX, iSizeY, BufferedImage.TYPE_INT_RGB);
		Color color;

		for (int y=0; y<=iSizeY-1; y++) {
			for (int x=0; x<=iSizeX-1; x++){
				//insert color from aiGrauwert into new image
				color = new Color(hiaiImage.get(iCurrLayer)[y][x],
						hiaiImage.get(iCurrLayer)[y][x],
						hiaiImage.get(iCurrLayer)[y][x]);
				image.setRGB(x, y, color.getRGB());
			}
		}
		return image;
	}

	private static void fnSaveToFile(){
		//
		// Handles the Saving
		//

		String sFileNamePath;
		File saveFile;
		BufferedWriter w;

		if (!bFileLoaded) { // check if a image is loaded
			sLastStatus = "Du musst vorher ein Bild Laden!!!";
			return;
		}
		sFileNamePath = fnFileDialog(true);

		if (sFileNamePath == null){ // If the file dialog was canceled
			sLastStatus += "Abgebrochen?";
			return;
		}

		System.out.printf("Datei wird gespeichert nach %s %n", sFileNamePath);
		try {
			//Saving the File to disk
			saveFile = new File(sFileNamePath);
			w = new BufferedWriter(new PrintWriter(saveFile));

			w.write("P2" + System.lineSeparator());
			w.write("# CREATOR: The GIMP's PNM Filter Version 1.0" + System.lineSeparator());

			//maybe file is different size so write size to header
			w.write(String.format("%d %d%s", iSizeX,  iSizeY, System.lineSeparator()));
			w.write("255" + System.lineSeparator());

			for (int[] line : hiaiImage.get(iCurrLayer)) {
				for (int val : line) {
					w.write(val + System.lineSeparator());
				}
			}
			w.flush();
			w.close();
			sFileNameSave = sFileNamePath;
			sLastStatus = "Bild gespeichert";
			bFileSaved = true;
		} catch (IOException e) {
			sLastStatus = "Beim schreiben der Datei ist ein fehler aufgetreten\n"
						+  "Vielleicht war der Name der Datei fehlerhaft\n";
		}
	}

	private static void fnLoadFromFile(String sFilePath) {
		//
		// Handles the File loading
		//

		String sFileNamePath;
		String sTemp;
		String line;
		File file;
		BufferedReader reader;
		String[] asHeader;
		HashMap<String, Object> hmTemp;

		// if argument was provided
		if (sFilePath != null){
			sFileNamePath = sFilePath;
		} else {
			sFileNamePath = fnFileDialog(false);
			if (sFileNamePath == null) {
				sLastStatus = "Abgebrochen?";
				return;
			}
		}


		try {
			//reading the file from disk
			file = new File(sFileNamePath);
			System.out.printf("Reading %s %n", file);
			reader = new BufferedReader(new FileReader(file));
			//get file Header some useful information
			asHeader = new String[4];
			for (int i = 0; i < 4; i++) {
				line = reader.readLine();
				asHeader[i] = line;
			}
			//dynamic image array created by the size contained in the header
			sTemp = asHeader[asHeader.length - 2];
			asHeader = sTemp.split(" ");
			iSizeX = Integer.parseInt(asHeader[0]);
			iSizeY = Integer.parseInt(asHeader[1]);
			sLastStatus += String.format("Größe des Bildes: X: %d Y: %d %n", iSizeX, iSizeY);
			hiaiImage.put(iCurrLayer, new int[iSizeY][iSizeX]);
			for (int y = 0; y <= iSizeY - 1; y++) {
				for (int x = 0; x <= iSizeX - 1; x++) {
					line = reader.readLine();
					hiaiImage.get(iCurrLayer)[y][x] = Integer.parseInt(line);
				}
			}
			reader.close();

			//write the Information to the LayerInfo for later use
			hmTemp = new HashMap<>();
			hmTemp.put("iSizeX", iSizeX);
			hmTemp.put("iSizeY", iSizeY);
			hmTemp.put("bFileSaved", true);
			hmTemp.put("sFileNameSave", "");
			hmTemp.put("sFileNameLoad", sFileNamePath);
			hmTemp.put("bFileLoaded", false);
			hmTemp.put("iPosArray", iCurrLayer);
			hihsoLayerInfo.put(iCurrLayer, hmTemp);

			sFileNameLoad = sFileNamePath;
			bFileLoaded = true;
			sLastStatus = "Bild geladen";
		} catch (IOException e) {
			sLastStatus += "Beim lesen der Datei ist ein fehler aufgetreten";
			sLastStatus += "Vielleicht war der Name der Datei fehlerhaft";
		}
	}

	private static void fnClear() {
		//
		//Clears screen by either resetting the cursor or printing linebreak
		//
		if (useVT100) {
			//Cursor reset code
			char escCode = 0x1B;
			int row = 0;
			int column = 0;
			//This is pretty cool right?
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

	private static String fnMotd(){
		//
		// Just randomly prints one of these nice Motd's
		//
		int iRand = ThreadLocalRandom.current().nextInt(1,100);
		String sMotd1 = "Wusstest du das man Bilder auch per argument laden kann?";
		String sMotd2 = "Wusstest du das das Programm eigentlich ein wenig witzig sein soll?";
		String sMotd3 = "";

		switch (iRand) {
			case 1: return sMotd1;
			case 2: return sMotd2;
			default: return sMotd3;
		}
	}

	private static void fnTitle(){
		//
		// Just randomly prints one of these nice Titles
		//
		int iRand = ThreadLocalRandom.current().nextInt(1,4);
		String title1 = "\n"+
				"     ###### #### ###### \n" +
				"     ##   ## ##  ##       \n" +
				"     ######  ##  ####   \n" +
				"     ##      ##  ##       \n" +
				"     ##     #### ###### \n"+
				"     (PGM Image Editor)";

		String title2 = ""+
				"     ██████╗  ██╗ ███████╗\n" +
				"     ██╔══██╗ ██║ ██╔════╝\n" +
				"     ██████╔╝ ██║ █████╗  \n" +
				"     ██╔═══╝  ██║ ██╔══╝  \n" +
				"     ██║      ██║ ███████╗\n" +
				"     ╚═╝      ╚═╝ ╚══════╝\n"+
				"     (PGM Image Editor)";

		String title3 = ""+
				"       _____ _____ ______ \n" +
				"      |  __ \\\\_   _|  ____|\n" +
				"      | |__) || | | |__   \n" +
				"      |  ___/ | | |  __|  \n" +
				"      | |    _| |_| |____ \n" +
				"      |_|   |_____|______|\n"+
				"      (PGM Image Editor)";
		switch (iRand) {
			case 1: System.out.println(title1);break;
			case 2: if(useBoxChars){System.out.println(title2);break;}//this one only if box chars are enabled
			case 3: System.out.println(title3);break;
		}
	}

	private static int[][] fnMenuArea(int[] newPoint0, int[] newPoint1) throws Exception {
		//
		// Creates a nice menu to specify the area.
		// Called by multiple functions in the code.
		//
		int[] oldPoint0 = newPoint0;
		int[] oldPoint1 = newPoint1;
		String[][] asActions;
		String[][] asTooltip;
		char cOption;
		String sPointA;
		String sPointB;

		sLastStatus = "Stelle den Bereich ein";
		while(true) {
			asActions = new String[][]{
					{"Um Punkt A zu ändern drücke:", "A"},
					{"Um Punkt B zu ändern drücke:", "B"},
					{"Um Das ganze Bild zu wählen drücke:", "C"},
					{"", ""},
					{"Zum bestätigen drücke", "J"},
					{"Zurück:", "Z"}
			};
			asTooltip = new String[][]{
					{"Bereich (X):", Arrays.toString(newPoint0)},
					{"Bereich (Y):", Arrays.toString(newPoint1)}
			};
			fnDrawMenu(asActions, asTooltip, "Currently selected area");
			cOption = fnUserInput();
			switch (cOption) {
				case 'a':
					sLastStatus = "Gib neue Werte für Punkt A an:";
					sInputOpt = "(0,0 - 400,300) A:";
					fnDrawMenu(asActions, asTooltip, "Currently selected area");
					sPointA = sc.nextLine();
					//Check if everything is alright with the entered string...
					if(!Character.isDigit(sPointA.charAt(0))|!sPointA.contains(",")) break;
					newPoint0 = fnGetPtFromStr(sPointA);
					break;
				case 'b':
					sLastStatus = "Gib neue Werte für Punkt B an:";
					sInputOpt = "(0,0 - 400,300) B:";
					fnDrawMenu(asActions, asTooltip, "Currently selected area");
					sPointB = sc.nextLine();
					//Check if everything is alright with the entered string...
					if(!Character.isDigit(sPointB.charAt(0))|!sPointB.contains(",")) break;
					newPoint1 = fnGetPtFromStr(sPointB);
					break;
				case 'c':
					sLastStatus = "Ganze Bild Gewählt";
					newPoint0 = new int[]{0, 0};
					newPoint1 = new int[]{iSizeX, iSizeY};
					break;
				case 'z':
					return new int[][]{oldPoint0, oldPoint1};
				case 'j':
					return new int[][]{newPoint0, newPoint1};
				default:
					sLastStatus = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static void fnMenuLighten() throws Exception {
		//
		// Menu interaction for the lighten method
		// values are then given in percent,
		//
		int iAmountL = 0;
		int [] point0; int[] point1;
		int[][] points;
		point0 = new int[]{0, 0};
		point1 = new int[]{iSizeX,iSizeY};
		String[][] asActions;
		String[][] asTooltip;
		char cOption;
		String sUi;

		while (true) {
			asActions = new String[][]{
					{"Um die Helligkeit einzustellen drücke:", "H"},
					{"Um den Bereich zu Ändern drücke:", "A"},
					{"", ""},
					{"Um zu bestätigen drücke:", "J"},
					{"Zurück", "Z"},
			};

			asTooltip = new String[][]{
					{"Neue Helligkeit:", iAmountL + "%"},
					{"Bereich (X):", Arrays.toString(point0)},
					{"Bereich (Y):", Arrays.toString(point1)}
			};

			fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
			cOption = fnUserInput();
			switch (cOption){
				case 'h':
					// see here, everything in percent
					sLastStatus = "Ändere die Helligkeit";
					asActions = new String[][]{
							{"Gib eine Prozentzahl ein:", "-100% - 100%"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Helligkeit:", iAmountL+"%"}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					sUi = sc.nextLine().toLowerCase(Locale.ROOT);
					if (sUi.equals("")) break;
					if (sUi.contains("%")) sUi = sUi.replace("%", "");
					if (!Character.isDigit(sUi.charAt(0))) break;
					iAmountL = Integer.parseInt(sUi);
					break;
				case 'a':
					//Function to set up the specified area
					points = fnMenuArea(point0, point1);
					point0 = points[0];
					point1 = points[1];
					break;
				case 'z':
					return;
				case 'b':
				case 'j':
					fnLighten(iAmountL, point0, point1);
					sLastStatus += "Bild Erhellt";
					return;
				default:
					sLastStatus = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static void fnMenuShowInfo() {
		//
		// Shows some Information about the currently loaded Image
		//
		StringBuilder sTmp;
		if (bFileLoaded) {
			sTmp = new StringBuilder();
			sTmp.append(String.format("Size:     %dx%d\n", iSizeX, iSizeY));
			sTmp.append(String.format("Layer:     %d\n", iCurrLayer));
			sTmp.append(String.format("Original Image Path:%n%s\n", fnWrapper(sFileNameLoad, sFsSeparator.charAt(0), 77)));
			if (!sFileNameSave.equals("")) sTmp.append(String.format("Saved to: %s\n", sFileNameSave));
			sTmp.append("Bild Infos gezeigt");
			sLastStatus += sTmp.toString();
		} else {
			sLastStatus += "Du musst zuerst eine Datei laden!!!";
		}
	}

	private static void fnCreateMaskString(double[][] mask) throws Exception {
		//
		// Creates the Table that will be showed after a convolution function was run
		// Absolute overcomplicated function but still...
		//
		int iSize = mask.length;
		String[][] asActions;
		char cOption;
		StringBuilder sMask;
		char c;
		int ind;

		c = 65;

		if (iSize >17){
			//mask may be too big to properly show, but you have the option
			sLastStatus = "Maske zu groß zum Anzeigen";
			asActions= new String[][]{
					{"Dennoch Anzeigen", "J"},
					{"Nicht zeigen", "N"}
			};
			fnDrawMenu(asActions,null, null);
			cOption = fnUserInput();
			if (cOption == 'n') return;
		}
		sMask = new StringBuilder();

		sLastStatus = "";
		//Top border
		sMask.append("  ").append(cTopRightCornerBold); //prepend some spaces
		for (int i=1; i<=iSize-1; i++,c++) {
			//print two line chars and a column identifier (c=A-Z)
			sMask.append(String.format("%c%c%c",cHorBarBold, c, cHorBarBold));
			sMask.append(cBottomCrossBold);
		}
		//once again just to finish the last row
		sMask.append(String.format("%c%c%c", cHorBarBold, c, cHorBarBold));
		sMask.append(cTopLeftCornerBold).append("\n");

		//Middle Fill
		c = 65;
		//prepend space and row identifier ; increment c = B
		//This part is complex...
		sMask.append(String.format(" %c%c",c, cVertBarBold)); c++;
		for (int i=0;i<=iSize-2;i++){sMask.append("   ").append(cVertBar);}
		sMask.append("   ").append(cVertBarBold).append("\n");
		for (int i=0;i<=iSize-2;i++,c++) {
			sMask.append("  ").append(cLeftBarBold);
			for (int j = 0; j <= iSize-2; j++) sMask.append(cHorBar).append(cHorBar).append(cHorBar).append(cCross);
			//sMask.append(String.format("───┨\n %c┃", c));
			// ^ that's the ame as that \|/
			sMask.append(cHorBar).append(cHorBar).append(cHorBar).append(cRightBarBold).append("\n ").append(c).append(cVertBarBold);
			for (int j = 0; j <= iSize - 2; j++) sMask.append("   ").append(cVertBar);
			sMask.append("   ").append(cVertBar).append("\n");
		}
		//Bottom border
		sMask.append("  ").append(cBottomRightCornerBold);
		for (int i=1; i<=iSize-1; i++) {
			sMask.append(cHorBarBold).append(cHorBarBold).append(cHorBarBold);
			sMask.append(cTopCrossBold);
		}
		sMask.append(cHorBarBold).append(cHorBarBold).append(cHorBarBold);
		sMask.append(cBottomLeftCornerBold).append("\n");

		//replace all empty space with NUMBERS
		//was easier to create an empty table
		for (double[] line: mask){
			for (double val: line){
				ind = sMask.indexOf("   ");
				sMask.replace(ind, ind+3, String.format("%3.1f", val));
			}
		}
		//Show the mask over the menu panel
		System.out.println(sMask.toString());
		sLastStatus += sMask.toString();
	}

	private static void fnMenuConvolve() throws Exception {
		//
		// Menu interaction for functions that convolve.
		//
		if (!bFileLoaded) {
			sLastStatus += "Du musst vorher ein Bild Laden!!!";
			return;
		}

		// set some standards not good but good enough
		boolean running;
		int iAmountB;
		int iSizeB;
		int[] point0;
		int[] point1;
		String sBlurFunc;
		String[][] asActions;

		running = true;
		iAmountB = 1;
		iSizeB = 3;
		point0 = new int[]{0, 0};
		point1 = new int[]{iSizeX, iSizeY};
		sBlurFunc  = "Mean Blur";

		while (running){
			asActions = new String[][]{
					{"Um die \"Convolute Function\" zu ändern drücke", "F"},
					{"Um die stärke einzustellen drücke", "W"},
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

			fnDrawMenu(asActions, asTooltip,"(Aktuelle Einstellungen)");
			char cBlurOpt = fnUserInput();
			switch (cBlurOpt){
				case 'f':
					sLastStatus = "Wähle eine Funktion";
					//each submenu needs a new loop or function so...
					boolean subMenF = true;
					while(subMenF) {
						asActions = new String[][]{
								{"Für einen \"Gaussian Blur\" drücke", "G"},
								{"Für einen \"Mean (Box) Blur\" drücke", "M"},
								{"Für \"Edge Detection\" drücke", "E"}
						};
						fnDrawMenu(asActions, null, null);
						char cBlurFn = fnUserInput();
						switch (cBlurFn) {
							case 'g':
								sBlurFunc = "Gaussian Blur";
								subMenF = false;
								sLastStatus = "Funktion gewählt.";
								break;
							case 'm':
								sBlurFunc = "Mean Blur";
								subMenF = false;
								sLastStatus = "Funktion gewählt.";
								break;
							case 'e':
								sBlurFunc = "Edge Detection";
								subMenF = false;
								sLastStatus = "Funktion gewählt.";
								break;
							default:
								sLastStatus = "Du musst schon eine Funktion auswählen";
						}
					}
					break;
				case 'm':
					sLastStatus = "Gib eine Größe ein";
					sInputOpt = "0 - 100";
					asActions = new String[][]{
							{"Gib eine Ganzzahl ein:", "1 - 100"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Größe:", iSizeB+""}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sUiS = sc.nextLine().toLowerCase(Locale.ROOT);
					if (!Character.isDigit(sUiS.charAt(0))) break;
					iSizeB = Integer.parseInt(sUiS);
					break;
				case 'w':
					sLastStatus = "Gib eine Stärke ein";
					sInputOpt = "0 - 100";
					asActions = new String[][]{
							{"Gib eine Ganzzahl ein:", "1 - 100"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Stärke:", iAmountB+""}};
					fnDrawMenu(asActions, asTooltip,"(Aktuelle Einstellungen)");
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
					running = false;
					break;
				case 'b':
				case 'j':
					switch (sBlurFunc) {
						case "Mean Blur":
							fnBlurMean(iAmountB, iSizeB, point0, point1);
							sLastStatus = sLastStatus.concat("Bild verwischt");
							running = false;
							break;
						case "Gaussian Blur":
							fnBlurGaussian(iAmountB, iSizeB, point0, point1);
							sLastStatus = sLastStatus.concat("Bild verwischt");
							running = false;
							break;
						case "Edge Detection":
							fnEdgeDetect(iAmountB, iSizeB, point0, point1);
							sLastStatus = sLastStatus.concat("Bild verwischt");
							running = false;
							break;
						default:
							sLastStatus = "Etwas ist falsch gelaufen...";
							break;
					}
				default:
					sLastStatus = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static void fnMenuInvert() throws Exception {
		//
		// Menu interaction for the Image invert function
		//

		if (!bFileLoaded) { //just a little check before unnecessary memory is stolen..
			sLastStatus += "Du musst vorher ein Bild Laden!!!";
			return;
		}

		boolean running = true;
		String[][] asActions;
		String[][] asTooltip;
		int [] point0;
		int[] point1;

		point0 = new int[]{0, 0};
		point1 = new int[]{iSizeX,iSizeY};


		sLastStatus = "Bild Invertieren";
		while (running) {
			asActions = new String[][]{
					{"Um einen Bereich auszuwählen drücke", "A"},
					{"Bestätigen", "J"},
					{"Zurück", "Z"}
			};
			asTooltip = new String[][]{
					{"Bereich (X):", Arrays.toString(point0)},
					{"Bereich (Y):", Arrays.toString(point1)}
			};
			fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
			char cOption = fnUserInput();
			switch (cOption) {
				case 'a':
					int[][] points = fnMenuArea(point0, point1);
					point0 = points[0];
					point1 = points[1];
					break;
				case 'z':
					running = false;
					break;
				case 'b':
				case 'j':
					fnInvert(point0, point1);
					sLastStatus = "Bild Invertiert";
					running = false;
					break;
				default:
					sLastStatus = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static boolean fnMenuQuit() throws Exception {
		//
		// little menu before you quit.
		// Will be shown if user didn't save the edited file.
		//
		if (bFileSaved) { // just return end exits the program if file is already saved.
			return true;
		}
		sLastStatus = "Du hast noch nicht gespeichert!!";
		String[][] asActions;

		asActions = new String[][]{
				{"Um zu speichern drücke", "S"},
				{"Um Abzubrechen drücke", "A"},
				{"Um trotzdem zu schließen drücke", "N"},
		};
		// yes an infinite loop but is easily readable.
		while (true) {
			fnDrawMenu(asActions, null,null);
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
					sLastStatus = "Du musst schon Ja, Nein oder Abbrechen antworten";
				}
			}
		}
	}

	private static void fnDrawMenu(String[][] asActions, String[][] asTooltip, String sTooltipTitle) throws Exception {
		//
		// Draws all the menus.
		// Overcomplicated but COOL...
		//
		boolean bShowTooltip;
		int iLenTooltipTitle;
		String[][] asTempTooltip;
		bShowTooltip = asTooltip != null; //nice oneliner

		//if a tooltip is given insert it into a temporary array
		if (bShowTooltip) {
			System.out.println(Arrays.deepToString(asTooltip));
			//count line breaks for temp array
			int iToolTipLinebreak = 0;
			for (String[] tooltip : asTooltip) {
				iToolTipLinebreak += tooltip[1].split("\n").length;
			}
			//create new temp array and insert into it
			asTempTooltip = new String[iToolTipLinebreak][2];
			int i = 0;
			int j;
			for (String[] tooltip : asTooltip) {
				asTempTooltip[i][0] = tooltip[0]; 					//standard beginning
				String[] tooltipMulti = tooltip[1].split("\n");	//split because multiline
				j = 0;
				System.out.println("ARRAY: " + Arrays.toString(tooltipMulti));
				do {
					System.out.println("J: " + j +" I: " + i);
					asTempTooltip[i][1] = tooltipMulti[j];
					i++;
					j++;
				} while (j <= tooltipMulti.length-1);
			}
			System.out.println(Arrays.deepToString(asTempTooltip));
		} else { asTempTooltip = null; }

		fnClear();
		fnTitle();
		StringBuilder sOut = new StringBuilder();
		int columns = 80;

		// Top Line
		sOut.append(cTopRightCornerBold);
		for (int width=1;width<=3;width++) sOut.append(cHorBarBold);
		sOut.append("(Status)"); // 8 chars
		if (bShowTooltip) {
			for (int width=11;width<=33;width++) sOut.append(cHorBarBold);
			sOut.append(cBottomCrossBold);
			if (sTooltipTitle != null){
				iLenTooltipTitle = sTooltipTitle.length();
				if (iLenTooltipTitle > 40){ throw new Exception("Tooltip title too long..."); }
				sOut.append(cHorBarBold).append(cHorBarBold).append(cHorBarBold);
				sOut.append(sTooltipTitle);
				for (int width=iLenTooltipTitle+38;width<=columns-1;width++) sOut.append(cHorBarBold);
			} else {
				for (int width=35;width<=columns-1;width++) sOut.append(cHorBarBold);
			}
		} else {
			for (int width=11;width<=columns-1;width++) sOut.append(cHorBarBold);
		}
		sOut.append(cTopLeftCornerBold).append("\n");

		String[] asStatus = sLastStatus.split("\n");
		if (bShowTooltip) {
			int lenTooltip = asTempTooltip.length;

			for (int line=0;line<=lenTooltip-1;line++) {
				sOut.append(cVertBarBold);
				//if sLastAction is not empty | aah whatever
				if (!asStatus[0].equals("") && line<asStatus.length) {
					//Print line of asStatus (sLastAction)
					sOut.append(String.format(" %-33s%c", asStatus[line], cVertBar));
				} else {
					//sLastAction is empty so fill it with space
					sOut.append(String.format(" %-33s%c", " ", cVertBar));
				}
				if(asTempTooltip[line][0]!=null){
					sOut.append(String.format(" %-24s", asTempTooltip[line][0])); // Print first part of Tooltip
					sOut.append(String.format(" %-19s", asTempTooltip[line][1])); // Print second part of Tooltip
				}  else {
					sOut.append(String.format(" %-43s ", asTempTooltip[line][1]));
				}

				sOut.append(cVertBarBold).append("\n");

			}
		} else {
			for (String line : asStatus) {
				sOut.append(cVertBarBold);
				sOut.append(String.format(" %-79s%c%n", line, cVertBarBold));
			}
		}
		sOut.append(cLeftBarBold);
		if (bShowTooltip){
			for (int width=1;width<=34;width++) sOut.append(cHorBar);
			sOut.append(cTopCross);
			for (int width=35;width<=columns-1;width++) sOut.append(cHorBar);
		} else {
			for (int width=1;width<=columns;width++) sOut.append(cHorBar);
		}

		sOut.append(cRightBarBold).append("\n");

		for (String[] sAction : asActions){
			sOut.append(cVertBarBold);
			if (sAction[0].equals("")) {
				for (int k=0;k<=columns-1;k++) sOut.append(" ");
				sOut.append(cVertBarBold).append("\n");
			} else {
				sOut.append(String.format(" %-56s %21s %c%n", sAction[0], "'"+sAction[1]+"'", cVertBarBold));
			}
		}

		sOut.append(cBottomRightCornerBold);
		for (int width=1;width<=columns;width++) sOut.append(cHorBarBold);
		sOut.append(cBottomLeftCornerBold).append("\n");

		System.out.print(sOut.toString());
		System.out.print(sInputOpt+" ->");
		sInputOpt = "";
	}

	private static void fnMenuSettings() throws Exception {
		//
		// Some smaller settings to interact with
		//
		sLastStatus = "Einstellungen werden nicht bis zum nächsten start gespeichert!";
		String sVt100;
		String sBoxChars;
		while (true) {

			if (useVT100) {
				sVt100 = "C: [AN]/AUS";
			} else {
				sVt100 = "C: AN/[AUS]";
			}

			if (useBoxChars) {
				sBoxChars = "B: [AN]/AUS";
			} else {
				sBoxChars = "B: AN/[AUS]";
			}
			//this could work with lists or Hashmaps but booooring...
			if (useBoxChars) {
				//┏
				cTopRightCornerBold = '┏';
				//┓
				cTopLeftCornerBold = '┓';
				//┠
				cLeftBarBold = '┠';
				//┨
				cRightBarBold = '┨';
				//┗
				cBottomRightCornerBold = '┗';
				//┯
				cBottomCrossBold = '┯';
				//━
				cHorBarBold = '━';
				//┛
				cBottomLeftCornerBold = '┛';
				//│
				cVertBar = '│';
				//┃
				cVertBarBold = '┃';
				//─
				cHorBar = '─';
				//┴
				cTopCross = '┴';
				//┷
				cTopCrossBold = '┷';
				//┼
				cCross = '┼';
			} else {
				//┏
				cTopRightCornerBold = '+';
				//┓
				cTopLeftCornerBold = '+';
				//┠
				cLeftBarBold = '+';
				//┨
				cRightBarBold = '+';
				//┗
				cBottomRightCornerBold = '+';
				//┯
				cBottomCrossBold = '+';
				//━
				cHorBarBold = '-';
				//┛
				cBottomLeftCornerBold = '+';
				//│
				cVertBar = '|';
				//┃
				cVertBarBold = '|';
				//─
				cHorBar = '-';
				//┴
				cTopCross = '+';
			}


			String[][] asActions = new String[][]{
					{"\"VT100 Contol chars\" benutzen", sVt100},
					{"\"Box chars\" benutzen", sBoxChars},
					{"", ""},
					{"Zurück", "Z"}
			};
			fnDrawMenu(asActions, null, null);
			char cOption = fnUserInput();
			switch (cOption) {
				case 'c':
					if (useVT100) {
						sLastStatus = "VT100 Control chars ausgeschaltet";
						useVT100 = false;
					} else {
						sLastStatus = "VT100 Control chars angeschaltet";
						useVT100 = true;
					}
					break;
				case 'b':
					if (useBoxChars) {
						sLastStatus = "Box chars ausgeschaltet";
						useBoxChars = false;
					} else {
						sLastStatus = "Box chars angeschaltet";
						useBoxChars = true;
					}
					break;
				case 'z':
					return;
				default:
					sLastStatus = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static void fnMenuLayer() throws Exception {
		//
		// Menu to interact with the different layers
		// Not fully bulletproof but still here...
		//
		int [] point0; int[] point1;
		point0 = new int[]{0, 0};
		point1 = new int[]{iSizeX,iSizeY};

		while (true) {
			HashMap<String, Object> hmCurr = hihsoLayerInfo.get(iCurrLayer);
			int iLaySizX = Integer.parseInt(String.valueOf(hmCurr.get("iSizeX")));
			int iLaySizY = Integer.parseInt(String.valueOf(hmCurr.get("iSizeY")));

			String[][] asActions = {
					{"Um den Layer zu wechseln drücke", "W"},
					{"Um einen layer zu erstellen drücke", "N"},
					{"Um Layer zu Kombinieren drücke", "M"},
					{"", ""},
					{"Zurück", "Z"}
			};
			String sFilePathLoad;
			if (!hmCurr.get("sFileNameLoad").equals("")) {
				sFilePathLoad = "\n" + fnWrapper((String) hmCurr.get("sFileNameLoad"), sFsSeparator.charAt(0), 43);
			} else {
				sFilePathLoad = "";
			}
			String[][] asTooltip = {
					{"Layer Nummer:", String.valueOf(iCurrLayer)},
					{"Layer Größe:", String.format("%dx%d", iLaySizX, iLaySizY)},
					{"Geladen von:", sFilePathLoad},
					{"Speicher Ort:", (String) hmCurr.get("sFileNameSave")},

			};
			fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
			char cOption = fnUserInput();
			int iTargetLayer = iCurrLayer;
			int[] aiCombLayer = new int[1];
			switch (cOption) {
				case 'w':
					sLastStatus = "Gib die Zahl des Layers ein";
					for (int key: hihsoLayerInfo.keySet()){
						sInputOpt = sInputOpt.concat(String.format("%d,", key));
					}
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					int iLayer = sc.nextInt();sc.nextLine();
					if (!hihsoLayerInfo.containsKey(iLayer)) {
						sLastStatus ="Layer nicht vorhanden"; break;}
					iCurrLayer = iLayer;
					break;
				case 'n':
					sLastStatus = "Gib an welche Nummer\nder layer haben soll";
					sInputOpt="(Ganzzahl größer als 0)";
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					int iNewLayer = sc.nextInt();sc.nextLine();
					sLastStatus = "Gib an wie Groß der Layer sein\n" +
							"soll. Diese Zahl wird beim Laden\n" +
							"eines Bildes auf den Layer\nverändert";
					sInputOpt = "z.B. 400x300";
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sSize = sc.nextLine();
					if (!sSize.contains("x")){
						sLastStatus ="Du musst schon eine valide größe eingeben"; break;}
					String[] asSize = sSize.split("x");
					int iNewSizeX = Integer.parseInt(asSize[0]);
					int iNewSizeY = Integer.parseInt(asSize[1]);
					fnCreateLayer(iNewLayer, iNewSizeY, iNewSizeX);
					sLastStatus = "Layer erstellt";
					break;
				case 'm':
					boolean bSubMenuComb = true;
					while (bSubMenuComb) {
						String[][] asActionsComb = {
								{"Um einen Bereich auszuwählen drücke", "A"},
								{"Um einen Layer hinzuzufügen drücke", "L"},
								{"Um einen Layer zu entfernen drücke", "D"},
								{"Um einen Ziel Layer zu wählen drücke", "T"},
								{"", ""},
								{"Bestätigen", "J"},
								{"Zurück", "Z"}
						};
						String[][] asTooltipComb = {
								{"Ausgewählte Layer", Arrays.toString(aiCombLayer)},
								{"Target Layer", String.valueOf(iTargetLayer)},
								{"Bereich (X):", Arrays.toString(point0)},
								{"Bereich (Y):", Arrays.toString(point1)}
						};
						sLastStatus = "Gib die Layer an die du Kombinieren möchtest";
						fnDrawMenu(asActionsComb, asTooltipComb, "(Aktuelle Einstellungen)");
						char cOptionComb = fnUserInput();
						switch (cOptionComb) {
							case 'a':
								int[][] points = fnMenuArea(point0, point1);
								point0 = points[0]; point1 = points[1];
								break;
							case 't':
								sLastStatus = "Gib einen Ziel Layer ein";
								fnDrawMenu(asActions, null, null);
								String sTargetLay = sc.nextLine();
								if (!Character.isDigit(sTargetLay.charAt(0))) break;
								iTargetLayer = Integer.parseInt(sTargetLay);
								break;
							case 'l':
								// Showing some Input Options
								sInputOpt = sInputOpt.concat("Layer:");
								for (int key : hihsoLayerInfo.keySet()) {
									sInputOpt = sInputOpt.concat(String.format(" %d", key));
								}
								sInputOpt = sInputOpt.concat(" z.B. (1,2,3)");
								//Getting user input
								fnDrawMenu(asActionsComb, null, null);
								String sComLayer = sc.nextLine();
								if (!Character.isDigit(sComLayer.charAt(0)) | sComLayer.length() < 2) break;
								String[] asCombLayer = sComLayer.split(",");
								aiCombLayer = new int[asCombLayer.length];
								for (int i=0;i<=asCombLayer.length-1;i++) {
									aiCombLayer[i] = Integer.parseInt(asCombLayer[i]);
								}
								break;
							case 'j':
								if (aiCombLayer.length<2) break;
								fnCombineLayers(aiCombLayer, iTargetLayer, point0, point1);
								return;
							case 'z':
								bSubMenuComb = false;
								break;
							default:
								sLastStatus = "Du musst schon eine Option wählen";
						}
					}
					break;
				case 'z':
					return;
				default:
					sLastStatus = "Du musst schon eine Option wählen";
			}

		}
	}

	private static String fnWrapper(String in, char cBreak, int len){
		//
		// Wraps input string at char after specified length.
		//
		StringBuilder sOut = new StringBuilder();
		String sTemp;
		String[] asTemp;
		sTemp = in.replace(cBreak+"", cBreak+"\n");
		asTemp = sTemp.split("\n");
		int i=0;
		for (String word : asTemp){
			if ((word.length()+i)>len) {
				sOut.append("\n");
				i=0;
			}
			sOut.append(word);
			i += word.length();
		}
		return sOut.toString();
	}

	private static char fnUserInput() {
		//
		// Handles the user Input
		// returns weird char if something went wrong
		//
		String ui = sc.nextLine().toLowerCase(Locale.ROOT);
		if (ui.equals("")) {
			return '²';
		}
		return ui.charAt(0);
	}

	// INTERFACE RELATED METHODS ------------- END
	// IMAGE RELATED METHODS

	private static void fnCombineLayers(int[] aiLayers, int iTargetLayer, int[] point0, int[] point1){
		//
		// Combines layers...
		// Very unstable function but i like it.
		//
		int iNewSizeY = 0, iNewSizeX = 0, iLayerX, iLayerY;
		for (int layer : aiLayers){
			iLayerX = (int) hihsoLayerInfo.get(layer).get("iSizeX");
			iLayerY = (int) hihsoLayerInfo.get(layer).get("iSizeY");
			if (iLayerX>iNewSizeX){
				iNewSizeX = iLayerX;
			}
			if (iLayerY>iNewSizeY){
				iNewSizeY = iLayerY;
			}
		}
		int iPixel;
		int[][] aiTemp = new int[iNewSizeY][iNewSizeX];
		for (int y=point0[1];y<=point1[1]-1;y++){
			for (int x=point0[0];x<=point1[0]-1;x++){
				iPixel = 0;
				for (int layer : aiLayers){
					iPixel += hiaiImage.get(layer)[y][x];
				}
				aiTemp[y][x] = iPixel/aiLayers.length;
			}
		}
		for (int layer : aiLayers){
			hiaiImage.remove(layer);
			hihsoLayerInfo.remove(layer);
		}
		hiaiImage.remove(iTargetLayer);
		hihsoLayerInfo.remove(iTargetLayer);

		iSizeY = iNewSizeY;
		iSizeX = iNewSizeX;

		HashMap<String, Object> hmTemp = new HashMap<>();
		hmTemp.put("iSizeX", iNewSizeX);
		hmTemp.put("iSizeY", iNewSizeY);
		hmTemp.put("bFileSaved", false);
		hmTemp.put("sFileNameSave", "");
		hmTemp.put("sFileNameLoad", "");
		hmTemp.put("bFileLoaded", true);
		hmTemp.put("iPosArray", iTargetLayer);
		hihsoLayerInfo.put(iCurrLayer, hmTemp);

		for (int[] line : aiTemp){
			System.out.println(Arrays.toString(line));
		}

		hiaiImage.put(iTargetLayer, aiTemp);
		iCurrLayer = iTargetLayer;
		sLastStatus = "Bilder kombiniert";
	}

	private static void fnCreateLayer(int iNewLayer, int iNewSizeY, int iNewSizeX){
		//
		// Creates a new layer and paints it black.
		//
		hiaiImage.put(iNewLayer, new int[iNewSizeY][iNewSizeX]);
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
		hihsoLayerInfo.put(iCurrLayer, hmTemp);
	}

	private static void fnFillZeros(){
		//
		// Fills current layer with zeros (makes ist black)
		//
		for (int[] line : hiaiImage.get(iCurrLayer)){
			Arrays.fill(line, 0);
		}
	}

	private static int[] fnGetPtFromStr(String in) {
		//
		// Returns two inserted points.
		// just some splitting...
		//
		String[] sPoint = in.split(",");
		int x = Integer.parseInt(sPoint[0]);
		int y = Integer.parseInt(sPoint[1]);
		return new int[]{x,y};
	}

	private static void fnLighten(int iAmount, int[] point0, int[] point1) {
		//
		// Lightens the Image by a percentage.
		//
		iAmount = (int) (iAmount * 2.55);
		int iVal;
		for (int y=point0[1]; y <= point1[1] - 1; y++) {
			for (int x=point0[0]; x <= point1[0] - 1; x++) {
				//adds the new amount onto old pixel value
				iVal = hiaiImage.get(iCurrLayer)[y][x] + iAmount;
				if (iVal < 0) {
					iVal = 0;
				}
				if (iVal > 255){
					iVal = 255;
				}
				hiaiImage.get(iCurrLayer)[y][x] = iVal;
			}
		}
		sLastStatus += "Bild Erhellt";
	}

	private static void fnInvert(int[] point0, int[] point1) {
		//
		// Inverts every pixel in the image.
		// Nice linear function...
		//
		for (int y=point0[1]; y<=point1[1]-1;y++) {
			for (int x=point0[0]; x<=point1[0]-1; x++){
				hiaiImage.get(iCurrLayer)[y][x] = hiaiImage.get(iCurrLayer)[y][x]*-1 + 255;
			}
		}
	}

	private static int[][] fnRunMask(double[][] aiMask, int iMaskSize, int[] point0, int[] point1){
		//
		// Runs a mask over an image and performs an operation.
		// originally I wanted to specify a function as parameter that performs the operation,
		// how do I do that?
		//

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

				for (int yMask=-iMaskSize, iMaskIndY = 0; yMask<=iMaskSize; yMask++, iMaskIndY++){
					for (int xMask=-iMaskSize, iMaskIndX=0; xMask<=iMaskSize; xMask++, iMaskIndX++){
						// Y calculations for pixel
						iNewPosY = yImg + yMask;
						if (iNewPosY < 0){continue;}
						if (iNewPosY >= iSizeY){continue;}

						// X calculations for pixel
						iNewPosX = xImg + xMask;
						if (iNewPosX < 0){continue;}
						if (iNewPosX >= iSizeX){continue;}

						iTmpVal += hiaiImage.get(iCurrLayer)[iNewPosY][iNewPosX] * aiMask[iMaskIndY][iMaskIndX];
						//here is where all the old math errors where fixed
						iSteps += Math.abs(aiMask[iMaskIndY][iMaskIndX]);
					}
				}
				//and here is where they happened...
				iTmpVal /= iSteps;

				if (iTmpVal < 0) {iTmpVal =0;}
				if (iTmpVal > 255) {iTmpVal=255;}
				aiTemp[yImg][xImg] = iTmpVal;
			}
		}

		aiOut = hiaiImage.get(iCurrLayer);
		for (int y=point0[1]; y<=point1[1]-1; y++){
			if (point1[0] - point0[0] >= 0)
				System.arraycopy(aiTemp[y], point0[0], aiOut[y], point0[0], point1[0] - point0[0]);
		}
		return aiOut;
	}

	private static void fnEdgeDetect(int iAmount, int iSize, int[] point0, int[] point1){
		//
		// Generate mask for edge detection
		// works, but not as intended
		double[][] mask1 = {
				{-1, 0 ,1},
				{-2, 0 ,2},
				{-1, 0 ,1}
		};
		double[][] mask2 = {
				{-1, -2, -1},
				{ 0,  0,  0},
				{ 1,  2,  1}
		};

		int[][] aiTemp1 = fnRunMask(mask1, 3, point0, point1);
		int[][] aiTemp2 = fnRunMask(mask2, 3, point0, point1);
		int[][] aiTempFinal = new int[iSizeY][iSizeX];


		for (int y = point0[1]; y<=point1[1]-1; y++){
			for (int x = point0[0]; x<=point1[0]-1; x++){
				//calculate the gradient and insert it.
				aiTempFinal[y][x] = (int) Math.sqrt(Math.pow(aiTemp1[y][x], 2) + Math.pow(aiTemp2[y][x], 2));
			}
		}
		for (int y=0; y<=iSizeY-1; y++){
			if (iSizeX - 1 + 1 >= 0) System.arraycopy(aiTempFinal[y], 0, hiaiImage.get(iCurrLayer)[y], 0, iSizeX);
		}
	}

	private static void fnBlurMean(int iAmount, int iSize, int[] position0, int[] position1) throws Exception {
		//
		// Perform a Box Blur
		//

		// Create Mask
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
			if (iSizeX - 1 + 1 >= 0) System.arraycopy(aiTemp[y], 0, hiaiImage.get(iCurrLayer)[y], 0, iSizeX);
		}
	}

	private static void fnBlurGaussian(int iAmount, int iSize, int[] point0, int[]point1) throws Exception {
		//
		// Performs a Gaussian Blur
		// Smart guy that Carl Gauss, pretty cool function
		//

		double coeff = iAmount;
		double denom = -0.012;
		double sig_1 = 0.15;
		double sig_2 = 0.15;
		double my_1 = 0;
		double my_2 = 0;
		double p = 0;
		sLastStatus = "Stelle deine Gaußglocke selbst\nein oder verwende das\nEingestellte.\n\nUm Mathematische Fehler" +
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
					{"Um Den brerich zu ändern drücke:", "A"},
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
					{"Bereich (X):", Arrays.toString(point0)},
					{"Bereich (Y):", Arrays.toString(point1)}

			};
			fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
			char cOption = fnUserInput();
			switch (cOption) {
				case '1':
					sLastStatus = "Gib eine Kommazahl ein";
					sInputOpt = "0.001 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "0.001 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", sig_1+""}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sUi1 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi1.charAt(0))&&sUi1.charAt(0) == '-') break;
					sig_1 = Double.parseDouble(sUi1);
					break;
				case '2':
					sLastStatus = "Gib eine Kommazahl ein";
					sInputOpt = "0.001 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "0.001 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", sig_2+""}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sUi2 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi2.charAt(0))&&sUi2.charAt(0) == '-') break;
					sig_2 = Double.parseDouble(sUi2);
					break;
				case '3':
					sLastStatus = "Gib eine Kommazahl ein";
					sInputOpt = "-1 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-1 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", my_1+""}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sUi3 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi3.charAt(0))&&sUi3.charAt(0) != '-') break;
					my_1 = Double.parseDouble(sUi3);
					break;
				case '4':
					sLastStatus = "Gib eine Kommazahl ein";
					sInputOpt = "-1 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-1 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", my_2+""}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sUi4 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi4.charAt(0))&&sUi4.charAt(0) != '-') break;
					my_2 = Double.parseDouble(sUi4);
					break;
				case '5':
					sLastStatus = "Gib eine Kommazahl ein";
					sInputOpt = "-1 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-1 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", p+""}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sUi5 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi5.charAt(0))&&sUi5.charAt(0) != '-') break;
					p = Double.parseDouble(sUi5);
					break;
				case '6':
					sLastStatus = "Gib eine Kommazahl ein";
					sInputOpt = "-5 - 5";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-5 - 5"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", coeff+""}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sUi6 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi6.charAt(0))&&sUi6.charAt(0) != '-') break;
					coeff = Double.parseDouble(sUi6);
					break;
				case '7':
					sLastStatus = "Gib eine Kommazahl ein";
					sInputOpt = "-1 - 1";
					asActions = new String[][]{
							{"Gib eine Kommazahl ein:", "-1 - 1"},
							{"Zum Abbrechen drücke", "A"}
					};
					asTooltip = new String[][]{{"Eingestellte Zahl:", denom+""}};
					fnDrawMenu(asActions, asTooltip, "(Aktuelle Einstellungen)");
					String sUi7 = sc.nextLine().toLowerCase(Locale.ROOT).replace(",", ".");
					if (!Character.isDigit(sUi7.charAt(0))&&sUi7.charAt(0) != '-') break;
					System.out.println(sUi7);
					denom = Double.parseDouble(sUi7);
					break;
				case 'a':
					int[][] points = fnMenuArea(point0, point1);
					point0 = points[0];
					point1 = points[1];
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
					try {
						int[][] aiTemp = fnRunMask(mask, iSize, point0, point1);
						for (int y = 0; y <= iSizeY - 1; y++) {
							if (iSizeX - 1 + 1 >= 0) System.arraycopy(aiTemp[y], 0, hiaiImage.get(iCurrLayer)[y], 0, iSizeX);
						}
					} catch (ArithmeticException e) {
						sLastStatus = "Mathematischer fehlschlag";
						throw new Exception(e);
					}
					return;
				default:
					sLastStatus = "Du musst schon eine\nvon den Optionen wählen";
			}
		}
	}

	private static double fnGaussianNormal(int x, int y, double p, double my_1, double my_2,
										   double sig_1, double sig_2, double coeff, double denom) {
		//
		// The math part that is required for the gaussian blur
		//
		double e = Math.E;

		//a(x,y)=Coeff ℯ^(Denom (((x-μ_{1})^(2))/(σ_{1}^(2))-2 p (x-μ_{1}) (y-μ_{2})/(σ_{1} σ_{2})+((y-μ_{2})^(2))/(σ_{2}^(2))))
		double pt1 = (Math.pow((x-p),2))/Math.pow(sig_1, 2);
		double pt2 = 2*p*(x-my_1)*((y-my_2)/(sig_1*sig_2));
		double pt3 = (Math.pow((y-my_2), 2))/(Math.pow(sig_2, 2));

		return (coeff * Math.pow(e, ((denom)/10)*(pt1-pt2+pt3)))+1;
	}

	// IMAGE RELATED METHODS ----------------- END

	public static void main (String[] args) {
		//
		// Entry point have fun...
		//
		fnCreateLayer(0, 300, 400);
		fnFillZeros();
		bFileLoaded = true;
		System.out.println(Arrays.toString(args));
		if (args.length > 0){
			String file = args[0];
			fnLoadFromFile(file);

			sLastStatus = "Bild geladen...";
		} else {
			sLastStatus = fnMotd();
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
			try {
				fnDrawMenu(asActions, null, null);
				sLastStatus = "";
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
						fnMenuConvolve();
						bFileSaved = false;
						break;
					case 's':
						fnSaveToFile();
						bFileSaved = true;
						break;
					case 'q':
						if (fnMenuQuit()) {
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
						sLastStatus = "Deine eingabe war fehlerhaft!!!";
				}
			} catch (Exception e){
				fnClear();
				System.out.println("Da ist wohl ein riesiger Fehler passiert (⊙.☉)7    ...\n"+
						"Kopiere den text auf deinem Bildschirm und reiche ihn ein THX.\n\n"+
						"--------------------------------------------------------------");
				e.printStackTrace();
				System.out.println("\n\n--------------------------------------------------------------" +
						"\n\n Möchtest du trotzdem fortfahren (J/N)");
				if (fnUserInput() != 'j'){running = false;}
			}
		}
		System.out.println("Bye");
		System.exit(0);
	}
}
