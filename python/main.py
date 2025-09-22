import ctypes
import platform
from flask import Flask, request
from waitress import serve

HTTP_STATUS_OK = 200

# Global
app = Flask(__name__)

class ChatbotResponse(ctypes.Structure):
    _fields_ = [
        ("output", ctypes.c_char_p),
        ("inference_time", ctypes.c_float),
        ("num_tokens", ctypes.c_int)
    ]

def create_chatbot():
    if platform.processor() == "x86_64":
        chatbotlib = ctypes.CDLL("libchatbot-amd64.so")
    else:
        chatbotlib = ctypes.CDLL("libchatbot-arm64.so")

    chatbotlib.init.argtypes = []
    chatbotlib.init.restype = ctypes.c_int32
    chatbotlib.predict.argtypes = [ctypes.c_char_p, ctypes.c_int32, ctypes.POINTER(ChatbotResponse)]
    chatbotlib.predict.restype = ctypes.c_int32
    chatbotlib.clean.restype = ctypes.c_int32

    return chatbotlib

chatbotlib = create_chatbot()
chatbotlib.init()

@app.route("/")
def hello_world():
    return "<p>Welcome to the Graviton Developer Day!</p>"

@app.route("/generateResponse", methods=['POST'])
def generate_response():
    prompt = request.get_json()["Prompt"]
    if prompt is None or len(prompt) == 0:
        raise ValueError("Invalid prompt")

    max_tokens = request.get_json()["Tokens"]
    if max_tokens is None or not isinstance(max_tokens, int):
        raise ValueError("Invalid value for max tokens")

    response = ChatbotResponse()
    chatbotlib.predict(prompt.encode(), max_tokens, ctypes.byref(response))

    prompt_response = response.output.decode()
    total_time = response.inference_time
    total_tokens = response.num_tokens

    if prompt_response is None or len(prompt_response) == 0:
        raise ValueError("Invalid response")

    return f"\nResponse: {prompt_response}\n\nInference Time: {total_time} ms | Total Tokens: {total_tokens}\n", HTTP_STATUS_OK

def main():
    serve(app, host="0.0.0.0", port=8081, threads=1) # process only one request at a time
    chatbotlib.clean()

main()
