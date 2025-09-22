
deps:
	sudo apt-get update
	sudo apt install -y --no-install-recommends python3-pip libcurl4-openssl-dev libgomp1
	sudo cp chatbot-libs/* /usr/lib/
	pip install -r requirements.txt
	sudo snap install hey

.PHONY: arm
arm:
	python3 main.py

.PHONY: intel
intel:
	python3 main.py

load-test:
	hey -m POST -n 10 -c 10 -H 'Content-Type: application/json' -d '{"Prompt":"[INST] How do I build a website in simple steps? [/INST]", "Tokens":25}' http://localhost:8081/generateResponse

simple-test:
	curl -X POST http://localhost:8081/generateResponse -H 'Content-Type: application/json' -d '{"Prompt":"[INST] How do I build a website in simple steps? [/INST]", "Tokens":25}'
