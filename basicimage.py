import cv2
import Cards

def empty(a):
    pass

cv2.namedWindow("Parameters")
cv2.resizeWindow("Parameters", 640, 180)
cv2.createTrackbar("Threshold1", "Parameters", 0, 255, empty)
cv2.createTrackbar("Threshold2", "Parameters", 140, 255, empty)
cv2.createTrackbar("Area", "Parameters", 0, 100000, empty)

"""Main while loop - frame from webcam is read, blurred and greyed, and all card-shaped contours are processed"""
frame = cv2.imread("./images/stardust_dragon_img.jpg")

height, width = frame.shape[:2]

# Define the new dimensions
new_width = 600
new_height = int(new_width * height / width)

# Resize the image
frame = cv2.resize(frame, (new_width, new_height), interpolation=cv2.INTER_AREA)

thresh = Cards.preprocess_image(frame)
ccs, isCard = Cards.find_cards(thresh)

cards = []
cCount = 0

"""cv2.drawContours(frame,ccs[0], -1, (255,0,0), 10)
cv2.imshow("Contours", thresh)"""

if len(ccs) != 0:
    for i in range(len(ccs)):
        if isCard[i]:
            cards.append(Cards.preprocess_card(ccs[i], frame))

            frame = Cards.draw_on_card(cards[cCount], frame)
            cCount = cCount + 1

    if (len(cards) != 0):
        print("Cards!")
        temp_cnts = []
        for i in range(len(cards)):
            temp_cnts.append(cards[i].contour)
        cv2.imshow("Card Detector", cards[0].warp)


cam = 0
while cam == 0:
    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        cam = 1
        

cv2.destroyAllWindows()
