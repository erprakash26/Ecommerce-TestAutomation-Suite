package CS522FinalProject.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;

public class ScreenshotUtil {
    private static final Logger logger = LogManager.getLogger(ScreenshotUtil.class);

    public static void takeScreenshot(WebDriver driver, String filePath) {
        try {
            TakesScreenshot scrShot = ((TakesScreenshot) driver);
            File src = scrShot.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(src, new File(filePath));
            logger.info("Screenshot saved to: " + filePath);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
        }
    }
}
