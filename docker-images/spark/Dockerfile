FROM ubuntu
USER root

RUN apt-get update;
RUN apt-get install -y sudo wget openjdk-8-jre vim;

# install SPARK LIB
RUN wget https://downloads.apache.org/spark/spark-2.4.5/spark-2.4.5-bin-hadoop2.7.tgz
RUN tar -zxf spark-2.4.5-bin-hadoop2.7.tgz -C /usr/local/ ; rm spark-2.4.5-bin-hadoop2.7.tgz
RUN cd /usr/local && ln -s ./spark-2.4.5-bin-hadoop2.7 spark
ENV SPARK_HOME /usr/local/spark

# SPARK CONFIG FILES
ADD spark-configuration/spark-defaults.conf $SPARK_HOME/conf
ADD spark-configuration/spark-env.sh $SPARK_HOME/conf
ADD spark-configuration/workers $SPARK_HOME/conf

ENV PATH $PATH:$SPARK_HOME
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

#create main directory
RUN mkdir /target
RUN mkdir /tmp/spark-events

#external linking
VOLUME /target
WORKDIR /target

# SSH port
EXPOSE 22
# Spark remote debugging for intellij
EXPOSE 43211
# Spark History UI port
EXPOSE 18080
