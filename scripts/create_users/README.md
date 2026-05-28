# create_users.groovy

A Groovy script that automates bulk user creation via a web UI using Selenide (Chrome browser automation). It reads users from a CSV file and creates each one through the admin panel of your application.

---

## Prerequisites

### 1. Install Java

Groovy requires Java (JDK 11 or later). Check if you already have it:

```bash
java -version
```

If not installed, download it from [https://adoptium.net](https://adoptium.net) and follow the installer for your OS.

---

### 2. Install Groovy

#### macOS

The easiest way is via [Homebrew](https://brew.sh):

```bash
brew install groovy
```

Verify the installation:

```bash
groovy --version
```

#### Windows

1. Download the latest Groovy binary installer from [https://groovy.apache.org/download.html](https://groovy.apache.org/download.html)
2. Run the installer
3. Ensure `GROOVY_HOME` is set as an environment variable pointing to the install folder
4. Add `%GROOVY_HOME%\bin` to your system `PATH`
5. Open a new Command Prompt and verify:

```cmd
groovy --version
```

---

### 3. Install Google Chrome

The script drives a Chrome browser automatically. Make sure [Google Chrome](https://www.google.com/chrome/) is installed on your machine. The script handles the ChromeDriver automatically — no manual setup needed.

---

## CSV Format

Prepare a CSV file with the following columns (header row required):

```
firstname,lastname,email,role,password
John,Doe,john.doe@example.com,Admin,secret123
Jane,Smith,jane.smith@example.com,User,secret456
```

| Column      | Description                              |
|-------------|------------------------------------------|
| `firstname` | User's first name                        |
| `lastname`  | User's last name                         |
| `email`     | User's email address (used as login)     |
| `role`      | Role as it appears in the app's dropdown |
| `password`  | Initial password for the user            |

---

## Usage

```bash
groovy create_users.groovy <url> <csv> <adminUser> <adminPass>
```

| Argument    | Description                        |
|-------------|------------------------------------|
| `url`       | Base URL of the IPT                |
| `csv`       | Path to the CSV file               |
| `adminUser` | Admin account email used to log in |
| `adminPass` | Admin account password             |

**Example:**

```bash
groovy create_users.groovy http://localhost:8080 users.csv admin@company.com mypassword
```

---

## What the Script Does

1. Reads all users from the CSV file
2. Logs into the application using the provided admin credentials
3. For each user, navigates to the admin user creation page and fills in the form
4. Verifies the user was created successfully by searching the users table
5. Prints a summary of created and skipped users at the end

---

## Output Example

```
Loaded 3 users from users.csv
Logging in as admin@company.com...
Logged in.
Creating John Doe (john.doe@example.com)... OK
Creating Jane Smith (jane.smith@example.com)... OK
Creating Bob Brown (bob.brown@example.com)... SKIPPED (Email already exists)

── Summary ─────────────────────────────
Created : 2
  ✓ john.doe@example.com
  ✓ jane.smith@example.com
Skipped : 1
  ✗ bob.brown@example.com — Email already exists
```

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `groovy: command not found` | Groovy is not on your PATH — re-check the install steps above |
| `java: command not found` | Install Java first (see Prerequisites) |
| Chrome doesn't open | Ensure Google Chrome is installed |
| Login fails | Double-check the `adminUser` and `adminPass` arguments |
| User skipped with unknown error | Check that the `role` value matches exactly what appears in the app's dropdown |
