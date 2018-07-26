# Local set up
  1. Make sure you have sbt installed, if not, do `brew install sbt`
  2. If you use IntelliJ: make sure you have sbt plugin and Scala plugin installed on IntelliJ
  3. Import project in IntelliJ from sbt
  4. Mark directories as sources / resources / tests as necessary
  5. To package the jar, in the RawDataSaver directory run `sbt package`
  6. To run the jar, see the spark-submit command in Dockerfile