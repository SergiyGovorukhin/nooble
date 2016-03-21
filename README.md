<h1>nooble - Simple Google clone project</h1>

Functionality:
 - Full-text indexing of a target page
 - Search for required word/fraze    

Frameworks 
 - Spring Boot
 - Spring MVC
 - Spring IoC
 - Lucene
 - Gradle
 - FreeMarker
 - Jsoup
 - lsf4j
 - java concurrency
 - java 8 (lambda, stream)
  
Web-server
 - Apache Tomcat 

IDE
 - InteliJ Idea 15
 

Quick start with gradle:

    win:    gradlew build
    unix:  ./gradlew build

Important: on unix you may need to add permissions to gradlew:

    chmod +x gradlew
 
Launch:

    java -jar build\libs\nooble.jar
      
Use:

    search: http://localhost:8080/
    index:  http://localhost:8080/index

Default search index directories specified in "lucene.properties":

    WIN: C:\temp\lucene\index
    UNIX: /var/tmp/lucene/index

Log 'nooble.log' is stored in project root
