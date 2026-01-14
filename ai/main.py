from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

# Spring Boot에서 보낼 데이터 구조 정의
class AIRequest(BaseModel):
    prompt: str

@app.get("/")
def read_root():
    return {"message": "AI Server is running"}

@app.post("/predict")
async def predict(request: AIRequest):
    # 여기에 AI 모델 추론 로직이 들어갑니다.
    # 지금은 테스트용 가짜 응답을 보냅니다.
    result = f"AI가 처리한 결과: {request.prompt}"
    return {"status": "success", "data": result}