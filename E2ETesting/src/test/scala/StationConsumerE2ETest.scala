import java.net.URI
import java.time.LocalDateTime

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SparkSession
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class StationConsumerE2ETest extends FeatureSpec with Matchers with GivenWhenThen {

  feature("Station consumer consumes station_sf kafka topic to produce station mart data") {

    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()

    scenario("station mart should have correct data for sf station") {
      val stationMartDf = spark.read
        .format("csv")
        .option("header", "true") //first line in file has headers
        .option("inferSchema", "true")
        .load(getPathToOutputFile)

      import spark.implicits._

      val expectedDF = Seq((12)).toDF("free_bikes")
      val actualFreeBikesDF = stationMartDf.filter($"station_id" === "c8131aed6f3df2f78149eb338df66e66").select($"bikes_available");

      assert(actualFreeBikesDF.except(expectedDF).count() === 0)

    }

    def getPathToOutputFile(): String = {
      def getPath(year: Int, month: Int, day: Int, hour: Int, minute: Int): String =
        s"year=$year/month=$month/day=$day/hour=$hour/minute=$minute/"

      val now = LocalDateTime.now
      val hdfsPath = getPath(now.getYear, now.getMonthValue, now.getDayOfMonth, now.getHour, now.getMinute)

      val hostPath = "hdfs://hadoop:9000/"
      val fs = FileSystem.get(new URI(hostPath), new Configuration())
      val path = new Path(s"${hostPath}free2wheelers/stationMart/data/$hdfsPath")

      s"${fs.listStatus(path)(0).getPath}/*.csv"
    }
  }

}