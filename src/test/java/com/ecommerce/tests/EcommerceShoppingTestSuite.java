package CS522FinalProject.tests;

import CS522FinalProject.utils.ExcelReader;
import CS522FinalProject.utils.ScreenshotUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.Level;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class EcommerceShoppingTestSuite {
    private static final Logger logger = LogManager.getLogger(EcommerceShoppingTestSuite.class);
    WebDriver driver;
    WebDriverWait wait;

    String driverPath = "C:\\BrowserDriversCS522\\chrome\\chromedriver.exe";
    String baseUrl = "https://www.ebay.com";
    String screenshotPath = "C:\\QA\\CS522Project\\ProjectScreenshoots\\";
    String excelFilePath = "C:\\QA\\CS522Project\\DataDriven\\Filters.xlsx";

    @BeforeClass
    public void setUp() {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);
        logger.info("Log4j2 configured at INFO level");

        System.setProperty("webdriver.chrome.driver", driverPath);
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        logger.info("Browser launched and maximized");
    }

    @DataProvider(name = "excelData")
    public Object[][] getExcelData() throws Exception {
        Object[][] sidebarData = ExcelReader.readSheetData("SidebarFilters");
        Object[][] variantData = ExcelReader.readSheetData("ProductVariants");

        int rows = Math.min(sidebarData.length, variantData.length);
        Object[][] testData = new Object[rows][6];

        for (int i = 0; i < rows; i++) {
            testData[i][0] = i;
            testData[i][1] = ((String) sidebarData[i][0]).trim();
            testData[i][2] = sidebarData[i][1];
            testData[i][3] = sidebarData[i][2];
            testData[i][4] = sidebarData[i][3];
            testData[i][5] = variantData[i];
        }

        return testData;
    }

    @Test(dataProvider = "excelData")
    public void runShoppingFlow(int rowIndex, String searchTerm, String storage, String model, String color, Object[] variant) throws Exception {
        String dropdownColor = (String) variant[0];
        String quantity = String.valueOf(variant[1]);

        driver.get(baseUrl);
        logger.info("Navigated to eBay");

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='gh-ac']")));
        searchBox.clear();
        searchBox.sendKeys(searchTerm, Keys.ENTER);
        logger.info("Searched for: {}", searchTerm);

        Assert.assertTrue(driver.getTitle().toLowerCase().contains(searchTerm.toLowerCase()), "Search page title doesn't contain search term.");

        ScreenshotUtil.takeScreenshot(driver, screenshotPath + "product" + (rowIndex + 1) + "_01_search.png");

        applySidebarFilter(storage, model, color);
        ScreenshotUtil.takeScreenshot(driver, screenshotPath + "product" + (rowIndex + 1) + "_02_filters.png");

        List<WebElement> items = driver.findElements(By.cssSelector("li.s-item"));
        Assert.assertTrue(items.size() > 0, "No products found after applying filters.");

        WebElement product = findLowestPricedProduct(searchTerm);
        Assert.assertNotNull(product, "No valid product found for the given search term and filters.");

        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0].href, '_blank');", product);
        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
        }
        logger.info("Opened product with lowest price in new tab");
        ScreenshotUtil.takeScreenshot(driver, screenshotPath + "product" + (rowIndex + 1) + "_03_product_detail.png");

        try {
            selectDropdownColor(dropdownColor);
        } catch (Exception e) {
            logger.info("No dropdown color option found.");
        }

        try {
            setQuantity(quantity);
        } catch (Exception e) {
            logger.info("No quantity option found.");
        }

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,250)");
        ScreenshotUtil.takeScreenshot(driver, screenshotPath + "product" + (rowIndex + 1) + "_04_before_add_to_cart.png");

        try {
            Thread.sleep(1000);
            WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='atcBtn_btn_1']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addToCartBtn);
            addToCartBtn.click();
            logger.info("Clicked Add to Cart");

            Thread.sleep(2000);
            ScreenshotUtil.takeScreenshot(driver, screenshotPath + "product" + (rowIndex + 1) + "_05_after_add_to_cart.png");

            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement proceedBtn = (WebElement) js.executeScript(
                    "return document.querySelector('#vas-interstitial-target-d')?.shadowRoot?.querySelector('#vas-spoke-container > div.bottom-ctas > div > button');"
            );

            if (proceedBtn != null) {
                js.executeScript("arguments[0].scrollIntoView(true);", proceedBtn);
                Thread.sleep(500);
                proceedBtn.click();
                logger.info("Clicked Proceed button inside Shadow DOM");
            } else {
                logger.info("No Shadow DOM Proceed button found.");
            }

            // Wait for cart page or fallback element
            try {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("cart"),
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Shopping cart') or contains(text(),'Cart')]"))
                ));
                logger.info("Cart page verified after Add to Cart / Proceed.");
            } catch (TimeoutException te) {
                logger.warn("Cart page was not detected after clicking Proceed.");
                ScreenshotUtil.takeScreenshot(driver, screenshotPath + "product" + (rowIndex + 1) + "_ERROR_cart_not_reached.png");
                Assert.fail("Cart page was not reached after adding the product.");
            }

            ScreenshotUtil.takeScreenshot(driver, screenshotPath + "product" + (rowIndex + 1) + "_06_cart_view.png");

        } catch (Exception e) {
            logger.warn("Add to cart or Proceed failed: {}", e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
        logger.info("Browser closed");
    }


    private void applySidebarFilter(String storage, String model, String color) throws InterruptedException {
        try {
            WebElement storageExpand = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='Storage Capacity']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", storageExpand);
            storageExpand.click();
            Thread.sleep(1000);
            WebElement storageCheckbox = driver.findElement(By.xpath("//span[text()='" + storage + "']/preceding::input[1]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", storageCheckbox);
            logger.info("Applied storage filter: {}", storage);
        } catch (Exception e) {
            logger.warn("Storage filter '{}' not found", storage);
        }

        try {
            WebElement modelExpand = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='Model']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", modelExpand);
            modelExpand.click();
            Thread.sleep(1000);
            WebElement modelCheckbox = driver.findElement(By.xpath("//span[text()='" + model + "']/preceding::input[1]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", modelCheckbox);
            logger.info("Applied model filter: {}", model);
        } catch (Exception e) {
            logger.warn("Model filter '{}' not found", model);
        }

        try {
            WebElement colorExpand = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='Color']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", colorExpand);
            colorExpand.click();
            Thread.sleep(1000);
            WebElement colorCheckbox = driver.findElement(By.xpath("//span[text()='" + color + "']/preceding::input[1]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", colorCheckbox);
            logger.info("Applied color filter: {}", color);
        } catch (Exception e) {
            logger.warn("Color filter '{}' not found", color);
        }
    }

    private WebElement findLowestPricedProduct(String expectedKeyword) {
        try {
            // wait for all items to load after filters
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.cssSelector("li.s-item")
            ));

            List<WebElement> items = driver.findElements(By.cssSelector("li.s-item"));
            double minPrice = Double.MAX_VALUE;
            WebElement cheapest = null;

            for (WebElement item : items) {
                // get title and normalize
                String title = item.findElement(By.cssSelector("a.s-item__link"))
                        .getText().toLowerCase();

                // include only true iPhone 16 Pro Max listings
                boolean hasImportantWords = title.contains("iphone")
                        && title.contains("16")
                        && title.contains("pro max");
                // exclude accessories, parts, covers, etc.
                boolean hasBadWords = title.contains("case")
                        || title.contains("cover")
                        || title.contains("charger")
                        || title.contains("protector")
                        || title.contains("tray")
                        || title.contains("housing")
                        || title.contains("replacement")
                        || title.contains("screen")
                        || title.contains("parts")
                        || title.contains("sim")
                        || title.contains("repair")
                        || title.contains("broken")
                        || title.contains("frame")
                        || title.contains("glass")
                        || title.contains("back")
                        || title.contains("battery")
                        || title.contains("shell")
                        || title.contains("flex");
                if (!hasImportantWords || hasBadWords) {
                    continue;
                }

                // extract and clean price text
                String priceText = item.findElement(By.cssSelector("span.s-item__price"))
                        .getText()
                        .replace("$", "")
                        .replace(",", "")
                        .split(" ")[0];
                // skip non-numeric or range values
                if (!priceText.matches("\\d+(\\.\\d+)?")) {
                    continue;
                }
                double price = Double.parseDouble(priceText);

                // pick the minimum
                if (price < minPrice) {
                    minPrice = price;
                    cheapest = item.findElement(By.cssSelector("a.s-item__link"));
                }
            }

            if (cheapest != null) {
                logger.info("Real product selected ({}): ${}", expectedKeyword, minPrice);
            } else {
                logger.warn(" No matching real product found for {}", expectedKeyword);
            }
            return cheapest;

        } catch (Exception e) {
            logger.error("Error finding lowest priced real product: {}", e.getMessage());
            return null;
        }
    }


    private void selectDropdownColor(String color) {
        try {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Select') or contains(@aria-label, 'Color')]")));
            dropdown.click();
            Thread.sleep(1000);
            WebElement colorOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@role='option' and .//span[contains(text(), '" + color + "')]]")));
            colorOption.click();
            logger.info("Selected color from dropdown: {}", color);
        } catch (Exception e) {
            logger.warn("Dropdown color '{}' not found or not available", color);
            logger.info("No color variant available");
        }
    }

    private void setQuantity(String qty) {
        try {
            WebElement qtyBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='qtyTextBox']")));
            qtyBox.clear();
            qtyBox.sendKeys(qty);
            logger.info("Set quantity to: {}", qty);
        } catch (Exception e) {
            logger.warn("Quantity box not found or not editable");
        }
    }
}
