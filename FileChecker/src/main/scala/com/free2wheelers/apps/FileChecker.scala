package com.free2wheelers.apps

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.SparkSession

object FileChecker {
    def main(args: Array[String]): Unit = {

        val outputFile = args(0)

        val spark = SparkSession.builder.appName("FileCheckerApp").getOrCreate()

        val hdfs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
        val status = hdfs.getFileStatus(new Path(outputFile))

        FileValidator.doesFileExist(outputFile, hdfs)
        validateFileExternals(status)
        validateFileContents(spark, outputFile)
    }

    private def validateFileContents(spark: SparkSession, outputFile: String) = {
        val stationMartDF = spark.read.option("header", "true").csv(outputFile)
        val fileContentValidationResult = new StationReportValidator(spark).validate(stationMartDF)
        if (fileContentValidationResult("IS_VALID") == false) {
            throw new RuntimeException("File is not valid : " + fileContentValidationResult.toString())
        }
    }

    private def validateFileExternals(status: FileStatus) = {
        val fileTimeValidationResult = FileValidator.isFileModifiedWithinTimeLimit(status)
        if (fileTimeValidationResult("is_delayed") == true) {
            throw new RuntimeException("File is too old")
        }
    }
}
