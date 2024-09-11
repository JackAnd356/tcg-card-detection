import cv2
import Cards

webcam = cv2.VideoCapture(0)
cam = 0

def empty(a):
    pass

cv2.namedWindow("Parameters")
cv2.resizeWindow("Parameters", 640, 180)
cv2.createTrackbar("Threshold1", "Parameters", 100, 255, empty)
cv2.createTrackbar("Threshold2", "Parameters", 140, 255, empty)
cv2.createTrackbar("Area", "Parameters", 35000, 100000, empty)

"""Main while loop - frame from webcam is read, blurred and greyed, and all card-shaped contours are processed"""
while (cam == 0):
    ret, frame = webcam.read()

    thresh = Cards.preprocess_image(frame)
    ccs, isCard = Cards.find_cards(thresh)

    cards = []
    cCount = 0

    if len(ccs) != 0:
        for i in range(len(ccs)):
            if isCard[i]:
                cards.append(Cards.preprocess_card(ccs[i], frame))

                frame = Cards.draw_on_card(cards[cCount], frame)
                cCount = cCount + 1

        if (len(cards) != 0):
                temp_cnts = []
                for i in range(len(cards)):
                    temp_cnts.append(cards[i].contour)
                cv2.drawContours(frame,temp_cnts, -1, (255,0,0), 2)
    
    cv2.imshow("Card Detector", frame)

    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        cam = 1
        

cv2.destroyAllWindows()
