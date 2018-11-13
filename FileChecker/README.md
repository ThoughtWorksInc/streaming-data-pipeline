File Checker checks if the csv file has been published in the stationMart, validates if there is a long and lat fields, validates duplicates based on the station id

to test locally you can run from File Checker root directory:
spark-submit --class com.free2wheelers.apps.FileChecker --master local target/scala-2.11/free2wheelers-file-checker_2.11-0.0.1.jar ./src/test/resources/test.csv