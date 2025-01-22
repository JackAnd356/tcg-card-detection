import cv2
import os
import Cards

filename = "Tynamo.jpg"
filepath = os.path.join("../images", filename)
img = cv2.imread(filepath)
img_cards = Cards.process_image(img)

for card in img_cards["cards"]:
    print(f'Name: {card["name"]} ID: {card["cardid"]} Set Code: {card["setcode"]}')

cam = 0
while cam == 0:
    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        cam = 1
cv2.destroyAllWindows()

"""maxFound = 0
maxThreshLow = -1
maxThreshHigh = -1
for i in range(20):
    for j in range(i+1, 20):
        print(f'i: {i}, j: {j}')
        totalFiles = 0
        cardsFound = 0
        for k, filename in enumerate(os.listdir("../images")):
            filepath = os.path.join("../images", filename)
            img = cv2.imread(filepath)
            
            img_cards = Cards.process_image(img, i*10, j*10)
            totalFiles += 1
            for card in img_cards["cards"]:
                if "name" in card:
                    cardsFound += 1
                    print("Actual: " + filename + ", Detected: " + card["name"])
                else:
                    print("Card Detected but not properly")

            if len(img_cards["cards"]) == 0:
                print(f'No Cards Found: {filename}')
            else:
                cam = 0
                while cam == 0:
                    key = cv2.waitKey(1) & 0xFF
                    if key == ord("q"):
                        cam = 1
                cv2.destroyAllWindows()
        if cardsFound > maxFound:
            maxFound = cardsFound
            maxThreshLow = i
            maxThreshHigh = j
        print(f'Total Files: {totalFiles}, Cards Found: {cardsFound}')
        
print(f'Max Found: {maxFound}, Thresh_Low: {maxThreshLow}, Thresh_High: {maxThreshHigh}')"""

