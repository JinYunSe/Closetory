from google import genai
from google.genai import types
from PIL import Image
import io, os

class EditingService:
    def __init__(self, api_key: str):
        self.client = genai.Client(api_key=api_key)

    def _load_reference_image(self, filename: str) -> Image.Image:
        current_dir = os.path.dirname(os.path.abspath(__file__))

        image_path = os.path.join(current_dir, "images", filename)
        
        return Image.open(image_path)

    def image_editing(self, clothes_img: Image.Image):
        try:
            ref_top = self._load_reference_image("top.png")
            ref_bottom = self._load_reference_image("bottom.png")
            ref_outer = self._load_reference_image("outer.png")

            # 프롬프트
            prompt = """
            You are an expert AI Fashion Editor specializing in e-commerce product photography.
            
            [TASK]
            Create a new "Flat Lay" product image of the [Input Clothes Image]. 
            Your goal is to take the **DESIGN & TEXTURE** from the input and apply the **FLAT LAYOUT ARRANGEMENT** suggested by the Reference Images.
            
            [CRITICAL DISTINCTION - READ CAREFULLY]
            * **Input Image Role:** Provides the **ONLY** source for the garment's identity, type, design details (zippers, hoods, pockets, collars), texture, color, and graphics. **Do NOT alter these.**
            * **Reference Image Role:** Provides **ONLY** a guide for how to position the sleeves and body perfectly flat on a surface. **Do NOT copy the reference garment's type or silhouette.** (e.g., If input is a hoodie and reference is a blazer, the output MUST be a hoodie, just laid flat *like* the blazer).
            
            [STEP-BY-STEP INSTRUCTIONS]
            
            **STEP 1: Analyze & Preserve Input Identity**
            - Identify the exact type and features of the [Input Clothes Image] (e.g., Hooded Zip-up Jacket).
            - **CONSTRAINT:** You must strictly preserve all unique design elements of the input: the shape of the hood, the zipper style, pocket placement, etc.
            
            **STEP 2: Apply Reference Layout (Not Shape)**
            - Select the appropriate Reference Image based on category (Top/Bottom/Outer).
            - Observe *only* how the reference garment is arranged flat: Are the sleeves extended straight? Is the body perfectly symmetrical?
            - Apply this *arrangement style* to the Input garment. **Force the Input garment into this flat, symmetrical pose without changing its inherent design.**
            
            **STEP 3: Texture Mapping & Digital Ironing**
            - Apply the texture, color, and details from the input onto this new flat arrangement.
            - **REMOVE** all original wrinkles, folds, and shadows. The final image must look perfectly smooth, pressed, and flat.
            
            **STEP 4: Final Output**
            - Background: Pure White (#FFFFFF).
            - The result is a clean, professional, symmetrical flat-lay shot of the ORIGINAL input garment.
            """

            contents = [
                "Here is the [Input Clothes Image] (Source of Design/Texture):",
                clothes_img,
                
                "Here is [Reference Layout A] for TOPS (Use ONLY for sleeve/body positioning guide):",
                ref_top,
                
                "Here is [Reference Layout B] for BOTTOMS (Use ONLY for leg positioning guide):",
                ref_bottom,
                
                "Here is [Reference Layout C] for OUTERWEAR (Use ONLY for sleeve/body positioning guide):",
                ref_outer,
                
                prompt
            ]
        
            response = self.client.models.generate_content(
                    model="gemini-3-pro-image-preview",
                    contents=contents,
                    config =types.GenerateContentConfig(
                        response_modalities=["IMAGE"],
                        temperature=0.0 
                    )
            )

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