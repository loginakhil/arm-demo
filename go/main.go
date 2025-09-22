package main

// struct chatbot_response {
//    char* output;
//    float inference_time;
//    int num_tokens;
// };
// int init();
// int predict(const char* prompt, int n_predict, struct chatbot_response* response);
// int clean();
import "C"

import (
	"net/http"
	"strconv"
	"sync"

	"github.com/gin-gonic/gin"
)

var mutex sync.Mutex

type ChatbotRequest struct {
	Prompt string `json:"Prompt" binding:"required"`
	Tokens int    `json:"Tokens" binding:"required"`
}

func helloWorld() func(*gin.Context) {
	return func(c *gin.Context) {
		c.String(http.StatusOK, "Welcome to the Graviton Developer Day!")
	}
}

func generateResponse() func(*gin.Context) {
	return func(c *gin.Context) {
		var request ChatbotRequest

		if err := c.BindJSON(&request); err != nil {
			c.AbortWithError(http.StatusBadRequest, err)
			return
		}

		mutex.Lock()

		response := C.struct_chatbot_response{}
		C.predict(C.CString(request.Prompt), C.int(request.Tokens), &response)

		defer mutex.Unlock()

		prompt_response := C.GoString(response.output)
		total_time := response.inference_time
		total_tokens := response.num_tokens

		c.Data(http.StatusOK,
			"application/json; charset=utf-8",
			[]byte("\nResponse: "+
				prompt_response+
				"\n\nInference Time: "+
				strconv.FormatFloat(float64(total_time), 'f', -1, 64)+
				" ms | Total Tokens: "+
				strconv.Itoa(int(total_tokens))+
				"\n"),
		)
	}
}

func main() {
	C.init()

	router := gin.New()
	router.GET("/", helloWorld())
	router.POST("/generateResponse", generateResponse())

	router.Run("0.0.0.0:8081")
	C.clean()
}
