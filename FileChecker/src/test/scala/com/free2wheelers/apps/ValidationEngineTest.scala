package com.free2wheelers.apps

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.DataFrame
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar._
import org.scalatest.{FeatureSpec, Matchers}

class ValidationEngineTest extends FeatureSpec with Matchers {

  feature("Checks output file") {
    val hdfs = mock[FileSystem]
    val outputFile = "/output/me/here"

    val fileStatus = mock[FileStatus]
    val stationReportValidator = mock[StationReportValidator]
    val fileValidator = mock[FileValidator]
    val stationMartDF = mock[DataFrame]
    val validationEngine = new ValidationEngine(stationReportValidator, fileValidator)

    scenario("No runtime exception is thrown when validation passes") {
      when(hdfs.getFileStatus(new Path(outputFile))).thenReturn(fileStatus)
      when(fileValidator.isFileModifiedWithinTimeLimit(fileStatus)).thenReturn(Map("is_delayed" -> false))
      when(stationReportValidator.validate(stationMartDF)).thenReturn(Map("IS_VALID" -> true))

      noException should be thrownBy validationEngine.checkFile(outputFile, stationMartDF, hdfs)
    }

    scenario("Runtime exception is thrown when external file timecheck validation does not pass") {
      when(hdfs.getFileStatus(new Path(outputFile))).thenReturn(fileStatus)
      when(fileValidator.isFileModifiedWithinTimeLimit(fileStatus)).thenReturn(Map("is_delayed" -> true))
      when(stationReportValidator.validate(stationMartDF)).thenReturn(Map("IS_VALID" -> true))

      the [RuntimeException] thrownBy validationEngine.checkFile(outputFile, stationMartDF, hdfs)

    }

    scenario("Runtime exception is thrown when content validation does not pass") {
      when(hdfs.getFileStatus(new Path(outputFile))).thenReturn(fileStatus)
      when(fileValidator.isFileModifiedWithinTimeLimit(fileStatus)).thenReturn(Map("is_delayed" -> false))
      when(stationReportValidator.validate(stationMartDF)).thenReturn(Map("IS_VALID" -> false))

      the [RuntimeException] thrownBy validationEngine.checkFile(outputFile, stationMartDF, hdfs)
    }
  }
}
