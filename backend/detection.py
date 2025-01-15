import cv2
import numpy as np
import os
import random
from pycocotools import mask as mask_utils
import json

def load_detection_training_images(directories):
    lst = []
    for directory in directories:
        for f in os.listdir(directory):
            if f.endswith(('.png', '.jpg', '.jpeg')): lst.append(os.path.join(directory, f))
    return lst

def random_transform_card(card, max_rotation=30, max_scale=1.5, max_skew=0.2):
    h, w = card.shape[:2]

    # Random rotation
    angle = random.uniform(-max_rotation, max_rotation)
    M_rot = cv2.getRotationMatrix2D((w // 2, h // 2), angle, 1.0)

    # Random scaling
    scale = random.uniform(0.5, max_scale)
    card = cv2.warpAffine(card, M_rot, (w, h), flags=cv2.INTER_LINEAR, borderMode=cv2.BORDER_CONSTANT)

    # Random skew
    pts1 = np.float32([[0, 0], [w, 0], [0, h]])
    skew_x = random.uniform(-max_skew, max_skew) * w
    skew_y = random.uniform(-max_skew, max_skew) * h
    pts2 = np.float32([[skew_x, 0], [w - skew_x, skew_y], [0, h - skew_y]])
    M_skew = cv2.getAffineTransform(pts1, pts2)
    card = cv2.warpAffine(card, M_skew, (w, h), flags=cv2.INTER_LINEAR, borderMode=cv2.BORDER_CONSTANT)

    return card

def generate_segmentation_info(card, transform_matrix, x_offset, y_offset, bg_shape):
    h, w = card.shape[:2]
    
    # Create binary mask from alpha channel (or threshold)
    if card.shape[2] == 4:  # RGBA image
        binary_mask = card[:, :, 3] > 0
    else:  # Threshold grayscale
        gray = cv2.cvtColor(card, cv2.COLOR_BGR2GRAY)
        _, binary_mask = cv2.threshold(gray, 10, 255, cv2.THRESH_BINARY)

    # Apply the transformation to the mask
    transformed_mask = cv2.warpAffine(binary_mask.astype(np.uint8), transform_matrix, (w, h), flags=cv2.INTER_NEAREST)

    # Place the transformed mask on the background
    bg_h, bg_w = bg_shape[:2]
    mask_on_background = np.zeros((bg_h, bg_w), dtype=np.uint8)
    for i in range(transformed_mask.shape[0]):
        for j in range(transformed_mask.shape[1]):
            if transformed_mask[i, j] > 0:
                y = y_offset + i
                x = x_offset + j
                if 0 <= y < bg_h and 0 <= x < bg_w:
                    mask_on_background[y, x] = 1

    # Convert mask to RLE and polygon
    rle = mask_utils.encode(np.asfortranarray(mask_on_background))
    rle['counts'] = rle['counts'].decode('utf-8')  # Ensure counts are string
    contours, _ = cv2.findContours(mask_on_background, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    polygons = [contour.flatten().tolist() for contour in contours if len(contour) >= 6]

    return rle, polygons

def composite_with_segmentation(background, cards, image_id, annotation_id):
    bg_h, bg_w = background.shape[:2]
    annotations = []

    for card_path in cards:
        card = cv2.imread(card_path)
        h, w = card.shape[:2]
        
        # Random skew transformation
        pts1 = np.float32([[0, 0], [w, 0], [0, h]])
        skew_x = random.uniform(-0.2, 0.2) * w
        skew_y = random.uniform(-0.2, 0.2) * h
        pts2 = np.float32([[skew_x, 0], [w - skew_x, skew_y], [0, h - skew_y]])
        transform_matrix = cv2.getAffineTransform(pts1, pts2)

        # Apply transformation
        transformed_card = cv2.warpAffine(card, transform_matrix, (w, h), flags=cv2.INTER_LINEAR, borderMode=cv2.BORDER_CONSTANT)

        # Randomly position the card
        x_offset = random.randint(0, max(0, bg_w - w))
        y_offset = random.randint(0, max(0, bg_h - h))

        # Composite the card onto the background
        for i in range(h):
            for j in range(w):
                if transformed_card[i, j, 3] > 0:  # Check alpha channel
                    y = y_offset + i
                    x = x_offset + j
                    if 0 <= y < bg_h and 0 <= x < bg_w:
                        background[y, x] = transformed_card[i, j, :3]

        # Generate segmentation information
        rle, polygons = generate_segmentation_info(card, transform_matrix, x_offset, y_offset, background.shape)

        # Add annotation
        annotations.append({
            "id": annotation_id,
            "image_id": image_id,
            "category_id": 1,
            "segmentation": polygons,
            "bbox": mask_utils.toBbox(rle).tolist(),
            "area": int(mask_utils.area(rle)),
            "iscrowd": 0
        })
        annotation_id += 1

    return background, annotations, annotation_id

def create_detection_dataset(output_dir, num_images, card_images, background_images):
    annotations = []
    images = []
    annotation_id = 1

    for image_id in range(1, num_images + 1):
        # Randomly pick a background
        background = cv2.imread(random.choice(background_images))

        # Randomly pick 2-5 cards
        num_cards = random.randint(2, 5)
        selected_cards = random.choices(card_images, k=num_cards)

        # Composite cards onto the background
        composited_image, image_annotations, annotation_id = composite_with_segmentation(background, selected_cards, image_id, annotation_id)

        # Save the composited image
        image_filename = f"{image_id:06d}.jpg"
        cv2.imwrite(os.path.join(output_dir, image_filename), composited_image)

        # Add image metadata
        images.append({
            "id": image_id,
            "file_name": image_filename,
            "width": composited_image.shape[1],
            "height": composited_image.shape[0]
        })

        # Add annotations
        annotations.extend(image_annotations)

    # Save annotations to a JSON file
    dataset = {
        "images": images,
        "annotations": annotations,
        "categories": [{"id": 1, "name": "card"}]
    }
    with open(os.path.join(output_dir, "annotations.json"), "w") as f:
        json.dump(dataset, f, indent=4)

card_dirs = ["../sample_images/yugioh", "../sample_images/mtg", "../sample_images/pokemon"]
card_images = load_detection_training_images(card_dirs)
background_imgs = load_detection_training_images(["../sample_images/background"])
create_detection_dataset("../sample_images/composites", 5, card_images, background_imgs)