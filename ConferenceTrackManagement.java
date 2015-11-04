import java.io.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
public class ConferenceTrackManagement 
{
//declaring global variables	
private static String[] titleArray;
private static int[] timeArray;
private static JFrame frame;
private static JTextArea textArea;
private static JLabel label;
private static String inputText, outputText = "";
private static boolean invalidOutput = false;
private static final int maxTrackTime = 420;
private static int startIndex = -1, endIndex = -1, time;

	public static void main(String args[])  throws IOException
	{
		//creatring the jframe to display the result on
		frame = new JFrame("Conference track management");
		//creating the text area for the output
        textArea = new JTextArea(30, 40);
        label = new JLabel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 2, 5));
        panel.add(label, BorderLayout.WEST);
        //making the text area scrollable
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new EmptyBorder(2, 5, 5, 5));
        //adding everything to the frame
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        //beginning the logic to calculate the result
        computeResult();
        label.setText("Test Output:");
        //putting the result into the text area
        textArea.setText(outputText);
        //making sure the user can't change the output themselves
        textArea.setEditable(false);
        //displaying the frame
        frame.setVisible(true);
	}

	//the start of the logic
	private static void computeResult()  throws IOException
	{
		//Input file is added here
		//calling a method to read the file into a string
    	inputText = readFile("testinput.txt");
    	getTitleAndTimeArray();
    	if (!invalidOutput) 
    	{
        	getTracks();
    	}
	}

	//method for reading in the file
	private static String readFile(String fileName) throws IOException 
	{
    	BufferedReader br = new BufferedReader(new FileReader(fileName));
    	try 
    	{
        	StringBuilder sb = new StringBuilder();
        	String line = br.readLine();
        	while (line != null) 
        	{
            	sb.append(line);
            	sb.append("\n");
            	line = br.readLine();
        	}
        	return sb.toString();
    	} 
    	finally 
    	{
        	br.close();
    	}
	}
	
	//a method to store the title and time in arrays
	private static void getTitleAndTimeArray() 
	{
		//storing each line of the file in an array
    	String lines[] = inputText.split("[\\r\\n]+");
    	//creating an array to store the talk name
    	titleArray = new String[lines.length];
    	//creating an array to store the talk time
    	timeArray = new int[lines.length];
    	for (int i = 0; i < lines.length; i++) 
    	{
	    	//getting a single line from the file
        	String line = lines[i];
        	line = line.trim();
        	int lastIndexOfSpace = line.lastIndexOf(' ');
        	//putting the title into the array
        	titleArray[i] = line.substring(0, lastIndexOfSpace);
        	//getting the time for the current line
        	String currentTime = line.substring(lastIndexOfSpace + 1);
        	currentTime = currentTime.toLowerCase();
        	//checking which type of time it is
        	if (currentTime.endsWith("lightning"))
            	setTitleAndTime(i, currentTime, "lightning", 5);
        	else if (currentTime.endsWith("min"))
            	setTitleAndTime(i, currentTime, "min", 1);
        	else 
        	{
            	setOutputAsInvalid(i);
        	}
    	}
	}
	
	//setting the time for the current line
	private static void setTitleAndTime(int i, String currentTime, String type, int scale) 
	{
		//getting the first part of the time eg. 45
    	String timeValue = currentTime.substring(0, currentTime.indexOf(type));
    	if (timeValue.equals(""))
        	timeArray[i] = scale;
    	else 
    	{
        	try 
        	{
	        	//setting the time in the time array
            	timeArray[i] = Integer.parseInt(timeValue) * scale;
        	} 
        	catch (Exception e) 
        	{
            	setOutputAsInvalid(i);
        	}
    	}
	}
	
	//displaying an error if the file input is incorrect
	private static void setOutputAsInvalid(int lineNumber) 
	{
    	outputText += "Invalid time entered in line " + (lineNumber + 1) + "\n";
    	invalidOutput = true;
	}

	private static void getTracks() 
	{
		//getting the time of all of the talks
    	int totalTime = getTotalTime();
    	int requiredTracks = totalTime / maxTrackTime + 1;
    	//sorting the arrays based on their time from shortest to longest
    	sortTitleAndTimeArray();
    	for (int i = 1; i <= requiredTracks; i++) 
    	{
	    	//filling the morning slot
        	boolean found = fillMorning(timeArray, 180); 
        	if (found) 
        	{
            	time = 9 * 60;
            	outputText += "\nTrack " + i + ":\n\n";
            	//displaying each talk for this slot
            	for (int j = startIndex; j <= endIndex; j++) 
            	{
                	outputText += timeStamp(time, "AM") + " " + titleArray[j] + "\n";
                	time += timeArray[j];
            	}
            	//removing the talks already scheduled
            	deleteScheduled();
            	outputText += "12:00PM Lunch";
            	//filling the evening slot
            	boolean relativeFound = fillEvening(timeArray, 180, 240);
            	if (relativeFound) 
            	{
                	time = 60;
                	outputText += "\n";
                	for (int j = startIndex; j <= endIndex; j++) 
                	{
                    	outputText += timeStamp(time, "PM") + " " + titleArray[j] + "\n";
                    	time += timeArray[j];
                	}
                	deleteScheduled();
                	outputText += timeStamp(time, "PM") + " Networking Event" + "\n";
                	outputText += "\n";
            	}
        	} 
        	else
            	outputText = "No solution found.";
    	}
	}
	
	//getting the total time of all talks
	private static int getTotalTime() 
	{
    	int timeSum = 0;
    	for (int i = 0; i < timeArray.length; i++)
        	timeSum += timeArray[i];
    	return timeSum;
	}
	
	//a method for sorting the arrays based on their time from shortest to longest using bubble sort
	private static void sortTitleAndTimeArray() 
	{
    	for (int i = 1; i < timeArray.length; i++) 
    	{
        	for (int j = 0; j < timeArray.length - i; j++) 
        	{
            	if (timeArray[j] > timeArray[j + 1]) 
            	{
                	int tempTime = timeArray[j];
                	timeArray[j] = timeArray[j + 1];
                	timeArray[j + 1] = tempTime;
                	String tempEvent = titleArray[j];
                	titleArray[j] = titleArray[j + 1];
                	titleArray[j + 1] = tempEvent;
            	}
        	}
    	}
	}
	
	//a method to fill the morning slot with talks
	private static boolean fillMorning(int array[], int sum) 
	{
    	if (!(array.length >= 1))
        	return false;
    	int currentSum = array[0], start = 0;
    	for (int i = 1; i <= array.length; i++) 
    	{
        	while (currentSum > sum && start < i - 1) 
        	{
            	currentSum -= array[start];
            	start++;
        	}
        	if (currentSum == sum) 
        	{
            	startIndex = start;
            	endIndex = i - 1;
            	return true;
        	}
        	if (i < array.length)
            	currentSum += array[i];
    	}
    	return false;
	}
	
	//a method for displaying the time depending on if it is morning or evening
	private static String timeStamp(int time, String mode) 
	{
    	String timeStamp = "";
    	int hours = time / 60;
    	int minutes = time % 60;
    	String hourHint = "", minuteHint = "";
    	if (hours < 10)
        	hourHint = "0";
    	if (minutes < 10)
        	minuteHint = "0";
    	timeStamp = hourHint + hours + ":" + minuteHint + minutes + mode;
    	return timeStamp;
	}
	
	//a method for deleting talks that are already scheduled
	private static void deleteScheduled() 
	{
    	String[] temptitleArray = new String[titleArray.length - (endIndex - startIndex) - 1];
    	int[] tempTimeArray = new int[temptitleArray.length];
    	int index = 0;
    	for (int j = 0; j < startIndex; j++) 
    	{
        	temptitleArray[index] = titleArray[j];
        	tempTimeArray[index] = timeArray[j];
        	index++;
    	}
    	for (int j = endIndex + 1; j < titleArray.length; j++) 
    	{
        	temptitleArray[index] = titleArray[j];
        	tempTimeArray[index] = timeArray[j];
        	index++;
    	}
    	timeArray = tempTimeArray;
    	titleArray = temptitleArray;
	}

	//a method to fill the evening slot with talks
	private static boolean fillEvening(int array[], int minSum, int maxSum) 
	{
    	if (!(array.length >= 1))
        	return false;
    	int currentSum = array[0], start = 0;
    	for (int i = 1; i <= array.length; i++) 
    	{
        	while (currentSum > maxSum && start < i - 1) 
        	{
            	currentSum -= array[start];
            	start++;
        	}
        	if (currentSum >= minSum && currentSum <= maxSum) 
        	{
            	startIndex = start;
            	endIndex = i - 1;
            	return true;
        	}
        	if (i < array.length)
            	currentSum += array[i];
    	}
    	return false;
	}
}