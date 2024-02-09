FROM ubuntu:20.04

ENV DEBIAN_FRONTEND noninteractive

# Install sysbench
RUN apt-get update && \
    apt-get install -y sysbench && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

CMD ["sleep", "infinity"]
