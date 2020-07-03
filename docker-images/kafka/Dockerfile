FROM effeerre/kafka

USER root
RUN apk --update add openssh nmap nmap-scripts curl tcpdump bind-tools jq nmap-ncat && \
sed -i s/#PermitRootLogin.*/PermitRootLogin\ yes/ /etc/ssh/sshd_config && rm -rf /var/cache/apk/*
RUN apk add openjdk8


RUN wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.6/jmx_prometheus_javaagent-0.6.jar
RUN wget https://raw.githubusercontent.com/prometheus/jmx_exporter/master/example_configs/kafka-0-8-2.yml

ENV JAVA_HOME /opt/jdk
ENV PATH ${PATH}:${JAVA_HOME}/bin

COPY config/server.properties /kafka/config/server.properties
COPY config/JMXconf.properties /kafka/config/JMXconf.properties
COPY config/update-config.sh /update-config.sh
COPY config/kafka-server-start.sh /kafka/bin/kafka-server-start.sh

RUN chmod 777 /kafka/bin/kafka-server-start.sh

ENV KAFKA_JMX_OPTS "-Djava.rmi.server.hostname=0.0.0.0 -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.rmi.port=9999"
ENV KAFKA_JMX_PORT 9999

EXPOSE 9092 9999 9992
#9999 JMX
ENTRYPOINT ["/bin/bash", "-c"]
CMD ["sh /update-config.sh && /kafka/bin/kafka-server-start.sh /kafka/config/ds.properties"]