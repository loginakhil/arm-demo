FROM public.ecr.aws/lts/ubuntu:22.04_stable

# Create app directory
WORKDIR /app/go

# Bundle app source
COPY chatbot-libs /usr/lib
COPY code /app/go

ENV GIN_MODE=release
ENV CGO_ENABLED=1

# Install app dependencies
RUN apt-get update && \
    apt install -y --no-install-recommends libcurl4-openssl-dev libgomp1 wget ca-certificates build-essential && \
    rm -rf /var/cache/apt && \
    apt-get clean

RUN if [ "$(uname -m)" = "aarch64" ]; \
    then export CPUARCH=arm64; \
    else export CPUARCH=amd64; \
    fi && \
    wget --no-check-certificate https://go.dev/dl/go1.22.3.linux-$CPUARCH.tar.gz && \
    tar -C /usr/local -xzf go1.22.3.linux-$CPUARCH.tar.gz && \
    export PATH=$PATH:/usr/local/go/bin && \
    rm go1.22.3.linux-$CPUARCH.tar.gz && \
    CGO_LDFLAGS="-lchatbot-$CPUARCH" go build -o chatbot

EXPOSE 8081
CMD [ "./chatbot"]
