FROM flink:1.8-scala_2.11

USER root

RUN apt-get update
RUN apt-get install -y sudo python2.7 python-pip vim

RUN pip install requests

 # hadoop
#RUN wget https://downloads.apache.org/hadoop/common/hadoop-3.2.1/hadoop-3.2.1.tar.gz ; tar -zxf hadoop-3.2.1.tar.gz -C /usr/local/ ; rm hadoop-3.2.1.tar.gz
#RUN cd /usr/local && ln -s ./hadoop-3.2.1 hadoop

ENV HADOOP_COMMON_HOME /usr/local/hadoop
ENV HADOOP_HDFS_HOME /usr/local/hadoop
ENV HADOOP_HOME /usr/local/hadoop
ENV HADOOP_MAPRED_HOME /usr/local/hadoop
ENV HADOOP_YARN_HOME /usr/local/hadoop
ENV HADOOP_CONF_DIR /usr/local/hadoop/etc/hadoop
ENV PATH $PATH:$HADOOP_HOME/bin
ENV FLINK_HOME /opt/flink
ENV HDFS_NAMENODE_USER "root"
ENV HDFS_DATANODE_USER "root"
ENV HDFS_SECONDARYNAMENODE_USER "root"

ADD hdfs-config/core-site.xml $HADOOP_CONF_DIR/core-site.xml
ADD hdfs-config/hdfs-site.xml $HADOOP_CONF_DIR/hdfs-site.xml

RUN chmod 777 $HADOOP_CONF_DIR/core-site.xml
RUN chmod 777 $HADOOP_CONF_DIR/hdfs-site.xml

COPY conf/docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ADD conf/flink-conf.yaml $FLINK_HOME/conf/flink-conf.yaml

RUN mkdir $FLINK_HOME/flink-jar
RUN mkdir $FLINK_HOME/flink-jar/results
RUN chmod 777 $FLINK_HOME/flink-jar/results
VOLUME $FLINK_HOME/flink-jar
WORKDIR $FLINK_HOME/flink-jar

#COPY external_jar_lib/*.jar $FLINK_HOME/lib/

ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 6123 8081
CMD ["help"]