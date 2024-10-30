import cv2
import numpy as np
import tensorflow as tf
import pytesseract

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

# Threshold Levels
THRESH_LOW = 0
THRESH_HIGH = 100

CARD_MAX_AREA = 2000000
CARD_MIN_AREA = 10000

#Classification Model
model = tf.keras.models.load_model('card_classifier_model_ver2.h5')


"""Processes the input image and returns a dictionary with a list of all cards in frame, 
card info is provided as a dictionary within each card"""
def process_image(image):
    height, width = image.shape[:2]
    cards = []

    # Define the new dimensions
    new_width = 600
    new_height = int(new_width * height / width)

    # Resize the image
    image = cv2.resize(image, (new_width, new_height), interpolation=cv2.INTER_AREA)

    thresh = preprocess_image(image)
    ccs, isCard = find_cards(thresh)

    if len(ccs) == 0: return None

    for i in range(len(ccs)):
        if isCard[i]:
            card = preprocess_card(ccs[i], image)
            if card is not None: cards.append(card)

    frameCards = dict()
    frameCards["cards"] = cards
    return frameCards
                
def predict_card(model, img):
    predictions = model.predict(img)
    confidence = np.max(predictions)
    category = np.argmax(predictions)

    if confidence < 0.4:
        return "other", confidence
    categories = ["yugioh", "mtg", "pokemon"]
    return categories[category], confidence

def preprocess_image(image):
    """Returns a grayed, blurred, and adaptively thresholded camera image."""

    gray = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray,(13,13),0)

    threshold1 = THRESH_LOW
    threshold2 = THRESH_HIGH
    imgCanny = cv2.Canny(blur, threshold1, threshold2)
    cv2.imshow("Canny Img", imgCanny)

    kernel = np.ones((5, 5), np.uint8)
    imgDil = cv2.dilate(imgCanny, kernel, iterations=1)
    
    return imgDil

def find_cards(thresh_image):
    """Finds all card-sized contours in a thresholded camera image.
    Returns the number of cards, and a list of card contours sorted
    from largest to smallest."""

    # Find contours and sort their indices by contour size
    cnts,hier = cv2.findContours(thresh_image,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
    index_sort = sorted(range(len(cnts)), key=lambda i : cv2.contourArea(cnts[i]),reverse=True)

    # If there are no contours, do nothing
    if len(cnts) == 0:
        return [], []
    
    # Otherwise, initialize empty sorted contour and hierarchy lists
    cnts_sort = []
    hier_sort = []
    cnt_is_card = np.zeros(len(cnts),dtype=int)

    # Fill empty lists with sorted contour and sorted hierarchy. Now,
    # the indices of the contour list still correspond with those of
    # the hierarchy list. The hierarchy array can be used to check if
    # the contours have parents or not.
    for i in index_sort:
        cnts_sort.append(cnts[i])
        hier_sort.append(hier[0][i])

    # Determine which of the contours are cards by applying the
    # following criteria: 1) Smaller area than the maximum card size,
    # 2), bigger area than the minimum card size, 3) have no parents,
    # and 4) have four corners

    for i in range(len(cnts_sort)):
        size = cv2.contourArea(cnts_sort[i])
        peri = cv2.arcLength(cnts_sort[i],True)
        approx = cv2.approxPolyDP(cnts_sort[i],0.01*peri,True)

        print(size)
        print(hier_sort[i][3])
        print(len(approx)) 

        if ((size < CARD_MAX_AREA) and (size > CARD_MIN_AREA)
            and (hier_sort[i][3] == -1) and (len(approx) == 4)):
                cnt_is_card[i] = 1


    return cnts_sort, cnt_is_card

def preprocess_card(contour, frame):
    qCard = dict()
    qCard["contour"] = contour

    peri = cv2.arcLength(contour,True)
    approx = cv2.approxPolyDP(contour,0.01*peri,True)
    pts = np.float32(approx)
    qCard["corner_points"] = pts

    # Find width and height of card's bounding rectangle
    x,y,w,h = cv2.boundingRect(contour)
    qCard["width"] = w, qCard["height"] = w, h

    # Find center point of card by taking x and y average of the four corners.
    average = np.sum(pts, axis=0)/len(pts)
    cent_x = int(average[0][0])
    cent_y = int(average[0][1])
    qCard["centerpoint"] = [cent_x, cent_y]

    # Warp card into 400x600 flattened image using perspective transform
    (warp_rgb, warp_bgr) = flattener(frame, pts, w, h)
    qCard["warp_rgb"] = warp_rgb
    qCard["warp_bgr"] = warp_bgr

    classifyImage = cv2.resize(warp_bgr, (128, 128), interpolation=cv2.INTER_AREA)
    img_array = tf.expand_dims(classifyImage, 0) # Create a batch of size 1
    cardType, confidence = predict_card(model, img_array)
    qCard["game"] = cardType

    if cardType == 'yugioh': (name, id, setCode) = get_yugioh_card_details(qCard["warp_rgb"])
    elif cardType == 'mtg': (name, id, setCode) = get_mtg_card_details(qCard["warp_rgb"])
    elif cardType == 'other': return None
    qCard["name"] = name
    qCard["cardid"] = id
    qCard["setcode"] = setCode

    return qCard

def draw_on_card(qCard, frame):
    """Draw the card name, center point, and contour on the camera image."""

    x = qCard["centerpoint"][0]
    y = qCard["centerpoint"][1]
    cv2.circle(frame,(x,y),5,(255,0,0),-1)

    card_info = qCard["name"] + ", " + qCard["cardid"] + ", " + qCard["setcode"]
    print(card_info)

    "cv2.putText(frame, card_info, (x,y), font, 1, (255, 0, 0), 2, cv2.LINE_AA)"

    return frame

def get_yugioh_card_details(image):
    """Use Pytesseract to find the name of card, id of card, and set code of card, by cropping the original image
       See https://pypi.org/project/pytesseract/"""
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Extract card name
    nameImg = image[12:60, 17:330]
    nameImgCubic = cv2.resize(nameImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    nameImg = cv2.resize(nameImg, None, fx=3, fy=3, interpolation=cv2.INTER_LINEAR)
    cv2.imshow("NameImg", nameImg)
    cv2.imshow("NameImgCubic", nameImgCubic)
    cardName = pytesseract.image_to_string(nameImg, config='-c preserve_interword_spaces=1 -c tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ- ')
    
    # Extract card ID
    cardIDImg = image[585:598, 5:70]
    cardIDImg = cv2.resize(cardIDImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cv2.imshow("cardIDImg", cardIDImg)
    cardID = pytesseract.image_to_string(cardIDImg, config='--psm 13 -c tessedit_char_whitelist=0123456789')
    
    # Extract card set code
    cardSetCodeImg = image[435:450, 285:380]
    cardSetCodeImg = cv2.resize(cardSetCodeImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cv2.imshow("setCodeImg", cardSetCodeImg)
    cardSetCode = pytesseract.image_to_string(cardSetCodeImg, config='--psm 13 -c tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-')

    return (cardName, cardID, cardSetCode)

def get_mtg_card_details(image):
    """Use Pytesseract to find the name of card, id of card, and set code of card, by cropping the original image
       See https://pypi.org/project/pytesseract/"""
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Extract card name
    nameImg = image[20:60, 30:330]
    nameImg = cv2.resize(nameImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cv2.imshow("NameImg", nameImg)
    cardName = pytesseract.image_to_string(nameImg, config='-c preserve_interword_spaces=1 tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ- ')
    
    cardSetCodeImg = image[572:590, 0:70]
    cardSetCodeImg = cv2.resize(cardSetCodeImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cv2.imshow("cardSetCodeImg", cardSetCodeImg)
    cardSetCode = pytesseract.image_to_string(cardSetCodeImg, config='--psm 13 -c tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-/')

    # Extract card ID
    cardIDImg = image[560:572, 0:70]
    cardIDImg = cv2.resize(cardIDImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cv2.imshow("cardIDImg", cardIDImg)
    cardID = pytesseract.image_to_string(cardIDImg, config='--psm 13 -c tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-/')

    return (cardName, cardID, cardSetCode)


def flattener(image, pts, w, h):
    """Flattens an image of a card into a top-down 200x300 perspective.
    Returns the flattened, re-sized, grayed image.
    See www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/"""
    temp_rect = np.zeros((4,2), dtype = "float32")
    
    s = np.sum(pts, axis = 2)

    tl = pts[np.argmin(s)]
    br = pts[np.argmax(s)]

    diff = np.diff(pts, axis = -1)
    tr = pts[np.argmin(diff)]
    bl = pts[np.argmax(diff)]

    # Need to create an array listing points in order of
    # [top left, top right, bottom right, bottom left]
    # before doing the perspective transform

    if w <= 0.8*h: # If card is vertically oriented
        temp_rect[0] = tl
        temp_rect[1] = tr
        temp_rect[2] = br
        temp_rect[3] = bl

    if w >= 1.2*h: # If card is horizontally oriented
        temp_rect[0] = bl
        temp_rect[1] = tl
        temp_rect[2] = tr
        temp_rect[3] = br
    
    if w > 0.8*h and w < 1.2*h: #If card is diamond oriented
        # card is tilted to the left.
        if pts[1][0][1] <= pts[3][0][1]:
            temp_rect[0] = pts[1][0] # Top left
            temp_rect[1] = pts[0][0] # Top right
            temp_rect[2] = pts[3][0] # Bottom right
            temp_rect[3] = pts[2][0] # Bottom left

        # card is tilted to the right
        if pts[1][0][1] > pts[3][0][1]:
            temp_rect[0] = pts[0][0] # Top left
            temp_rect[1] = pts[3][0] # Top right
            temp_rect[2] = pts[2][0] # Bottom right
            temp_rect[3] = pts[1][0] # Bottom left
            
        
    maxWidth = 400
    maxHeight = 600

    # Create destination array, calculate perspective transform matrix,
    # and warp card image
    dst = np.array([[0,0],[maxWidth-1,0],[maxWidth-1,maxHeight-1],[0, maxHeight-1]], np.float32)
    M = cv2.getPerspectiveTransform(temp_rect,dst)
    warp = cv2.warpPerspective(image, M, (maxWidth, maxHeight))
    warp_rgb = cv2.cvtColor(warp,cv2.COLOR_BGR2RGB)

    return (warp_rgb, warp)