# BusEasy — Quick Start Guide

## Prerequisites
| Tool | Version | Download |
|---|---|---|
| Java JDK | 17+ | https://adoptium.net |
| Apache Maven | 3.9+ | https://maven.apache.org/download.cgi |
| XAMPP | Any | https://www.apachefriends.org |

---

## Step 1 — Start XAMPP MySQL
1. Open XAMPP Control Panel
2. Click **Start** next to **MySQL**

---

## Step 2 — Create the Database
Open phpMyAdmin (http://localhost/phpmyadmin) or a MySQL CLI, then run:

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/seed.sql
```

Or paste the contents of each file directly into phpMyAdmin's SQL tab.

---

## Step 3 — Configure Database Connection
Edit `src/main/resources/db.properties` if your MySQL credentials differ from the defaults:
```
db.host=localhost
db.port=3306
db.name=buseasy
db.user=root
db.password=        ← leave blank if XAMPP root has no password
```

---

## Step 4 — Build & Run

```bash
mvn package
java -jar target/buseasy.jar
```

Or run inside an IDE (IntelliJ IDEA / Eclipse / VS Code with Java Extension Pack):
- Open the project folder
- Run `com.buseasy.Main`

---

## Demo Accounts (from seed.sql)
| Username | Password    |
|----------|-------------|
| alice    | password123 |
| bob      | password123 |
| charlie  | password123 |
