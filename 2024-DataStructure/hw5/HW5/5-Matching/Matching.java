import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;

public class Matching
{
	private static Hashtable<Integer,myAVLtree> table;
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			try
			{
				String input = br.readLine();
				if (input.compareTo("QUIT") == 0)
					break;

				command(input);
			}
			catch (IOException e)
			{
				System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
			}
		}
	}

	private static void command(String input)
	{
		// TODO : 아래 문장을 삭제하고 구현해라.
		String[] newinput= input.split(" ",2);

		switch (newinput[0])
		{
			case "<":
				readData(newinput[1]);
				break;
			case "@":
				printData(Integer.parseInt(newinput[1]));
				break;
			case "?":
				findData(newinput[1]);
				break;
			case "/":
				deleteDate(newinput[1]);
				break;
			case "+":
				addData(newinput[1]);
				break;


		}
		//System.out.println("<< command 함수에서 " + input + " 명령을 처리할 예정입니다 >>");
	}

	private static void readData(String input)
	{
		System.out.println("readData");
		table=new Hashtable<Integer, myAVLtree>(100);
		try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null) {
				if (line.length() >= 6) {
					for (int i = 0; i <= line.length() - 6; i++) {
						String substring = line.substring(i, i + 6);
						int hashValue = hashing(substring);
						if (!table.containsKey(hashValue)) {
							table.put(hashValue, new myAVLtree<String>());
						}
						table.get(hashValue).insert(substring, new Loc(lineNumber, i + 1));
					}
				}
				lineNumber++;
			}
		} catch (IOException e) {
			System.out.println("파일을 읽는 도중 오류가 발생했습니다: " + e.getMessage());
		}


	}

	private static void printData(Integer input)
	{
		table.get(input).traversal();
	}

	private static void findData(String pattern) {
		if (pattern.length() < 6) {
			System.out.println("패턴의 길이는 6 이상이어야 합니다.");
			return;
		}

		StringBuilder result = new StringBuilder();
		boolean found = false;


		String subPattern = pattern.substring(0, 6);
		int hashValue = hashing(subPattern);

		if (table.containsKey(hashValue)) {
			myAVLNode<String> node = table.get(hashValue).search(subPattern);
			if (node.item != null) {
				for (Loc loc : node.locList) {
					if (checkFullPattern(loc, pattern)) {
						if (found) result.append(" ");
						result.append("(").append(loc.line).append(", ").append(loc.idx).append(")");
						found = true;
					}
				}
			}
		}


		if (!found) {
			System.out.println("(0, 0)");
		} else {
			System.out.println(result);
		}
	}

	private static boolean checkFullPattern(Loc startLoc, String pattern) {
		int patternLength = pattern.length();
		int currentIdx = startLoc.idx;
		int hashValue;
		String subString;

		for (int i = 6; i < patternLength; i += 6) {
			currentIdx += 6;
			if (i + 6 <= patternLength) {
				subString = pattern.substring(i, i + 6);
			} else {
				currentIdx = patternLength -6;
				subString = pattern.substring(patternLength-6, patternLength);
			}
			hashValue = hashing(subString);

			if (!table.containsKey(hashValue) ||
					table.get(hashValue).search(subString) == null ||
					!containsLocation(table.get(hashValue).search(subString).locList, startLoc.line, currentIdx)) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsLocation(LinkedList<Loc> locList, int line, int idx) {
		for (Loc loc : locList) {
			if (loc.line == line && loc.idx == idx) {
				return true;
			}
		}
		return false;
	}

	private static void deleteDate(String input)
	{

	}

	private static void addData(String input)
	{

	}

	private static int hashing(String input){
		int sum=0;
		if(input.length()!=6) System.err.println("잘못된 입력입니다.");
		for (int i=0; i<6;i++){
			sum+=input.charAt(i);
		}
		return sum%100;
	}


}
