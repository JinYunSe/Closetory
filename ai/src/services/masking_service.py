from PIL import Image
from rembg import remove, new_session
import io
import numpy as np
import cv2

class MaskingService:
    def __init__(self):
        self.session = new_session("isnet-general-use")
    
    def image_masking(self, img: Image.Image):
        try:
            # 1. 배경 제거 
            output = remove(img, session=self.session, alpha_matting=False)
            
            # 2. OpenCV 포맷으로 변환
            img_np = np.array(output)
            
            # 3. 알파 채널(투명도) 추출
            alpha = img_np[:, :, 3]
            
            contours, _ = cv2.findContours(alpha, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            
            if contours: # 덩어리가 있을 때만 실행
                sorted_contours = sorted(contours, key=cv2.contourArea, reverse=True)
                max_area = cv2.contourArea(sorted_contours[0])

                mask = np.zeros_like(alpha)
                
                # 메인 덩어리(20% 이상)만 살리기
                for cnt in sorted_contours:
                    if cv2.contourArea(cnt) > max_area * 0.2: 
                        cv2.drawContours(mask, [cnt], -1, 255, thickness=cv2.FILLED)

                # 마스크 적용 (잡티 삭제)
                img_np[:, :, 3] = cv2.bitwise_and(alpha, mask)

            # 4. 결과 반환
            result_pil = Image.fromarray(img_np)
            output_stream = io.BytesIO()
            result_pil.save(output_stream, format="PNG")
            
            return output_stream.getvalue()

        except Exception as e:
            raise e