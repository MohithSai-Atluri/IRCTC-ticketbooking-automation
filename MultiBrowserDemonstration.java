package irctc;

import java.util.Scanner;

public class MultiBrowserDemonstration {
	
	static final String[] availableBrowsers = {"chrome", "edge"};
	
	static boolean isBrowserAvailable(final String name) {
		boolean available = false;
		
		for(String browser : MultiBrowserDemonstration.availableBrowsers) {
			if(browser.equals(name)) {
				available = true;
			}
		}
		return available;
	}
	
	static String getBrowserName(Scanner sc) {
		String name = "";
		
		System.out.println("Please choose a browser from the following:" + "\n1. Chrome" + "\n2. Edge");
		/*
		  If the control flow reaches the fun.call, the string is not null and empty so, we can convert it to lower case without any worry.
		  Because the control flow will return immediately if the string is null or empty since the order of evaluation is from left to right for 'Logical OR',
          It will never reach the fun.call to begin with!
        */
		while(name == null || name.equals("") || !name.matches("[a-zA-Z]+") || !MultiBrowserDemonstration.isBrowserAvailable(name.toLowerCase())) {
			System.out.println("Please enter a valid browser name: ");
			name = sc.nextLine().trim();
		}
		return name.toLowerCase();
	}
	
	static int getThreadCount(Scanner sc) {
		int threadCount = 0;
		while(threadCount < 1) {
			System.out.println("Please enter a valid thread count: ");
			threadCount = sc.nextInt();
		}
		return threadCount;
	}
	
	static int limitThreadsTo(Scanner sc, int max) {
		int requiredBrowserThreadCount = 0;
		while((requiredBrowserThreadCount = MultiBrowserDemonstration.getThreadCount(sc)) > max) {
			System.out.println("Sorry, System is only able to spin up " + max + " Threads atmost at the moment!\nSo please enter thread count accordingly!");
		}
		return requiredBrowserThreadCount;
	}
	
	public static void main(String args[]) throws InterruptedException{
		int requiredThreads = 0, max = 4;
		Scanner sc = new Scanner(System.in);
		
		requiredThreads = MultiBrowserDemonstration.limitThreadsTo(sc, max);
		sc.nextLine(); // Cleans up input buffer, so we can take strings as input.
		String browsers[] = new String[requiredThreads];
		
		for(int i = 0; i < requiredThreads; i++) {
		  System.out.println("Enter browser " + (i + 1) + ":");
		  browsers[i] = MultiBrowserDemonstration.getBrowserName(sc);
		}
		
		sc.close();
		
		for(int count = 0; count < requiredThreads; count++) {
			new Thread(new IRCTCTicketBookingAutomation(browsers[count]), (browsers[count] + (count + 1))).start();
		} 
		
	}
}