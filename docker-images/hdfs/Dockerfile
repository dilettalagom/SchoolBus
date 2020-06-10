# Creates pseudo distributed hadoop 3.1.2
#
# docker build -t effeerre/hadoop .

FROM effeerre/hadoop
USER root

RUN apt-get update;
#RUN apt-get install vi;

# HDFS CONFIG FILES
ADD hdfs-config/hadoop-env.sh $HADOOP_HOME/etc/hadoop/hadoop-env.sh
ADD hdfs-config/core-site.xml $HADOOP_HOME/etc/hadoop/core-site.xml
ADD hdfs-config/hdfs-site.xml $HADOOP_HOME/etc/hadoop/hdfs-site.xml
ADD hdfs-config/mapred-site.xml $HADOOP_HOME/etc/hadoop/mapred-site.xml
ADD hdfs-config/workers $HADOOP_HOME/etc/hadoop/workers
ENV PATH $PATH:$HADOOP_PREFIX/bin:$HADOOP_HOME

# BOOTSTRAP SERVICE SCRIPT
RUN mkdir /target
VOLUME /target
WORKDIR /target
RUN mkdir /results

ADD start-services.sh /target
RUN chmod 700 /target/start-services.sh
ADD stop-services.sh /target
RUN chmod 700 /target/stop-services.sh

ADD hdfs-config/bootstrap.sh /usr/local/bootstrap.sh
RUN chown root:root /usr/local/bootstrap.sh
RUN chmod 700 /usr/local/bootstrap.sh
ENV BOOTSTRAP /usr/local/bootstrap.sh
CMD /usr/local/bootstrap.sh


# HDFS ports
EXPOSE 9861 9862 9863 9870
# SSH port
EXPOSE 22