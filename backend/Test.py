import cv2 as cv
import numpy as np
import imutils

from Cards import flattener

img_list = ['Call_Of_The_Haunted', 'Deskbot_007', 'Dragon_Capture_Jar', 'Fighting_Spirit', 'Gouki_Suprex', 'Gouki_Thunder_Ogre', 'Red_Dragon_Archfiend', 'The_One_Ring', 'Tynamo']
img_index = 0  # Start with the first image

def process_image():
    img = cv.imread(f'./images/{img_list[img_index]}.jpg')
    if img is None:
        print(f"Error: File {img_list[img_index]} not found!")
        return

    img = imutils.resize(img, height=640)
    gray = cv.cvtColor(img, cv.COLOR_BGR2GRAY)
    
    # For low contrast (lighter colored) cards
    if needs_clahe(gray):
        clahe = cv.createCLAHE(clipLimit=3.0, tileGridSize=(8, 8))
        gray = clahe.apply(gray)

    # Adaptive Contrast Enhancement for Darker Cards
    mean_intensity = np.mean(gray)
    if mean_intensity < 100:
        clahe = cv.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        gray = clahe.apply(gray)

    # Pre-Processing: Blur and Morphological Operations
    blurred = cv.edgePreservingFilter(gray, flags=1, sigma_s=25, sigma_r=0.375)
    
    # Adaptive Canny Edge Detection
    high_thresh, _ = cv.threshold(blurred, 0, 255, cv.THRESH_BINARY + cv.THRESH_OTSU)
    low_thresh = max(30, int(high_thresh * 0.5))  # Adjust dynamically

    edges = cv.Canny(blurred, low_thresh, high_thresh)

    # Morphological Closing to Connect Broken Edges
    kernel = np.ones((25, 25), np.uint8)
    edges = cv.morphologyEx(edges, cv.MORPH_CLOSE, kernel)

    # Find Contours
    cnts = cv.findContours(edges.copy(), cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)
    cnts = sorted(cnts, key=cv.contourArea, reverse=True)[:2]  # Get the largest few contours

    screenCnt = None
    card_aspect_ratio = 1.38  # Yu-Gi-Oh! card aspect ratio
    img_display = img.copy()
    best_ratio = 0
    for c in cnts:
        peri = cv.arcLength(c, True)
        approx = cv.approxPolyDP(c, 0.05 * peri, True)
        x, y, w, h = cv.boundingRect(approx)
        aspect_ratio = float(h) / float(w)

        if abs(best_ratio - 1.434) > abs(aspect_ratio - 1.434):  # Ensuring it is close to a card shape
            screenCnt = approx
            best_ratio = aspect_ratio

    # Draw a Rectangle Around the Detected Card
    testFlattenImage_BGR = ""
    testFlattenImage_RGB = ""
    if screenCnt is not None:
        x, y, w, h = cv.boundingRect(screenCnt)
        cv.rectangle(img_display, (x, y), (x + w, y + h), (0, 255, 0), 3)
        contour = np.array([[x, y], [x + w, y], [x + w, y + h], [x, y + h]])
        contour = contour.reshape((-1, 1, 2))  # Reshape for OpenCV compatibility
        (testFlattenImage_RGB, testFlattenImage_BGR) = flattener(img.copy(), contour, w, h)
        

    # Convert Edges to BGR for Display
    edges_colored = cv.cvtColor(edges, cv.COLOR_GRAY2BGR)
    display = np.hstack((img_display, edges_colored))
    
    

    cv.imshow('Image and Edge Detection', testFlattenImage_RGB)

def change_image(val):
    global img_index
    img_index = (img_index + 1) % len(img_list)
    process_image()
    
def needs_clahe(image):
    # Compute contrast using standard deviation
    contrast = np.std(image)
    return contrast < 30  # Adjust this threshold if needed

cv.namedWindow('Image and Edge Detection')
process_image()

while True:
    key = cv.waitKey(1) & 0xFF
    if key == ord('n'):
        change_image(0)
    elif key == 27:  # Press ESC to exit
        break

cv.destroyAllWindows()
