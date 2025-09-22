FROM public.ecr.aws/lts/ubuntu:22.04_stable

# Create app directory
WORKDIR /app/java

# Bundle app source
COPY chatbot-libs /usr/lib
COPY code /app/java

# Install app dependencies
RUN apt-get update && \
    apt install -y --no-install-recommends libcurl4-openssl-dev libgomp1 wget maven && \
    rm -rf /var/cache/apt && \
    apt-get clean

RUN if [ "$(uname -m)" = "aarch64" ]; \
    then export CPUARCH=aarch64; \
    else export CPUARCH=x64; \
    fi && \
    wget --no-check-certificate https://corretto.aws/downloads/latest/amazon-corretto-21-$CPUARCH-linux-jdk.deb && \
    dpkg -i amazon-corretto-21-$CPUARCH-linux-jdk.deb && \
    mvn clean package

EXPOSE 8081
CMD [ "mvn", "exec:java", "-Dexec.mainClass=chatbot.Main"]