from fastapi import APIRouter, UploadFile, File, Response, HTTPException, Form
from typing import List, Optional
from services.recommendation_service import RecommendationService
from services.virtual_fitting_service import VirtualFittingService
from services.masking_service import MaskingService
from services.editing_service import EditingService
from pydantic import BaseModel
from PIL import Image
import io
import os
import httpx
import asyncio

router = APIRouter()

# 서비스 초기화 (API 키 로드)
API_KEY = os.getenv("API_KEY")
recommender = RecommendationService(API_KEY)
virtual_fitting = VirtualFittingService(API_KEY)
masking = MaskingService()
editing = EditingService(API_KEY)


# ai 추천 - request body 정의
class AiRecommendationRequest(BaseModel):
    ref_image: str  
    top_image_list: List[str]
    bottom_image_list: List[str]
    shoes_image_list: List[str]
    outer_image_list: List[str] = []
    accessory_image_list: List[str] = []
    bag_image_list: List[str] = []


# 이미지 다운로드 (S3 -> FastApi)
async def download_image(client: httpx.AsyncClient, url: str) -> Optional[Image.Image]:
    try:
        if not url:
            return None
            
        response = await client.get(url)
        response.raise_for_status()
        
        return Image.open(io.BytesIO(response.content))
    
    except Exception as e:
        return None 


# ai 추천 코디 생성
@router.post("/recommend-fit")
async def recommend_fit_service(request: AiRecommendationRequest):
    async with httpx.AsyncClient() as client:
        
        ref_task = download_image(client, request.ref_image)
        top_tasks = [download_image(client, url) for url in request.top_image_list]
        bottom_tasks = [download_image(client, url) for url in request.bottom_image_list]
        shoes_tasks = [download_image(client, url) for url in request.shoes_image_list]
        outer_tasks = [download_image(client, url) for url in request.outer_image_list]
        acc_tasks = [download_image(client, url) for url in request.accessory_image_list]
        bag_tasks = [download_image(client, url) for url in request.bag_image_list]

        pil_ref = await ref_task
        pil_top_list = await asyncio.gather(*top_tasks)
        pil_bottom_list = await asyncio.gather(*bottom_tasks)
        pil_shoes_list = await asyncio.gather(*shoes_tasks)
        pil_outer_list = await asyncio.gather(*outer_tasks)
        pil_accessory_list = await asyncio.gather(*acc_tasks)
        pil_bag_list = await asyncio.gather(*bag_tasks)

    if pil_ref is None:
        raise HTTPException(status_code=400, detail="참고 이미지를 다운로드할 수 없습니다.")

    result = recommender.recommend_outfit(
        pil_ref,
        pil_top_list,
        pil_bottom_list,
        pil_shoes_list,
        pil_outer_list,
        pil_accessory_list,
        pil_bag_list
    )

    return {
        "reason": result["reason"],
        "selections": result["selections"]
    }


# 가상 피팅
@router.post("/virtual-fitting")
async def virtual_fitting_service(
    model_image: UploadFile = File(...),
    top_image: UploadFile = File(...),
    bottom_image: UploadFile = File(...),
    shoes_image: UploadFile = File(...),
    outer_image: UploadFile = File(default=None),
    accessory_image: UploadFile = File(default=None),   
    bag_image: UploadFile = File(default=None),
    height: int = Form(...),
    weight: int = Form(...)
):
    try:
        pil_model = Image.open(io.BytesIO(await model_image.read()))
        pil_top = Image.open(io.BytesIO(await top_image.read()))
        pil_bottom = Image.open(io.BytesIO(await bottom_image.read()))
        pil_shoes = Image.open(io.BytesIO(await shoes_image.read()))
        pil_outer = Image.open(io.BytesIO(await outer_image.read())) if outer_image else None
        pil_accessory = Image.open(io.BytesIO(await accessory_image.read())) if accessory_image else None
        pil_bag = Image.open(io.BytesIO(await bag_image.read())) if bag_image else None

        png_bytes = virtual_fitting.generate_virtual_fitting_image(
            pil_model, pil_top, pil_bottom, pil_shoes, height, weight, pil_outer, pil_accessory, pil_bag
        )
        
        return Response(content=png_bytes, media_type="image/png")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# 사진 누끼 따기
@router.post("/masking")
async def masking_service(
    image: UploadFile = File(...)
):
    try:
        pil = Image.open(io.BytesIO(await image.read()))

        result = masking.image_masking(pil)
    
        return Response(content=result, media_type="image/png")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 사진 보정
@router.post("/editing")
async def editing_service(
    image: UploadFile = File(...)
):
    try:
        clothes_pil = Image.open(io.BytesIO(await image.read()))
        
        result = editing.image_editing(clothes_pil)
        
        return Response(content=result, media_type="image/png")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
