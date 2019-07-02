package com.free2wheelers.apps

import java.util.concurrent.TimeUnit

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}

class FileValidator {

    var LIMIT_IN_MINUTES = 5

    def isFileModifiedWithinTimeLimit(status: FileStatus): Map[String, AnyVal] = {
        val durationSinceLastModified = System.currentTimeMillis() - status.getModificationTime
        val durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(durationSinceLastModified)

        val isDelayed = durationInMinutes > LIMIT_IN_MINUTES
        var result: Map[String, AnyVal] = Map("is_delayed" -> isDelayed)
        if (isDelayed) {
            result += ("delayed_by_ms" -> durationSinceLastModified)
        }
        result
    }

    def doesFileExist(outputFile: String, hdfs: FileSystem) = {
        if (!hdfs.exists(new Path(outputFile))) {
            throw new RuntimeException("File does not exist")
        }
    }
}
