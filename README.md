# HKUST-Room-Lookup
## Compile to JAR
On Unix:

```
./gradlew dist
```
On Windows:
```
gradlew.bat dist
```

Jar is named HKUST-Room-Lookup-1.0.jar, in the directory build/libs/.

First run 
```
java -jar HKUST-Room-Lookup-1.0.jar --fetch
```
This will prompt you to download the html files from UST's website.

Then, run
```
BOT-TOKEN="Insert Your Token Here" java -jar HKUST-Room-Lookup-1.0.jar
```
This will launch the bot process.
```
```
