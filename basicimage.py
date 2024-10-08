import cv2
import Cards

frame = cv2.imread("./images/stardust_dragon_img.jpg")

frameCards = Cards.process_image(frame)
for card in frameCards["cards"]:
    print("Name: " + card["name"] + "Set_Code: " + card["setcode"] + "ID: " + card["cardid"])


cam = 0
while cam == 0:
    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        cam = 1
        

cv2.destroyAllWindows()
