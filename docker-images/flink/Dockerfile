FROM flink:1.10.0-scala_2.11

USER root

RUN apt-get update
RUN apt-get install -y sudo python2.7 python-pip vim

RUN pip install requests

ENV FLINK_HOME /opt/flink

COPY conf/docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ADD conf/flink-conf.yaml $FLINK_HOME/conf/flink-conf.yaml

RUN mkdir $FLINK_HOME/flink-jar
RUN mkdir $FLINK_HOME/flink-jar/results
RUN chmod 777 $FLINK_HOME/flink-jar/results
VOLUME $FLINK_HOME/flink-jar
WORKDIR $FLINK_HOME/flink-jar


ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 6123 8081
CMD ["help"]