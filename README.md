# streaming-data-pipeline
Streaming pipeline repo for data engineering training program

See producers and consumers set up README in their respective directories

# local environment setup
Make sure you have sbt installed.
Make sure you have docker installed.

Run `./sbin/buildAndRunLocal.sh` to create Docker containers for running and testing this setup on your local machine

To run the Streaming application in one Docker image run have to run inside each folder before run the docker container:

sbt assembly

To run the docker container you have available this commands:

make run => that run the container with all infrastructure needed 

make kill => kill the docker container

make console => access to the console of the docker container

