import cv2
import numpy as np
import tensorflow as tf
import pytesseract
import os
import imagehash
import difflib
from PIL import Image
import json
import imutils
import easyocr
from fuzzywuzzy import fuzz

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

# Threshold Levels
THRESH_LOW = 0
THRESH_HIGH = 120

CARD_MAX_AREA = 2000000
CARD_MIN_AREA = 10000

# Hashing Variables
hash_size = 16 #Bytes
min_similarity = 117
check_flipped = True

pokemon_hash_path = os.path.join(os.path.dirname(__file__), 'cardHashes/pokemon_dphash_16byte.json')
yugioh_hash_path = os.path.join(os.path.dirname(__file__), 'cardHashes/yugioh_dphash_16byte.json')
mtg_hash_path = os.path.join(os.path.dirname(__file__), 'cardHashes/mtg_dphash_16byte.json')

# Card Hash Dictionaries
with open(pokemon_hash_path, 'r', encoding='utf-8') as json_file:
    pokemon_hash_dict = json.load(json_file)

with open(yugioh_hash_path, 'r', encoding='utf-8') as json_file:
    yugioh_hash_dict = json.load(json_file)

with open(mtg_hash_path, 'r', encoding='utf-8') as json_file:
    mtg_hash_dict = json.load(json_file)

#Classification Model
model_path = os.path.join(os.path.dirname(__file__), 'card_classifier_model_ver2.h5')
model = tf.keras.models.load_model(model_path)


"""Processes the input image and returns a dictionary with a list of all cards in frame, 
card info is provided as a dictionary within each card"""
def process_image(image, thresh_low=THRESH_LOW, thresh_high=THRESH_HIGH):
    height, width = image.shape[:2]
    cards = []

    # Define the new dimensions
    #new_width = 600
    #new_height = int(new_width * height / width)

    # Resize the image
    #image = cv2.resize(image, (new_width, new_height), interpolation=cv2.INTER_AREA)

    #thresh = preprocess_image(image, thresh_low, thresh_high)
    #ccs, isCard = find_cards(thresh)

    #if len(ccs) == 0: return None

    #for i in range(len(ccs)):
    #    if isCard[i]:
    #        card = preprocess_card(ccs[i], image)
    #        if card is not None: cards.append(card)

    contour, w, h = get_one_card_contour(image)
    cards.append(new_process_card(image, contour, w, h))

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

def preprocess_image(image, thresh_low, thresh_high):
    """Returns a grayed, blurred, and adaptively thresholded camera image."""

    gray = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray,(13, 13),0)

    """cv2.imshow("Blur Img", blur)"""

    threshold1 = thresh_low
    threshold2 = thresh_high
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

    """cv2.drawContours(thresh_image, cnts, -1, (0,255,0), 3)
    cv2.imshow("title", thresh_image)"""

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

        """print(size)
        print(hier_sort[i][3])
        print(len(approx))"""

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

    """cv2.imshow("Warp", warp_bgr)"""

    classifyImage = cv2.resize(warp_bgr, (128, 128), interpolation=cv2.INTER_AREA)
    img_array = tf.expand_dims(classifyImage, 0) # Create a batch of size 1
    cardType, confidence = predict_card(model, img_array)
    qCard["game"] = cardType

    print(cardType)

    if cardType == 'yugioh': match = get_yugioh_card_details(qCard["warp_bgr"])
    elif cardType == 'mtg': match = get_mtg_card_details(qCard["warp_rgb"])
    elif cardType == 'pokemon': match = get_pokemon_card_details(qCard["warp_rgb"])
    else: return None

    if match is None: return None
    qCard["name"] = match["name"]
    qCard["cardid"] = match["id"]
    if "setcode" in match: qCard["setcode"] = match["setcode"]
    else: qCard["setcode"] = None
    if "scryfallid" in match: qCard["scryfallid"] = match["scryfallid"]

    return qCard

def draw_on_card(qCard, frame):
    """Draw the card name, center point, and contour on the camera image."""

    x = qCard["centerpoint"][0]
    y = qCard["centerpoint"][1]
    cv2.circle(frame,(x,y),5,(255,0,0),-1)

    """card_info = qCard["name"] + ", " + qCard["cardid"] + ", " + qCard["setcode"]
    print(card_info)"""

    "cv2.putText(frame, card_info, (x,y), font, 1, (255, 0, 0), 2, cv2.LINE_AA)"

    return frame

def get_yugioh_card_details(image):
    """Use Pytesseract/Imagehash to find the name, id, and setcode of a yugioh card through matching hashes"""
    leftTopEdge = image[100:200, 0:7]
    rightTopEdge = image[100:200, 393:400]
    leftBottomEdge = image[400:500, 0:7]
    rightBottomEdge = image[400:500, 393:400]
    
    topLeft = cv2.cvtColor(leftTopEdge, cv2.COLOR_BGR2HSV)
    topRight = cv2.cvtColor(rightTopEdge, cv2.COLOR_BGR2HSV)
    bottomLeft = cv2.cvtColor(leftBottomEdge, cv2.COLOR_BGR2HSV)
    bottomRight = cv2.cvtColor(rightBottomEdge, cv2.COLOR_BGR2HSV)

    avgColorTopLeft = np.mean(topLeft, axis=(0, 1))
    avgColorTopRight = np.mean(topRight, axis=(0, 1))
    avgColorBottomLeft = np.mean(bottomLeft, axis=(0, 1))
    avgColorBottomRight = np.mean(bottomRight, axis=(0, 1))

    green_hsv_range = ((60, 0, 0), (90, 255, 255))
    isGreenBottom = np.all(avgColorBottomLeft >= green_hsv_range[0]) and np.all(avgColorBottomLeft <= green_hsv_range[1]) and np.all(avgColorBottomRight >= green_hsv_range[0]) and np.all(avgColorBottomRight <= green_hsv_range[1])
    isNotGreenTop = np.all(avgColorTopLeft < green_hsv_range[0]) or np.all(avgColorTopLeft > green_hsv_range[1]) or np.all(avgColorTopRight < green_hsv_range[0]) or np.all(avgColorTopRight > green_hsv_range[1])
    print(isGreenBottom)
    print(isNotGreenTop)
    
    # Extract card name
    reader = easyocr.Reader(['en'])
    
    result = reader.readtext(image)
    
    cardName = ""
    
    if  len(result) > 0 and len(result[0]) > 1 and not result[0][1] is None:
        cardName = result[0][1]
    print(cardName)
    
    """
    nameImg = image[0:100, 0:400]
    nameImg = cv2.resize(nameImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cardName = pytesseract.image_to_string(nameImg)
    print(cardName)
    # Extract card ID
    cardIDImg = image[585:598, 5:70]
    cardIDImg = cv2.resize(cardIDImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cardID = pytesseract.image_to_string(cardIDImg, config='--psm 13 -c tessedit_char_whitelist=0123456789')"""
    match = get_match_pool(image, yugioh_hash_dict, cardName)
    if match is None: return None

    # Extract card set code
    cardSetCodeImg = image[435:450, 285:380]
    cardSetCodeImg = cv2.resize(cardSetCodeImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cardSetCodeOCR = pytesseract.image_to_string(cardSetCodeImg, config='--psm 13 -c tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-')
    print(cardSetCodeOCR)
    bestSetcodeMatch = difflib.get_close_matches(cardSetCodeOCR, match["setcodes"], n = 3, cutoff = 0.5)
    if len(bestSetcodeMatch) == 0: 
        print("No matches found for setcode")
        match["setcode"] = None
    else:
        match["setcode"] = bestSetcodeMatch[0]

    return match

def get_mtg_card_details(image):
    """Use Pytesseract/Imagehash to find the name, id, and collector number of a magic the gathering card through matching hashes"""
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Extract card name
    reader = easyocr.Reader(['en'])
    
    result = reader.readtext(image)
    cardName = ""
    
    if  len(result) > 0 and len(result[0]) > 1 and  not result[0][1] is None:
        cardName = result[0][1]
    print(cardName)
    """nameImg = image[20:60, 30:330]
    nameImg = cv2.resize(nameImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cardName = pytesseract.image_to_string(nameImg, config='-c preserve_interword_spaces=1 tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ- ')
    
    cardSetCodeImg = image[572:590, 0:70]
    cardSetCodeImg = cv2.resize(cardSetCodeImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    cardSetCode = pytesseract.image_to_string(cardSetCodeImg, config='--psm 13 -c tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-/')"""
    match = get_match_pool(image, mtg_hash_dict, cardName)
    if match is None: return None

    # Extract card ID
    cardIDImg = image[560:572, 0:70]
    cardIDImg = cv2.resize(cardIDImg, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
    """cv2.imshow("cardIDImg", cardIDImg)"""
    cardID = pytesseract.image_to_string(cardIDImg, config='--psm 13 -c tessedit_char_whitelist=0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-/')
    match["id"] = cardID

    return match

def get_pokemon_card_details(image):
    reader = easyocr.Reader(['en'])
    
    result = reader.readtext(image)
    
    cardName = ""
    
    if  len(result) > 1 and len(result[1]) > 1 and not result[1][1] is None:
        cardName = result[1][1]
    print(cardName)
    return get_match_pool(image, pokemon_hash_dict, cardName)


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

def get_match_pool(card_image, hash_dict, name):
    if card_image is None:
        return None
    image_hash = hash_image(card_image)
    match = find_match(image_hash, hash_dict, name)
    if match is None and check_flipped is True:
        match = find_flipped_match(card_image, hash_dict, name)
    if match is not None:
        return match
    return None

def hash_image(img):
    """Computes the difference and perceptual hash of an image"""
    img = Image.fromarray(img)

    img = img.convert('RGB')

    dhash = imagehash.dhash(img, hash_size)
    phash = imagehash.phash(img, hash_size)

    hash = f'{dhash}{phash}'

    return hash

def hamming_distance(hash1, hash2):
    assert len(hash1) == len(hash2), "Hash lengths are not equal"
    return sum(ch1 != ch2 for ch1, ch2 in zip(hash1, hash2))

def find_match(hash_a, hash_dict, name):
    """Finds the closest match for a card by hamming distance to hashed dictionary"""
    best_match = None
    min_sim = min_similarity
    better_matches = 0

    for card_id, data in hash_dict.items():
        hash_b = data['hash']
        similarity = hamming_distance(hash_a, hash_b)
        if data['name'] == 'The One Ring':
            print("The One Ring simiarity: " + str(similarity))
            print('The One Ring fuzzy: ' + str(fuzz.ratio(name.upper(), data['name'].upper())))
        if similarity < min_sim:
            if fuzz.ratio(name.upper(), data['name'].upper()) > 90 or similarity < 80:
                min_sim = similarity
                best_match = card_id
            
    print("Cards we'd look through: " + str(better_matches))

    if best_match is None:
        return None

    return hash_dict[best_match]

def find_flipped_match(card_image, hash_dict, name):
    """Rotates the card 180 degrees and tries to find closest match"""
    card_image = cv2.rotate(card_image, cv2.ROTATE_180)
    image_hash = hash_image(card_image)
    match = find_match(image_hash, hash_dict, name)
    return match

def ygoprodeck_to_card_data(ygoCard, setcode):
    cardData = {}
    cardData["quantity"] = 1
    cardData["game"] = "yugioh"
    cardData["cardname"] = ygoCard["name"]
    cardData["attribute"] = ygoCard["attribute"]
    cardData["level"] = ygoCard["level"]
    cardData["type"] = ygoCard["type"]
    cardData["description"] = ygoCard["description"]
    cardData["atk"] = ygoCard["atk"]
    cardData["def"] = ygoCard["def"]
    cardData["frameType"] = ygoCard["frameType"]
    setname = ''
    rarities = []
    for set in ygoCard["card_sets"]:
        if setcode == set["set_code"]:
            setname = set["set_name"]
            rarities.append(set["set_rarity"])
    
    if setname == '':
        return None
    
    cardData["setcode"] = setcode
    if (len(rarities) == 1):
        cardData["rarity"] = rarities[0]
    else:
        cardData["possRarities"] = rarities

    return cardData

def scryfall_to_card_data(scryfallCard):
    cardData = {}
    cardData["quantity"] = 1
    cardData["game"] = "mtg"
    cardData["cardname"] = scryfallCard["name"]
    return cardData

def get_one_card_contour(imgIn):
    img = imutils.resize(imgIn, height=640)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    
    # For low contrast (lighter colored) cards
    if needs_clahe(gray):
        clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8, 8))
        gray = clahe.apply(gray)

    # Adaptive Contrast Enhancement for Darker Cards
    mean_intensity = np.mean(gray)
    if mean_intensity < 100:
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        gray = clahe.apply(gray)

    # Pre-Processing: Blur and Morphological Operations
    blurred = cv2.edgePreservingFilter(gray, flags=1, sigma_s=25, sigma_r=0.375)
    
    # Adaptive Canny Edge Detection
    high_thresh, _ = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    low_thresh = max(30, int(high_thresh * 0.5))  # Adjust dynamically

    edges = cv2.Canny(blurred, low_thresh, high_thresh)

    # Morphological Closing to Connect Broken Edges
    kernel = np.ones((25, 25), np.uint8)
    edges = cv2.morphologyEx(edges, cv2.MORPH_CLOSE, kernel)

    # Find Contours
    cnts = cv2.findContours(edges.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)
    cnts = sorted(cnts, key=cv2.contourArea, reverse=True)[:2]  # Get the largest few contours

    screenCnt = None
    card_aspect_ratio = 1.38  # Yu-Gi-Oh! card aspect ratio
    img_display = img.copy()
    best_ratio = 0
    for c in cnts:
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.05 * peri, True)
        x, y, w, h = cv2.boundingRect(approx)
        aspect_ratio = float(h) / float(w)

        if abs(best_ratio - 1.434) > abs(aspect_ratio - 1.434):  # Ensuring it is close to a card shape
            screenCnt = approx
            best_ratio = aspect_ratio

    # Draw a Rectangle Around the Detected Card
    testFlattenImage_BGR = ""
    testFlattenImage_RGB = ""
    if screenCnt is not None:
        x, y, w, h = cv2.boundingRect(screenCnt)
        cv2.rectangle(img_display, (x, y), (x + w, y + h), (0, 255, 0), 3)
        contour = np.array([[x, y], [x + w, y], [x + w, y + h], [x, y + h]])
        contour = contour.reshape((-1, 1, 2))  # Reshape for OpenCV compatibility
        (testFlattenImage_RGB, testFlattenImage_BGR) = flattener(img.copy(), contour, w, h)
        return contour, w, h
    return None
    
def needs_clahe(image):
    # Compute contrast using standard deviation
    contrast = np.std(image)
    return contrast < 30  # Adjust this threshold if needed

def new_process_card(frame, contour, w, h):
    qCard = dict()
    qCard["contour"] = contour
    # Warp card into 400x600 flattened image using perspective transform
    (warp_rgb, warp_bgr) = flattener(frame, contour, w, h)
    qCard["warp_rgb"] = warp_rgb
    qCard["warp_bgr"] = warp_bgr

    """cv2.imshow("Warp", warp_bgr)"""

    classifyImage = cv2.resize(warp_bgr, (128, 128), interpolation=cv2.INTER_AREA)
    img_array = tf.expand_dims(classifyImage, 0) # Create a batch of size 1
    cardType, confidence = predict_card(model, img_array)
    qCard["game"] = cardType

    print(cardType)

    if cardType == 'yugioh': match = get_yugioh_card_details(qCard["warp_rgb"])
    elif cardType == 'mtg': match = get_mtg_card_details(qCard["warp_rgb"])
    elif cardType == 'pokemon': match = get_pokemon_card_details(qCard["warp_rgb"])
    else: return None

    if match is None: return None
    qCard["name"] = match["name"]
    qCard["cardid"] = match["id"]
    if "setcode" in match: qCard["setcode"] = match["setcode"]
    else: qCard["setcode"] = None
    if "scryfallid" in match: qCard["scryfallid"] = match["scryfallid"]
    return qCard