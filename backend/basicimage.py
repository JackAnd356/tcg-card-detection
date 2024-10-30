import cv2
import Cards

frame = cv2.imread("../images/the_one_ring.webp")

frameCards = Cards.process_image(frame)
for card in frameCards["cards"]:
    """cv2.imwrite("../images/stardust_dragon_warp.jpg", card["warp_bgr"])"""
    print("Name: " + card["name"] + "Set_Code: " + card["setcode"] + "ID: " + card["cardid"])


cam = 0
while cam == 0:
    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        cam = 1
        

cv2.destroyAllWindows()
