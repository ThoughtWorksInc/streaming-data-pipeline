#!/usr/bin/env bash

./sbin/buildProducer.sh && ./sbin/buildRawDataSaver.sh && ./sbin/buildStationConsumer.sh && ./sbin/buildFileChecker.sh