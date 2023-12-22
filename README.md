
# Overview

***

~~The goal of this project is to emulate, at a basic level, a financial trade message routing system where XML messages 
containing 'trade data' are picked up, processed into the recipient's XML data format, and delivered while persisting 
each original message locally as well as persisting the message's metadata and 'trade data' in a local, in-memory, H2 
Database.~~

* TODO: This is intended to overview of what I was doing, i.e. the project's prompt.

### Licenses

This project is licensed under Apache License Version 2.0.

# Usage 

***

This project uses Apache Camel to pick up XML 'trade messages' from a single 'inbound' FTP server, processing each message
into its recipient's XML data format while persisting the full message as well as the message's metadata and trade data, 
and deliver each message to the FTP server of its recipient.

The processing and persistence of each message is done via a series of individual Camel routes, each dedicated to
a single task. When a 'trade message' is pulled into the trade-message-router its filename is used to determine the 
message's routing and processing information. This information is stored in an external JSON file, 'HeaderPackets.json,' 
which contains entries for each sender and recipient specifying the Camel routes used to process the message and any 
instructions needed for those routes.

* TODO: 'This JSON file can be modified to ... and, remove or change message route through the routes' and same
with application.properties, etc.  !!!!!


### Sample Files and Configuration

Packaged with this project are a pair of sample XML 'trade messages,' a HeaderPackets.json file containing the routing 
and processing information for those messages, as well as the application.properties file and secrets.properties file for
use with these sample messages. These sample files utilize the full capabilities of the project, allowing the user to send, 
transform, persist, and deliver XML 'trade messages.'

The sample application.properties and the HeaderPackets.json files assume a linux system with the project's 
application.properties, secrets.properties, and HeaderPackets.json files sitting in a directory named 'trade-message-router' 
in the root directory. Further, the sample configuration does not use FTP servers and instead will pull the sample 
'Trade Messages' from a directory named 'InboundDirectory' that should also be placed in the above 'trade-message-router' 
directory. These sample 'trade messages' will also be persisted and sent to directories in the 'trade-message-router' 
directory.

Given the application.properties file is external the following command line argument is needed to run the project:

```-Dspring-boot.run.arguments=”--spring.config.location=/trade-message-router/application.properties”```

When the project is running the in-memory H2 database containing the message's metadata and trade data 
can be accessed through a browser via the address: http://localhost:8080/h2-console/. The JDBC URL should be set to: 
'jdbc:h2:mem:testdb' and by default there is no password. 


To change the sample files to use FTP, change the property 'inbound.message.location.uri' in the 'application.properties'
file and the 'RecipientAddress' field for each message in HeaderPackets.json to an Apache Camel FTP URI:
https://camel.apache.org/components/4.0.x/ftp-component.html

***
What I still need to cover:
1. "Report Generation Guide"
3. What each part of file name indicates?