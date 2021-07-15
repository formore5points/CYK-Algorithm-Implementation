import java.io.*;
import java.util.*;

public class CYK {
//PROGRAM IS CORRECTLY WORKING WITH CNF
	public static String word;
	public static String startingSymbol;
	public static boolean isTokenWord = false;
	public static ArrayList<String> terminals = new ArrayList<String>();
	public static ArrayList<String> nonTerminals = new ArrayList<String>();
	public static TreeMap<String, ArrayList<String>> grammar = new TreeMap<>();

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: java CYK <File> <Word>.");
			System.exit(1);
		} else if (args.length > 2) {
			isTokenWord = true;
		}

		doSteps(args);

	}

	public static String convertFile() throws IOException {

		File file = new File("CFG_1.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;

		ArrayList<String> nonTerminal = new ArrayList<>();
		ArrayList<Character> Terminal = new ArrayList<>();

		while ((st = br.readLine()) != null) {
			String ba[] = st.split(">");
			nonTerminal.add(ba[0]);
			String bb[] = ba[1].split("\\|");

			for (int i = 0; i < bb.length; i++) {
				char a[] = bb[i].toCharArray();
				for (int j = 0; j < a.length; j++) {
					if (!(Terminal.contains(a[j])) && ((Character.isLowerCase(a[j]))
							|| (!Character.isLowerCase(a[j]) && !Character.isUpperCase(a[j])))) {
						Terminal.add(a[j]);
					}
				}
			}
		}

		br = new BufferedReader(new FileReader(file));
		ArrayList<String> rules = new ArrayList<>();

		while ((st = br.readLine()) != null) {
			st = st.replace('>', ' ');
			st = st.replace('|', ' ');
			rules.add(st);
		}

		String startValue = nonTerminal.get(0);

		FileWriter fileWriter = new FileWriter("newfile.txt");
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println(startValue);

		for (int i = 0; i < Terminal.size(); i++) {
			if ((i + 1) > Terminal.size()) {
				printWriter.print(Terminal.get(i));
			} else
				printWriter.print(Terminal.get(i) + " ");
		}
		printWriter.println();

		for (int i = 0; i < nonTerminal.size(); i++) {
			if ((i + 1) > Terminal.size()) {
				printWriter.print(nonTerminal.get(i));
			} else
				printWriter.print(nonTerminal.get(i) + " ");
		}
		printWriter.println();

		for (int i = 0; i < rules.size(); i++) {
			printWriter.println(rules.get(i));
		}
		printWriter.close();

		String result = "newfile.txt";
		return result;
	}

	public static void doSteps(String[] args) throws IOException {
		args[0] = convertFile();
		parseGrammar(args);
		String[][] cykTable = createCYKTable();
		printResult(doCyk(cykTable));
	}

	public static void parseGrammar(String[] args) {
		Scanner input = openFile(args[0]);
		ArrayList<String> tmp = new ArrayList<>();
		int line = 2;

		word = getWord(args);
		startingSymbol = input.next();
		input.nextLine();

		while (input.hasNextLine() && line <= 3) {
			tmp.addAll(Arrays.<String>asList(toArray(input.nextLine())));
			if (line == 2) {
				terminals.addAll(tmp);
			}
			if (line == 3) {
				nonTerminals.addAll(tmp);
			}
			tmp.clear();
			line++;
		}

		while (input.hasNextLine()) {
			tmp.addAll(Arrays.<String>asList(toArray(input.nextLine())));
			String leftSide = tmp.get(0);
			tmp.remove(0);
			grammar.put(leftSide, new ArrayList<String>());
			grammar.get(leftSide).addAll(tmp);
			tmp.clear();
		}
		input.close();
	}

	public static String getWord(String[] args) {
		if (!isTokenWord) {
			return args[1];
		}
		String[] argsWithoutFile = new String[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			argsWithoutFile[i - 1] = args[i];
		}
		return toString(argsWithoutFile);
	}

	public static void printResult(String[][] cykTable) {
		System.out.println("Word: " + word);
		drawTable(cykTable);
	}

	public static void drawTable(String[][] cykTable) {
		int l = findLongestString(cykTable) + 2;
		String formatString = "| %-" + l + "s ";
		String s = "";
		StringBuilder sb = new StringBuilder();
		// Building Table Structure Modules
		sb.append("+");
		for (int x = 0; x <= l + 2; x++) {
			if (x == l + 2) {
				sb.append("+");
			} else {
				sb.append("-");
			}
		}
		String low = sb.toString();
		sb.delete(0, 1);
		String lowRight = sb.toString();
		// Print Table
		// Step 4: Evaluate success.
		if (cykTable[cykTable.length - 1][cykTable[cykTable.length - 1].length - 1].contains(startingSymbol)) {
			System.out.println("The word \"" + word + "\" is an element of the CFG G and can be derived from it.");
		} else {
			System.out.println(
					"The word \"" + word + "\" is not an element of the CFG G and can not be derived from it.");
		}
	}

	public static int findLongestString(String[][] cykTable) {
		int x = 0;
		for (String[] s : cykTable) {
			for (String d : s) {
				if (d.length() > x) {
					x = d.length();
				}
			}
		}
		return x;
	}

	public static String[][] createCYKTable() {
		int length = isTokenWord ? toArray(word).length : word.length();
		String[][] cykTable = new String[length + 1][];
		cykTable[0] = new String[length];
		for (int i = 1; i < cykTable.length; i++) {
			cykTable[i] = new String[length - (i - 1)];
		}
		for (int i = 1; i < cykTable.length; i++) {
			for (int j = 0; j < cykTable[i].length; j++) {
				cykTable[i][j] = "";
			}
		}
		return cykTable;
	}

	public static String[][] doCyk(String[][] cykTable) {
		// Step 1: Fill header row
		for (int i = 0; i < cykTable[0].length; i++) {
			cykTable[0][i] = manageWord(word, i);
		}
		// Step 2: Get productions for terminals
		for (int i = 0; i < cykTable[1].length; i++) {
			String[] validCombinations = checkIfProduces(new String[] { cykTable[0][i] });
			cykTable[1][i] = toString(validCombinations);
		}
		if (word.length() <= 1) {
			return cykTable;
		}
		// Step 3: Get productions for subwords with the length of 2
		for (int i = 0; i < cykTable[2].length; i++) {
			String[] downwards = toArray(cykTable[1][i]);
			String[] diagonal = toArray(cykTable[1][i + 1]);
			String[] validCombinations = checkIfProduces(getAllCombinations(downwards, diagonal));
			cykTable[2][i] = toString(validCombinations);
		}
		if (word.length() <= 2) {
			return cykTable;
		}
		// Step 3: Get productions for subwords with the length of n
		TreeSet<String> currentValues = new TreeSet<String>();

		for (int i = 3; i < cykTable.length; i++) {
			for (int j = 0; j < cykTable[i].length; j++) {
				for (int compareFrom = 1; compareFrom < i; compareFrom++) {
					String[] downwards = cykTable[compareFrom][j].split("\\s");
					String[] diagonal = cykTable[i - compareFrom][j + compareFrom].split("\\s");
					String[] combinations = getAllCombinations(downwards, diagonal);
					String[] validCombinations = checkIfProduces(combinations);
					if (cykTable[i][j].isEmpty()) {
						cykTable[i][j] = toString(validCombinations);
					} else {
						String[] oldValues = toArray(cykTable[i][j]);
						ArrayList<String> newValues = new ArrayList<String>(Arrays.asList(oldValues));
						newValues.addAll(Arrays.asList(validCombinations));
						currentValues.addAll(newValues);
						cykTable[i][j] = toString(currentValues.toArray(new String[currentValues.size()]));
					}
				}
				currentValues.clear();
			}
		}
		return cykTable;
	}

	public static String manageWord(String word, int position) {
		if (!isTokenWord) {
			return Character.toString(word.charAt(position));
		}
		return toArray(word)[position];
	}

	public static String[] checkIfProduces(String[] toCheck) {
		ArrayList<String> storage = new ArrayList<>();
		for (String s : grammar.keySet()) {
			for (String current : toCheck) {
				if (grammar.get(s).contains(current)) {
					storage.add(s);
				}
			}
		}
		if (storage.size() == 0) {
			return new String[] {};
		}
		return storage.toArray(new String[storage.size()]);
	}

	public static String[] getAllCombinations(String[] from, String[] to) {
		int length = from.length * to.length;
		int counter = 0;
		String[] combinations = new String[length];
		if (length == 0) {
			return combinations;
		}
		;
		for (int i = 0; i < from.length; i++) {
			for (int j = 0; j < to.length; j++) {
				combinations[counter] = from[i] + to[j];
				counter++;
			}
		}
		return combinations;
	}

	public static String toString(String[] input) {
		return Arrays.toString(input).replaceAll("[\\[\\]\\,]", "");
	}

	public static String[] toArray(String input) {
		return input.split("\\s");
	}

	public static Scanner openFile(String file) {
		try {
			return new Scanner(new File(file));
		} catch (FileNotFoundException e) {
			System.out.println("Error: Can't find or open the file: " + file + ".");
			System.exit(1);
			return null;
		}
	}
}
