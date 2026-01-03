# MySQL Connector JAR Required

## Download Instructions

This folder should contain the MySQL JDBC driver JAR file.

### Step 1: Download MySQL Connector/J

**Option 1: Official Website**

1. Visit: https://dev.mysql.com/downloads/connector/j/
2. Select "Platform Independent" version
3. Download the ZIP archive
4. Extract the downloaded file
5. Copy `mysql-connector-j-8.2.0.jar` to this folder

**Option 2: Direct Download**

- Direct link: https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-j-8.2.0.zip

**Option 3: Maven Repository (Alternative)**

- Visit: https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.2.0/
- Download: `mysql-connector-j-8.2.0.jar`

### Step 2: Verify File Location

After downloading, this folder should contain:

```
lib/
└── mysql-connector-j-8.2.0.jar
```

### Step 3: Add to Eclipse Build Path

1. Right-click on project in Eclipse
2. Properties → Java Build Path
3. Libraries tab → Add JARs (or Add External JARs)
4. Navigate to this lib/ folder
5. Select mysql-connector-j-8.2.0.jar
6. Apply and Close

## File Information

**File Name**: `mysql-connector-j-8.2.0.jar`
**Version**: 8.2.0 (or latest compatible version)
**Size**: ~2.4 MB
**Type**: JAR (Java Archive)

## Alternative Versions

If you have a different version of MySQL Connector/J:

- MySQL Connector/J 8.x.x works with MySQL 8.0+
- Update the .classpath file if using a different version name

## Troubleshooting

**Error: "ClassNotFoundException: com.mysql.cj.jdbc.Driver"**
→ JAR file not in lib/ folder or not added to build path

**Error: "Build path is incomplete"**
→ Right-click project → Properties → Java Build Path → Libraries
→ Ensure mysql-connector-j-8.2.0.jar is listed

## Note

⚠️ The JAR file is NOT included in this repository due to licensing and size.
You must download it separately from the official MySQL website.

✅ The project will NOT compile without this JAR file.
