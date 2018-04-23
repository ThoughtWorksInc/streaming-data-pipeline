zk_command="docker exec -it streamingdatapipeline_zookeeper_1 zkCli.sh -server localhost:2181"
echo $zk_command
$zk_command create /free2wheelers1 ''
$zk_command create /free2wheelers1/statusinformation ''
$zk_command create /free2wheelers1/statusinformation/kafka-brokers localhost:9092
$zk_command create /free2wheelers1/statusinformation/topic status_information
$zk_command create /free2wheelers1/statusinformation/checkpointLocation file:///Users/alpandya/dev-eng/data/stationinformationcheckpoint
$zk_command create /free2wheelers1/statusinformation/dataLocation file:///Users/alpandya/dev-eng/data/stationinformationchedata
