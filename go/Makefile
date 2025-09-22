
deps:
	sudo apt-get update
	sudo apt install -y --no-install-recommends libcurl4-openssl-dev libgomp1 wget ca-certificates build-essential
	sudo cp chatbot-libs/* /usr/lib/
	sudo snap install hey

.PHONY: arm
arm:
	wget --no-check-certificate https://go.dev/dl/go1.22.3.linux-arm64.tar.gz
	sudo tar -C /usr/local -xzf go1.22.3.linux-arm64.tar.gz
	export PATH=$(PATH):/usr/local/go/bin && \
	CGO_ENABLED=1 GIN_MODE=release CPUARCH=arm64 CGO_LDFLAGS="-lchatbot-arm64" go build -o chatbot

.PHONY: intel
intel:
	wget --no-check-certificate https://go.dev/dl/go1.22.3.linux-amd64.tar.gz
	sudo tar -C /usr/local -xzf go1.22.3.linux-amd64.tar.gz
	export PATH=$(PATH):/usr/local/go/bin && \
	CGO_ENABLED=1 GIN_MODE=release CPUARCH=amd64 CGO_LDFLAGS="-lchatbot-amd64" go build -o chatbot

load-test:
	hey -m POST -n 10 -c 10 -H 'Content-Type: application/json' -d '{"Prompt":"[INST] How do I build a website in simple steps? [/INST]", "Tokens":25}' http://localhost:8081/generateResponse

simple-test:
	curl -X POST http://localhost:8081/generateResponse -H 'Content-Type: application/json' -d '{"Prompt":"[INST] How do I build a website in simple steps? [/INST]", "Tokens":25}'
