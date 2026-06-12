#!/usr/bin/env groovy

@Grab('com.codeborne:selenide:7.3.3')
@Grab('io.github.bonigarcia:webdrivermanager:5.8.0')

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import io.github.bonigarcia.wdm.WebDriverManager

import static com.codeborne.selenide.Condition.text
import static com.codeborne.selenide.Condition.visible
import static com.codeborne.selenide.Selenide.$
import static com.codeborne.selenide.Selenide.$$
import static com.codeborne.selenide.Selenide.open
import static com.codeborne.selenide.WebDriverRunner.url

// Usage: groovy create_users.groovy <url> <csv> <adminUser> <adminPass>
// Example: groovy create_users.groovy http://localhost:8080 users.csv admin password

if (args.length < 4) {
    println "Usage: groovy create_users.groovy <url> <csv> <adminUser> <adminPass>"
    System.exit(1)
}

String baseUrl = args[0]
String csvPath = args[1]
String adminUser = args[2]
String adminPass = args[3]

// Read CSV
def csvFile = new File(csvPath)
if (!csvFile.exists()) {
    println "ERROR: CSV file not found: ${csvPath}"
    System.exit(1)
}

def users = []
csvFile.withReader('UTF-8') { reader ->
    def lines = reader.readLines()
    lines.drop(1).each { line ->                        // skip header
        if (line.trim().empty) return
        def cols = line.split(',').collect { it.trim() }
        users << [
                firstname: cols[0],
                lastname : cols[1],
                email    : cols[2],
                role     : cols[3],
                password : cols[4]
        ]
    }
}

println "Loaded ${users.size()} users from ${csvPath}"

// Browser setup
WebDriverManager.chromedriver().setup()
Configuration.baseUrl = baseUrl
Configuration.holdBrowserOpen = false

// Disable Selenide reports
Configuration.reportsFolder = null
Configuration.downloadsFolder = null
Configuration.screenshots = false
Configuration.savePageSource = false

// Login
println "Logging in as ${adminUser}..."
open(baseUrl + '/login.do')
$('input[name="email"]').setValue(adminUser)
$('input[name="password"]').setValue(adminPass)
$('[name="login"]').click()
$('a[href*=\'/admin/\']').shouldBe(visible)
println "Logged in."

// Create users
def created = []
def skipped = [:]

users.each { user ->
    print "Creating ${user.firstname} ${user.lastname} (${user.email})... "
    try {
        open(baseUrl + '/admin/users.do')
        $('#create').shouldBe(visible).click()

        $('input[name="user.firstname"]').setValue(user.firstname)
        $('input[name="user.lastname"]').setValue(user.lastname)
        $('input[name="user.email"]').setValue(user.email)
        $('select[name="user.role"]').selectOption(user.role)
        $('input[name="user.password"]').setValue(user.password)
        $('input[name="password2"]').setValue(user.password)
        $('[name="save"]').click()

        // If still on the user form, something went wrong — read the alert
        Selenide.sleep(1_000) // temporary solution - find a better way
        if (!url().contains('users.do')) {
            def alertEl = $('.alert-danger span')
            def fieldErrors = $$('.invalid-feedback')
            println(alertEl)

            def alertExists = alertEl.exists();
            def fieldErrorsExist = !fieldErrors.isEmpty()
            def reason

            if (alertExists) {
                reason = alertEl.text()
            } else if (fieldErrorsExist) {
                reason = fieldErrors.texts().join("; ")
            } else {
                reason = 'unknown error'
            }

            println "SKIPPED (${reason})"
            skipped[user.email] = reason
            return
        }

        // Verify the user appears in the table
        $('input[type="search"]').setValue(user.email)
        def row = $$('table tbody tr').findBy(text(user.email))
        // check for email and firstname/lastname
        row.shouldHave(text(user.email))
        row.shouldHave(text("$user.firstname $user.lastname"))

        println "OK"
        created << user.email
    } catch (Exception e) {
        def reason = e.message?.readLines()?.first() ?: 'unknown error'
        println "SKIPPED (${reason})"
        skipped[user.email] = reason
    }
}

Selenide.closeWebDriver()

// Summary
println "\n── Summary ─────────────────────────────"
println "Created : ${created.size()}"
created.each { println "  ✓ ${it}" }
if (skipped) {
    println "Skipped : ${skipped.size()}"
    skipped.each { entry -> println "  ✗ ${entry.key} — ${entry.value}" }
}
