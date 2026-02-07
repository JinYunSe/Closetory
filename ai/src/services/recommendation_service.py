import json
from datetime import datetime
from google import genai
from google.genai import types
from PIL import Image

class RecommendationService:
    def __init__(self, api_key: str):
        self.client = genai.Client(api_key=api_key)

    def recommend_outfit(
        self, 
        ref_image: Image.Image, 
        top_list: list[Image.Image], 
        bottom_list: list[Image.Image],
        shoes_list: list[Image.Image],
        outer_list: list[Image.Image] = None,
        accessory_list: list[Image.Image] = None,
        bag_list: list[Image.Image] = None
    ) -> dict:
        """
        스타일 참고 사진(ref_image)을 분석하여 가장 잘 어울리는 코디 조합을 추천합니다.
        반환 예시:
        {
            "selections": {
                "Top": 0,
                "Bottom": 2,
                "Shoes": 1,
                "Outer": null,     # 선택 안함
                "Accessory": 3,
                "Bag": null        # 선택 안함
            },
            "reason": "전신 사진을 분석했을 때, ~"
        }
        """

        current_month = datetime.now().month
        if 3 <= current_month <= 5:
            current_season = "Spring (봄)"
        elif 6 <= current_month <= 8:
            current_season = "Summer (여름)"
        elif 9 <= current_month <= 11:
            current_season = "Autumn (가을)"
        else:
            current_season = "Winter (겨울)"

        # 프롬프트 
        prompt = f"""
        You are a high-end **Fashion Coordinator & Visual Stylist** in Korea.
        Your primary capability is **"Vibe Translation"**: analyzing the style/mood of a reference image and adapting it perfectly to the user's wardrobe and the current season.

        [INPUTS]
        - Reference Image: The source for **Style Vibe, Color Mood, and TPO** (Time, Place, Occasion).
        - Current Season: **{current_season}** (CRITICALLY IMPORTANT).
        - Candidate Lists: Top (Total: {len(top_list)}), Bottom ({len(bottom_list)}), Shoes ({len(shoes_list)}), 
        Outer ({len(outer_list) if outer_list else 0}), Accessory ({len(accessory_list) if accessory_list else 0}), Bag ({len(bag_list) if bag_list else 0}).

        [STYLING LOGIC & RULES]
        1. **Step 1: Style Vibe Analysis**: 
        - Analyze the reference image to extract the **Core Style Keywords** (e.g., Minimalist, Street, Preppy, Amekaji) and **Color Palette**.
        - Do NOT analyze the user's physical traits (skin tone, body type). Focus ONLY on the fashion style.

        2. **Step 2: Seasonal Adaptation (THE MOST IMPORTANT)**: 
        - **Absolute Rule**: You MUST select items based on the **{current_season}**.
        - **Cross-Season Logic**: If the reference image shows an outfit for a different season (e.g., Shorts/T-shirt in Summer) but the Current Season is **Winter**:
            - Do NOT copy the item types (Shorts -> Shorts is WRONG).
            - **TRANSLATE the Vibe**: Keep the "Cool/Casual" mood but select "Winter items" (e.g., Padding, Coat, Slacks, Knitwear) from the candidate lists.
            - The final outfit must be wearable physically in the **{current_season}**.

        3. **Step 3: Selection Strategy**:
        - Select items from the Candidate Lists that best bridge the [Reference Vibe] and [Current Season].
        - Prioritize color harmony that matches the reference mood.

        4. **Step 4: Reasoning Guidelines (Strict)**:
        - **DO NOT mention "Reference Image", "Photo", "Picture", or "Source"**.
        - **DO NOT mention physical traits** like skin tone or body shape.
        - Explain why this combination works focusing on **Style Compatibility** and **Seasonal Appropriateness**.
        - *Good Example*: "To capture the casual mood you wanted, I chose a heavy knit and slacks that are perfect for this winter weather."
        - Tone: Professional, trendy, and polite Korean stylist.

        [OUTPUT FORMAT]
        You MUST return the result in the following JSON format:
        {{
            "selections": {{
                "Top": <int index>,
                "Bottom": <int index>,
                "Shoes": <int index>,
                "Outer": <int index or null>,
                "Accessory": <int index or null>,
                "Bag": <int index or null>
            }},
            "reason": "<string in Korean>"
        }}
        """

        contents = [prompt, "Reference Image:", ref_image]
        
        def add_images(label, image_list):
            if image_list:
                for idx, img in enumerate(image_list):
                    contents.append(f"{label} Candidate Index {idx}:")
                    contents.append(img)

        add_images("Top", top_list)
        add_images("Bottom", bottom_list)
        add_images("Shoes", shoes_list)
        add_images("Outer", outer_list)
        add_images("Accessory", accessory_list)
        add_images("Bag", bag_list)

        try:
            # Gemini API 호출 
            response = self.client.models.generate_content(
                model="gemini-3-flash-preview", 
                contents=contents,
                config=types.GenerateContentConfig(
                    response_mime_type="application/json"
                )
            )

            # 결과 파싱 
            result_json = json.loads(response.text)

            return result_json

        except Exception as e:
            return {
                "selections": {
                    "Top": 0, "Bottom": 0, "Shoes": 0,
                    "Outer": None, "Accessory": None, "Bag": None
                },
                "reason": "AI 서비스 연결 중 오류가 발생하여 기본 코디를 제공합니다."
            }