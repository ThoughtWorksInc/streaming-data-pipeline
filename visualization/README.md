To Setup Tableau Desktop on Mac

1) Download TableauDesktop client 'https://www.tableau.com/products/desktop'
2) Follow the step for 'Amazon EMR Hadoop Hive' from link
https://www.tableau.com/en-us/support/drivers?edition=pro&lang=en-us&platform=mac&cpu=64&version=2019.2&__full-version=20192.19.0621.1547#
3) Make sure you are connected to corresponding VPN for UAT/PROD env
   and there is enough resources on EMR cluster to run HIVE queries
4) Open Tableau file from 'visualization' directory and you are ready to go !!

Pre-requisites

- Open 10000 thousand port on Driver to enable querying via Hive
- Run 'V1_create_table_freewheelers.sql' to create hive table pointing to HDFS mart folder
