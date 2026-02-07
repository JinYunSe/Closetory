from google import genai
from google.genai import types
from PIL import Image
import io

class VirtualFittingService:
    def __init__(self, api_key: str):
        self.client = genai.Client(api_key=api_key)

    def get_body_description(self,height_cm, weight_kg):
        # 간단한 BMI 계산을 통해 체형 형용사 도출
        height_m = height_cm / 100
        bmi = weight_kg / (height_m ** 2)
        
        body_shape = "average build"
        if bmi < 18.5:
            body_shape = "very slender and thin frame"
        elif 18.5 <= bmi < 23:
            body_shape = "slim and fit physique"
        elif 23 <= bmi < 25:
            body_shape = "average, healthy build"
        elif 25 <= bmi < 30:
            body_shape = "stocky and broad build with some weight"
        else: 
            body_shape = "heavy-set, large, and rounded physique"

        # 키 묘사 추가
        height_desc = "average height"
        if height_cm > 185:
            height_desc = "very tall and imposing"
        elif height_cm < 165:
            height_desc = "short stature"
            
        return f"{height_desc}, {body_shape}"

    def generate_virtual_fitting_image(
        self, 
        model_img: Image.Image, 
        top_img: Image.Image, 
        bottom_img: Image.Image,
        shoes_img: Image.Image,
        height: int,
        weight: int,
        outer_img: Image.Image = None,
        accessory_img: Image.Image = None,
        bag_img: Image.Image = None,
    ):
        try:
            body_visual_prompt = self.get_body_description(height, weight)
            
            prompt = f"""
            [SYSTEM ROLE]
            You are a world-class AI Virtual Try-On Specialist. 
            Your goal is to replace the clothes on the [Target Person] using the provided garment assets while strictly following the body specifications and ensuring a complete redesign of the clothing features.

            [SPECIFIC BODY TARGET]
            - **Physical Guide:** {body_visual_prompt}
            - Use this guide to adjust the drape, tension, and fit of the new clothes naturally over the model's physique.

            [STRICT CONSTRAINTS - DO NOT ALTER]
            1. **Background & Environment:** The background pixels must remain 100% IDENTICAL to the original [Target Person] image. Do not regenerate, blur, or shift the horizon.
            2. **Identity & Face:** The model's face, hair, and skin tone must be preserved exactly. Do not modify facial features.
            3. **Pose Flexibility:** You may slightly adjust the pose (e.g., arm angle, shoulder slope) ONLY if it is necessary to make the clothing fit the {body_visual_prompt} more naturally.

            [CLOTHING SYNTHESIS RULES - CRITICAL]
            - **Complete Replacement & Feature Erasure:** You must **completely eradicate** all design features of the original garments. Even if the new garment's color is similar, **DO NOT inherit any design elements** (e.g., pockets, seams, logos, fit style) from the old clothes.
            - **Bottom Garment Specifics:** The new [Bottom Garment] is a plain black pant **WITHOUT any side cargo pockets**, as shown in its input image. **Crucially, make sure the final image does NOT show the cargo pockets present on the original pants.** The new pants must have a clean, pocket-less side profile.
            - **Outer Garment Logic:** If an [Outer Garment] is provided, it MUST be worn OPEN to show the [Top] underneath.
            - **Accessory & Bag:** Place them naturally according to the model's final pose. If a bag is provided but hidden by the pose, place it on the ground at the bottom-right.

            [EXECUTION]
            - Completely mask out the original clothing area, ensuring no trace of the old design remains.
            - Synthesize the new items onto the body shape defined by {body_visual_prompt}, strictly adhering to the design of the new assets.
            - Ensure seamless blending at the edges (neck, wrists, ankles) with realistic lighting and shadows applied to the new fabric.
            - Output ONLY the final generated image.
            """

            contents = [
                "This is the [Target Person] image:", 
                model_img, 
                
                "This is the [Top Garment] image:", 
                top_img,

                "This is the [Bottom Garment] image:",
                bottom_img,

                "This is the [Shoes] image",
                shoes_img,
            ]

            if outer_img:
                contents.append("This is the [Outer Garment] image:")
                contents.append(outer_img)

            if accessory_img:
                contents.append("This is the [Accessory] image:")
                contents.append(accessory_img)
            
            if bag_img:
                contents.append("This is the [Bag] image:")
                contents.append(bag_img)
            
            contents.append(prompt)

            # Gemini에게 요청 보내기
            response = self.client.models.generate_content(
                model="gemini-3-pro-image-preview",
                contents=contents,
                config =types.GenerateContentConfig(
                    response_modalities=["IMAGE"],
                    temperature=0.0 
                )
            )

            # 4. 결과 처리 
            if response.candidates and response.candidates[0].content.parts:
                for part in response.candidates[0].content.parts:
                    if part.inline_data:
                        
                        # raw data
                        raw_img_data = part.inline_data.data
                        
                        # 포맷 변환
                        image = Image.open(io.BytesIO(raw_img_data))
                        
                        # PNG로 저장하기 위해 바이트 스트림 생성
                        output_stream = io.BytesIO()
                        image.save(output_stream, format="PNG")
                        
                        # 바이트 값만 리턴
                        return output_stream.getvalue()
            
            raise Exception("No image generated from Gemini")

        except Exception as e:
            raise e