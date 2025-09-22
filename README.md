# 🛒 Ecommerce-TestAutomation-Suite

This repository contains Project for Software Quality Assurance & Test Automation.
It is a comprehensive Selenium TestNG automation framework that simulates a real-world E-commerce shopping workflow on Ebay.

The framework demonstrates end-to-end test automation practices, including data-driven testing with Excel, logging, exception handling, screenshots, and modular test execution. It was built as part of my graduate coursework but is structured to reflect industry-level QA automation standards.

---

## 📌 Project Goals

The primary objective of this project is to design and implement a robust and reusable automation framework that:

- Automates a real-world shopping scenario on Ebay.
- Applies data-driven testing using Excel sheets instead of hardcoding values.
- Ensures scalability and maintainability by organizing tests and utilities with best practices.
- Captures logs and screenshots for traceability and reporting.
- Demonstrates how modern QA teams build automation frameworks for e-commerce platforms.

---

## 🔹 End-to-End Flow Covered

The test suite automates the following Ebay Holiday Shopping scenario:

### Product Search
- Reads product name (e.g., iPhone 16 Pro Max) from Excel input.
- Searches for the product on Ebay.

### Filter Application
Applies filters dynamically in this order:
- Storage Capacity (e.g., 512GB, 256GB)
- Model (e.g., iPhone 16 Pro Max)
- Color (e.g., Black, Blue)

### Product Selection
- Extracts product prices from the filtered results.
- Selects either the lowest priced or highest priced product depending on test input.

### Product Details Page
- Handles product variants such as color selection or quantity via dropdowns.

### Add to Cart
- Clicks Add to Cart.
- If Ebay shows an Additional Services popup (inside Shadow DOM), the script:
  - Detects it dynamically.
  - Clicks the Proceed button inside Shadow DOM.

### Validation
- Navigates to cart confirmation.
- Captures a screenshot as evidence.
- Logs success/failure of each step.

---

## 🔹 Key Features

- ✅ Data-Driven Testing – Uses Apache POI to read Excel sheets (SidebarFilters.xlsx, ProductVariants.xlsx).
- ✅ Dynamic Filtering – Automatically expands and applies filters from the sidebar.
- ✅ Smart Product Selection – Extracts prices and chooses best product dynamically.
- ✅ Product Variants – Handles dropdown selections on product detail pages.
- ✅ Shadow DOM Handling – Detects and interacts with hidden elements (e.g., Proceed button).
- ✅ Logging with Log4j2 – Every step logged for debugging & analysis.
- ✅ Screenshot Capture – Automatic screenshots stored in /screenshots/.
- ✅ Reusable Utilities – ExcelReader, ScreenshotUtil, logging setup.
- ✅ TestNG Integration – Modular test methods with annotations (@Test, @BeforeClass, etc.).
- ✅ Exception Handling – Try/catch with assertion recovery for stability.

---

## ⚙️ Tech Stack

- Language: Java
- Automation Frameworks: Selenium WebDriver, TestNG
- Data Handling: Apache POI (Excel)
- Logging: Log4j2
- Build Tool: Maven
- IDE: IntelliJ IDEA / VS Code
- Reports: TestNG HTML reports + captured screenshots

---

## 📂 Project Structure

```
Ecommerce-TestAutomation-Suite/
├── src/
│   ├── main/java/com/ecommerce/utils/
│   │   ├── ExcelReader.java         # Reads test data from Excel
│   │   ├── ScreenshotUtil.java      # Utility for screenshots
│   │   └── log4j.properties         # Logging configuration
│   │
│   └── test/java/com/ecommerce/tests/
│       ├── EcommerceShoppingTestSuite.java   # Main test class
│       ├── TestProceedShadowDOM.java         # Handles Shadow DOM popup
│       ├── EbayHolidayShoppingF1.java        # Supporting versions
│       ├── EbayHolidayShoppingBF.java
│       ├── EbayHolidayShoppingSD.java
│       └── (other supporting tests if needed)
│
├── ExcelFiles/
│   ├── SidebarFilters.xlsx           # Sidebar filter inputs
│   └── ProductVariants.xlsx          # Product variant inputs
│
├── screenshots/                      # Captured screenshots
├── logs/                             # Log files
│
├── pom.xml                           # Maven dependencies
├── testng.xml                        # TestNG suite runner
├── README.md                         # Documentation
└── .gitignore                        # Ignore unnecessary files
```

---

## 🚀 How to Run

### Clone the repository
```bash
git clone https://github.com/<your-username>/Ecommerce-TestAutomation-Suite.git
cd Ecommerce-TestAutomation-Suite
```

### Open the project in IntelliJ IDEA or VS Code.

### Install dependencies
```bash
mvn clean install
```

### Run the test suite
```bash
mvn test
```
Or run directly from **testng.xml** inside your IDE.

---

## 📸 Sample Outputs

- Screenshots are saved in `/screenshots/` at each major step.
- Logs are saved in `/logs/` for traceability.
- Test Reports are generated by TestNG under `/test-output/`.

Example evidence includes:  
- Search results with applied filters  
- Product details before cart  
- Added-to-cart confirmation page  

---

## 🧾 Test Case Matrix

The Excel-based Test Case Matrix provides:  
- Input filters: Storage, Model, Color  
- Product variants: Color dropdown, Quantity  
- Expected Result vs Actual Result  
- Pass/Fail status  
- Comments for each execution  

This ensures complete **traceability between requirements and test results**.  

---

## 🎯 Learning Outcomes

From this project, I demonstrated the ability to:  
- Build a data-driven automation framework from scratch.  
- Apply QA automation best practices such as POM structure, logging, reusable utilities.  
- Handle real-world challenges like Shadow DOM, dynamic waits, and Excel-driven inputs.  
- Produce auditable test artifacts (logs, screenshots, reports).  
- Deliver a project that reflects industry-level automation frameworks.  

---

## 👨‍💻 Author

**Prakash Bhandari**  
Computer Engineer
Specializations: **QA Automation | Machine Learning | Data Engineering**  

---

## 🏷️ Tags

`selenium` `java` `testng` `qa-automation` `excel` `log4j2` `maven` `automation-framework` `ecommerce`
