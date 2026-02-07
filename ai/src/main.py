from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
# 1. 환경변수 로드
load_dotenv()


from routers import style_router
import uvicorn

# 2. 앱 초기화
app = FastAPI(
    title="Virtual Try-On API",
    description="Spring Boot에서 요청을 받아 옷 추천 및 가상 피팅을 수행하는 AI 서버",
    version="1.0.0"
)

# 3. CORS 설정 
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 4. 라우터 등록 
app.include_router(style_router.router)

# 5. 서버 구동 여부 체크용 기본 엔드포인트
@app.get("/")
def server_check():
    return {"status": "ok", "message": "AI Server is running!"}

# 6. 실행 코드 
if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)