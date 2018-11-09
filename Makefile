build:
	@docker build -f Dockerfile docker -t sdp

kill:
	@docker rm -f sdp

run: build
	@docker run -v ${PWD}/RawDataSaver/target/scala-2.11:/apps/RawDataSaver/target/scala-2.11 -v ${PWD}/CitibikeApiProducer/build/libs:/apps/CitibikeApiProducer/build/libs -v ${PWD}/StationConsumer/target/scala-2.11/:/apps/StationConsumer/target/scala-2.11 -v ${PWD}/StationTransformerNYC/target/scala-2.11:/apps/StationTransformerNYC/target/scala-2.11 -p 8080:8080 -p 9092:9092 -p 7077:7077 -p 2181:2181 -p 18080:18080 --rm --name sdp -d sdp

console:
	@docker exec -it sdp  bash
