
# Overview

***

This project is a simulation exercise using a prompt created by a senior developer friend based on his real-world experience 
(adjusted for introductory purposes). The goal of this project is to create an application that sends 'trade messages'
to and from the 'clients,' the 'Bank of Charles' (BOC) and the 'Zippo Stock Exchange' (ZSE), performing any necessary 
transformations and data persistence along the way. 

As part of the simulation, this friend answered any questions related to the specifications and function of the application  
as if the answers came from the client or the 'project manager.' The original project prompt, as well as all messages 
from the client and project manager are included in the 'project-files' directory.

# Usage 

***

This project uses Apache Camel to pick up XML trade messages from a single 'inbound' FTP server where the trade messages 
are deposited by the clients. When a typical trade message enters the application, first a full copy of it is persisted 
in a local directory, before it is transformed into the 'internal XML message format' where its metadata and trade data 
are then persisted in an in-memory H2 database. Once all internal processing is completed, the message is transformed 
into its recipient's XML message format, before finally the message is delivered to the FTP server of its recipient.

The processing and persistence of each message is done via a series of individual Apache Camel routes, each dedicated to
a single task. When a trade message is pulled into the trade-message-router its filename is used to determine the 
message's routing and processing information. This information is stored in an external JSON file, 'HeaderPackets.json,' 
which contains entries for each sender and recipient specifying the Apache Camel routes used to process the message and 
any instructions needed for those routes. 

### Basic Operating Principles 

* Trade message filenames must be in the following format: ```{Sender Client Code}_{Sender Filename Format}_{Destination 
Client Code}_{Message ID}.xml``` e.g. 'BOC_STD_MSG_ZSE_0123456789.xml' or 'ZSE_TRD_MSG_BOC_987654321.xml'
* When inbound trade messages are converted to the internal XML message format all monetary values are rounded to three 
digits of precision, the maximum used by the clients.

### Sample Files and Configuration

Packaged with this project are a pair of sample XML trade messages, a HeaderPackets.json file containing the routing 
and processing information for these messages, as well as the application.properties file and secrets.properties file for
use with these sample messages. These sample files utilize the full capabilities of the project, allowing the user to send, 
transform, persist, and deliver XML trade messages.

The sample application.properties and the HeaderPackets.json files assume a Linux system with the project's 
application.properties, secrets.properties, and HeaderPackets.json files sitting in a directory named 'trade-message-router' 
in the root directory. They also assume that the 'FullMessagePersistenceDirectory' is located in the 'trade-message-router' 
directory. Note that the 'FullMessagePersistenceDirectory' needs to be given write permissions for the application to be 
able to place the message into the directory.

As the application.properties file is external, the following command line argument is needed to run the project:

```-Dspring-boot.run.arguments=”--spring.config.location=/trade-message-router/application.properties”```

When the project is running, the in-memory H2 database containing the message's metadata and trade data can be accessed 
through a browser via the address: http://localhost:8080/h2-console/. The JDBC URL should be set to: 
```'jdbc:h2:mem:testdb'``` and by default there is no password. 

To keep the setup needed to run this project to a minimum, the provided sample configuration does not use FTP servers and 
instead will pull the sample trade messages from a directory named 'InboundDirectory' that should also be placed in the 
above 'trade-message-router' directory. Further, the recipient directories: 'BocDirectory' and 'ZseDirectory' must be 
included in the 'trade-message-router' directory with the proper write permissions for the application to be able to 
place the trade messages into these directories.

To change the sample files to use FTP, change the property 'inbound.message.location.uri' in the 'application.properties'
file and the 'RecipientAddress' field for each message in HeaderPackets.json to an Apache Camel FTP URI that points to your
FTP servers:
https://camel.apache.org/components/4.0.x/ftp-component.html

***

### Report Generation Guide 

In addition to the goals outlined in the project prompt, a subsequent message from the project manager requested a 
list of SQL queries that could be used by a 'business analyst' to compile a report on the data contained in the trade 
messages. The data sets requested, as well as the SQL queries needed to generate them are listed below:
1. The number transactions made of a particular asset. The below query is for all transactions of an asset with a 
'fdic_id' of 'TR02409438284'.  <br>
```SELECT asset_type, asset_id, fdic_id, COUNT(fdic_id) AS number_of_transactions FROM trade WHERE fdic_id = 'TR02409438284'``` 
2. The net value traded. The below query will produce the sum of all BUYs and the sum of all SELLs then subtract the sum 
of BUYs from the sum of SELLs to produce the report requirement of net value traded.  <br>
```SELECT (SELECT SUM(CAST(trade_value AS NUMERIC(20,3))) FROM trade WHERE trade_type = 'Sell') - (SELECT SUM(CAST(trade_value AS NUMERIC(20,3))) FROM trade WHERE trade_type = 'Buy') AS value_traded```
3. All trades of a single asset by a sender/recipient pair. The below query is for all transactions of an asset with a
'fdic_id' of 'TR02409438284' between the sender 'BCD' and the recipient 'JAX'.  <br>
```SELECT recipient_id, sender_id, asset_type, asset_id, fdic_id, COUNT(fdic_id) AS number_of_transactions FROM trade WHERE fdic_id = 'TR02409438284' AND recipient_id = 'JAX' AND sender_id = 'BCD'``` <br>
Note: The report requirements specify this will need to include the sender/recipient pair as one combined identifier: 
e.g. TradeInfo['from', 'to'] as in the BOC message format.
4. All trades of all assets by a sender/recipient pair. The below query is for all transactions between the sender 'BCD' 
and the recipient 'JAX'.  <br>
```SELECT recipient_id, sender_id, asset_type, asset_id, fdic_id FROM trade WHERE recipient_id = 'JAX' AND sender_id = 'BCD'```


***

### Licenses

This project is licensed under Apache License Version 2.0.

***