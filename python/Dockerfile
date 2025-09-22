FROM public.ecr.aws/lts/ubuntu:22.04_stable

# Create app directory
WORKDIR /app/python

# Bundle app source
COPY chatbot-libs /usr/lib
COPY code /app/python

# Install app dependencies
RUN apt update && \
    apt install -y --no-install-recommends python3-pip libcurl4-openssl-dev libgomp1 && \
    pip install -r requirements.txt && \
    rm -rf /var/cache/apt && \
    apt-get clean

EXPOSE 8080
CMD [ "python3", "main.py" ]