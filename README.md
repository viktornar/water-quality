# Water quality analysis
## What this project about

The purpose of this project is to try spark with kafkawith zeppelin by implement POC and application for such steps:
* Set up kafka, spark and zeppelin with docker
* Create a new Kafka topic called: seb-demo
* Read Waterbase_v2018_1_T_WISE4_AggregatedData.csv file in /opt/data/initial with Spark
* Convert cvs file to avro file in /opt/data/target
* Read avro file that was stored in /opt/data/target
* Create structured streaming by following these rules:
    1. Use target file as input
    2. Proceed with data aggregations, data cleansing, or similar to analyse the data by country. 
   3. Publish the analysis results to the Kafka topic seb-demo
* Ensure that streaming pipeline is working. This will be required during the exercise review.

## Requirements

You need to have linux with:
* docker version 20.10.5
* version 1.24.1
Set up is not working with MacOS and probably will not work with Windows. In current set up I'm using bridge network set up with imap and on Mac it is not working.
  
## Docker

Spark, kafka, zeppelin are set up with docker compose. For easy set up and automate some repetitive tasks you can use **op.sh** shell script.

Initial data (csv), converted files (avro), zeppelin notebooks are placed in data and mapped respectively:

```yaml
volumes:
  - ./data/.cache:/.cache
  - ./data/.local:/.local
  - ./data/notebook:/opt/zeppelin/notebook
  - ./data/conf:/opt/zeppelin/conf
  - ./data/initial:/opt/data/initial
  - ./data/target:/opt/data/target
  - ./target/scala-2.12:/opt/data/work
  - spark:/spark
```

```shell
cd [project root]
# Will download containers, set up network and create kafka topic `seb-demo`
./op.sh compose=up
# Will destroy everything
./op.sh compose=down
# Will pause containers
./op.sh compose=stop
# Will wake up containers
./op.sh compose=start
# Display topic in kafka
./op.sh topic=list
# Will create seb-demo topic
./op.sh topic=seb-demo
# Will create kafka producer for testing
./op.sh producer=seb-demo
# Will create kafka consumer for testing
./op.sh producer=seb-demo
# Will submit spark application for reading csv, writing avro, aggregation and publishing to topic 
./op.sh spark=submit-producer
# Will submit spark application for stream from kafka topic and displaying data in ascii table :D
./op.sh spark=submit-consumer
```
Some examples:

![op.sh](images/op.sh_helper.png)

## POC

For quick prototyping I have used Zeppelin with Analyses/POC notebook where all basic steps are implemented

![Zeppelin POC](images/zeppelin_poc.png)

## Spark application

Basic steps are also implemented with Spark applications in **src** folder as **WaterQualityProducer.scala** **WaterQualityConsumer.scala**

**WaterQualityProducer.scala** will read csv, convert it to avro, read avro, do some normalization, aggregation and publish to seb-demo topic.

This application can be published by using:
```shell
./op.sh spark=submit-producer
```

**WaterQualityConsumer.scala** will subscribe to kafka topic **seb-demo** and will display batch as ascii table.

```shell
./op.sh spark=submit-consumer
```

Result of applications:

![Spark app results](images/spark_consumer_producer.png)

It is also possible to read kafka stream and display in pie chart with Zeppelin notebook **Analyses/Water prie chart**

![Zeppelin pie chart](images/zeppelin_prie_chart.png)