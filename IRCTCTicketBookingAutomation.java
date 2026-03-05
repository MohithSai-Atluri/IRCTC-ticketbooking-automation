package irctc;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.io.FileHandler;

import java.io.File;

public class IRCTCTicketBookingAutomation implements Runnable{
	public WebDriver driver;
	
	public IRCTCTicketBookingAutomation() {
		ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<String, Object>();
        
        // To change user preferences itself because, using addArguments to do so will only stop the pop-up -
        // one time only, if the web site were to use a script to check again, the pop up will be displayed!
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);

        // For interacting with IRCTC by hiding/disabling WebDriver(Browser itself) native property called navigator.webDriver.
        options.addArguments("--disable-blink-features=AutomationControlled");
        
        this.driver = new ChromeDriver(options);
	}
	
	public IRCTCTicketBookingAutomation(String browser) {
		if(browser.equals("edge")) {
			EdgeOptions options = new EdgeOptions();

			Map<String, Object> prefs = new HashMap<String, Object>();
			prefs.put("profile.default_content_setting_values.notifications", 2);
			options.setExperimentalOption("prefs", prefs);

			options.addArguments("--disable-blink-features=AutomationControlled");

			this.driver = new EdgeDriver(options);
		} else if(browser.equals("chrome")) {
			ChromeOptions options = new ChromeOptions();
	        Map<String, Object> prefs = new HashMap<String, Object>();
	        
	        prefs.put("profile.default_content_setting_values.notifications", 2);
	        options.setExperimentalOption("prefs", prefs);

	        options.addArguments("--disable-blink-features=AutomationControlled");
	        
	        this.driver = new ChromeDriver(options);
		}
	}
	
	public void selectStationAt(WebElement textBox, String location, String station) {
		textBox.sendKeys(location);
		
		WebDriverWait dropdownResult = new WebDriverWait(this.driver, Duration.ofSeconds(15));
		dropdownResult.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(@class, 'ui-autocomplete-list-item')]/span[contains(text(),'" + station + "')]"))).click();
	} 
	
	@Override
    public void run() {
    	int count = 1;
    	
    	System.out.println("Starting automation on: " + Thread.currentThread().getName()+ " started at: " + System.currentTimeMillis());
        //this.driver.manage().window().maximize();

        System.out.println(((JavascriptExecutor) this.driver).executeScript("return navigator.webdriver;"));
        this.driver.get("https://www.irctc.co.in/nget/train-search");
        // Because sometimes, the script is executing before IRCTC loads completely and disrupting the flow of execution!
        WebDriverWait wait = new WebDriverWait(this.driver, Duration.ofSeconds(30));
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState;").equals("complete"));
        
        // AadharCard Validation notification is in a <div> which is overlayed on the <body>
        WebDriverWait ANAlert = new WebDriverWait(this.driver, Duration.ofSeconds(30));
        ANAlert.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".btn.btn-primary"))).click();
        
        // Because all From, to locations and date picker <input> are selected by using the same class.
        List<WebElement> locations = this.driver.findElements(By.xpath("//input[contains(@class,'ui-inputtext')]"));
        
        // To select From location
        ((JavascriptExecutor) this.driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'start'});", locations.get(0));
        this.selectStationAt(locations.get(0), "Hyd", "HYDERABAD DECAN - HYB");
        
        // To select destination
        this.selectStationAt(locations.get(1), "Pune", "PUNE JN - PUNE");
        
        LocalDate targetDate = LocalDate.now().plusDays(4);
        // Because it returns the date in yyyy/mm/dd format but the date field requires dd/mm/yyyy format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateInRequiredFormat = targetDate.format(formatter);
        
        WebElement travelDate = locations.get(2);
        /* 
           Since IRCTC uses Angular(PrimeNG) framework which doesnot "notice" the clear() method,
           So the typing must be done in order to trigger the event listener for a change in date to update the U.I.
           Yes, JavascriptExecutor will do the trick but the Angular event listener will not be triggered if -
           The value attribute of input tag is changed(set) directly with the required date. 
        */
        travelDate.sendKeys(Keys.CONTROL + "a");
        travelDate.sendKeys(Keys.BACK_SPACE);
        travelDate.sendKeys(dateInRequiredFormat);
        
        WebElement currDay = this.driver.findElement(By.xpath("//td[contains(@class, 'ui-datepicker-current-day')]"));
        currDay.click(); // To close date picker
        
        this.driver.findElement(By.xpath("//div/span[text() = 'All Classes']")).click();
        List<WebElement> compartmentClasses = this.driver.findElements(By.xpath("//div[contains(@class, 'ui-dropdown-items-wrapper')]/ul/p-dropdownitem/li/span"));
        
        for(WebElement compartmentClass: compartmentClasses) {
        	if(compartmentClass.getText().toLowerCase().contains("sleeper")) {
        		compartmentClass.click();
        		break;
        	}
        }

        //To select person with disability concession
        this.driver.findElement(By.cssSelector(".css-label_c.t_c")).click();
        
        // To click on the button to comply with the guidelines for conducting a medical check up for the person
        this.driver.findElement(By.xpath("//span[contains(@class, 'pi') and contains(@class, 'pi-check')]/parent::button")).click();
        
        // To find all trains for the specified criteria.
        this.driver.findElement(By.cssSelector(".search_btn.train_Search")).click();
        
        // To find all trains for the specified criteria
        String xpathForAvailableTrains = ".//div[contains(@class, 'form-group') and contains(@class, 'no-pad')]";
        WebDriverWait waitForAvailableTrains = new WebDriverWait(this.driver, Duration.ofSeconds(15));
        waitForAvailableTrains.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathForAvailableTrains)));
        
        // To Take a ScreenShot and also to Display details of the trains having the same From location specified because,
        // The result has trains starting from different locations but they are all going to the specified location.
        List<WebElement> availableTrains = this.driver.findElements(By.xpath(xpathForAvailableTrains));
        
        if(availableTrains.size() > 0) {
          System.out.println("Trains that are availble for the specified route upto the specified date are: ");
        } else {
        	System.out.println("Currently, there are no trains available for the specified requirements");
        }
        
        for(WebElement train: availableTrains) {
        	String name = train.findElement(By.xpath(".//div[contains(@class, 'train-heading')]")).getText();
        	String from = train.findElement(By.xpath(".//div[contains(@class, 'col-xs-5')]")).getText();
        	String to = train.findElement(By.xpath(".//div[contains(@class, 'col-xs-7')]")).getText();
        	
        	System.out.println(name + from + to);
        	if(to.contains("PUNE JN")) {
        		
        		((JavascriptExecutor) this.driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", train); 

        		File source = train.getScreenshotAs(OutputType.FILE);
        		String fileName = "Train_" + count + ".png";
        		
        		try {
        		  FileHandler.copy(source, new File("C:\\Temp Docs\\Assets\\images\\" + fileName));
        		  System.out.println("Saved screenshot: " + fileName + "\n");
          		  count++;
        		} catch(Exception e) {
        			System.out.println(e.getMessage());
        		}
        		    
        	}
        } // For loop closed
        this.driver.quit();
    }
}