package com.free2wheelers.apps

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar._
import org.scalatest.{FeatureSpec, Matchers}

class FileValidatorTest extends FeatureSpec with Matchers {

    feature("validate timestamp of the file") {
        scenario("should return false if file is modified within 5 minutes") {
            val mockFileStatus = mock[FileStatus]

            val threeMinutesInMillis = 3 * 60 * 1000
            val nowMinusThreeMinutes = System.currentTimeMillis() - threeMinutesInMillis

            when(mockFileStatus.getModificationTime).thenReturn(nowMinusThreeMinutes)

            val result = FileValidator.isFileModifiedWithinTimeLimit(mockFileStatus)
            result("is_delayed") should equal(false)
            result should not contain key ("delayed_by_ms")
        }

        scenario("should return false if file is modified at exactly 5 minutes") {
            val mockFileStatus = mock[FileStatus]

            val fiveMinutesInMillis = 5 * 60 * 1000
            val nowMinusFiveMinutes = System.currentTimeMillis() - fiveMinutesInMillis

            when(mockFileStatus.getModificationTime).thenReturn(nowMinusFiveMinutes)

            val result = FileValidator.isFileModifiedWithinTimeLimit(mockFileStatus)
            result("is_delayed") should equal(false)
            result should not contain key ("delayed_by_ms")
        }

        scenario("should return true if file is modified more than 5 minutes ago") {
            val mockFileStatus = mock[FileStatus]

            val sixMinutesInMillis = 6 * 60 * 1000
            val nowMinusSixMinutes = System.currentTimeMillis() - sixMinutesInMillis

            when(mockFileStatus.getModificationTime).thenReturn(nowMinusSixMinutes)

            val result = FileValidator.isFileModifiedWithinTimeLimit(mockFileStatus)
            result("is_delayed") should equal(true)
            result("delayed_by_ms") should equal (sixMinutesInMillis)
        }
    }

    feature("does file exist") {
        val path = "hdfs://"
        val hdfs = mock[FileSystem]

        scenario("should not throw exception if file exist "){
            when(hdfs.exists(new Path(path))).thenReturn(true)

            noException should be thrownBy FileValidator.doesFileExist(path,hdfs)
        }

        scenario("should throw exception if file does not exist") {
            when(hdfs.exists(new Path(path))).thenReturn(false)

            the [RuntimeException] thrownBy FileValidator.doesFileExist(path,hdfs)
        }
    }


}
