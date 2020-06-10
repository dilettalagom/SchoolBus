FROM alluxio/alluxio:1.8.1

USER root

COPY conf/entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]