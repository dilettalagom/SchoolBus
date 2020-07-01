FROM effeerre/kafka

USER root
RUN apk --update add openssh nmap nmap-scripts curl tcpdump bind-tools jq nmap-ncat && \
sed -i s/#PermitRootLogin.*/PermitRootLogin\ yes/ /etc/ssh/sshd_config && rm -rf /var/cache/apk/*

COPY config/server.properties /kafka/config/server.properties
COPY config/JMXconf.config /kafka/config/JMXconf.config
COPY config/update-config.sh /update-config.sh
COPY config/kafka-server-start.sh /kafka/bin/kafka-server-start.sh

RUN chmod 777 /kafka/bin/kafka-server-start.sh


EXPOSE 9092
#9999 JMX
ENTRYPOINT ["/bin/bash", "-c"]
CMD ["sh /update-config.sh && env JMX_PORT=9999 && /kafka/bin/kafka-server-start.sh /kafka/config/ds.properties"]