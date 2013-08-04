TML readme

Introduction

TML is a Text Mining library focused on calculating Semantic Spaces
using different factorization techniques (like SVD or NNMF), and
performing operations using those spaces like calculating distances
between documents.

Installation

0. If you are reading this file you might have unzipped the tml-xxx.zip file.

Put the content of the tml folder somewhere in your computer. We usually
put it in /opt/tml for Unix/Linux systems and in C:\opt\tml for
Windows systems.

This is important if you are planning to run tml from a different program.

1. Create a mysql database for TML to store its metadata. 

Log into mysql with the root user:

mysql -u root

 Execute the following commands:

CREATE DATABASE tml_metadata CHARACTER SET 'UTF8';

GRANT ALL PRIVILEGES ON tml_metadata.* TO 'tmluser'@'localhost' IDENTIFIED BY 'password';

FLUSH PRIVILEGES;

NOTE: You should use your own values for username and password.

2. Create the tables for TML metadata in MySql.

Run the following command (login into mysql if you loged yourself out).

USE tml_metadata;

SOURCE C:\opt\tml\db\defaultTML.sql

NOTE: Change the last command to reflect where did you put your tml folder.

3. Modify the tml.properties file to reflect your database.

Check the values for:
tml.database.url.db=//localhost/tml_metadata
tml.database.username=tmluser
tml.database.password=password

4. Modifiy the tml.folder property in the tml.properties file.

If you are running tml from the command line, you can keep the "." value (which
means the directory you are currently using).

However, if you are running TML from another program (like a Tomcat web application), you need
to put TML's jar wherever your app requires, and then the tml.properties file should
indicate where did you put your TML installation folder.

For example, if you put your folder in C:\opt\tml in windows, then you should change the
tml.properties file to reflect this, instead of using the default "." value.

5. Validate your installation.

From your tml folder run:

java -jar tml-xxx.jar -I -repo ./lucene --iclean --idocs ./corpora/uppsala

If everything is fine you should see:

2011-05-01 12:09:19,893  INFO [main] (Configuration.java:233) - ----------------------------------------------------
2011-05-01 12:09:19,893  INFO [main] (Configuration.java:234) - TML - Text Mining Library
2011-05-01 12:09:19,895  INFO [main] (Configuration.java:235) - Version: x.y initialized
2011-05-01 12:09:19,896  INFO [main] (Configuration.java:236) - ----------------------------------------------------
2011-05-01 12:09:19,898  INFO [main] (Configuration.java:140) - TML folder:             .
2011-05-01 12:09:20,072  INFO [main] (DbConnection.java:99) - Metadata:         Storing metadata info in DB com.mysql.jdbc.Driver at jdbc:mysql://localhost/tml_metadata
2011-05-01 12:09:20,073  INFO [main] (DbConnection.java:53) - Cleaning meta data storage, all documents will be lost!
2011-05-01 12:09:20,165  INFO [main] (Repository.java:419) - TML initialization
2011-05-01 12:09:20,166  INFO [main] (Repository.java:421) - Repository path:   ./lucene
2011-05-01 12:09:20,171  INFO [main] (Repository.java:425) - Repository:                Lucene initialized
2011-05-01 12:09:20,184  INFO [main] (DbConnection.java:99) - Metadata:         Storing metadata info in DB com.mysql.jdbc.Driver at jdbc:mysql://localhost/tml_metadata
2011-05-01 12:09:20,185  INFO [main] (Repository.java:528) - TML initialized
2011-05-01 12:09:21,986  INFO [main] (Repository.java:748) - Successfully added 3 documents in 1800 ms
TML finished successfully in 21.307394770000002 seconds. 