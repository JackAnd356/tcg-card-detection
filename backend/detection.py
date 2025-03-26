import cv2
import numpy as np
import os
import random
import sys
from pycocotools import mask as mask_utils
import json

"""Usage: Arg1 is number of training images to produce, Arg2 is number of test images to produce"""

def load_detection_training_images(directories):
    lst = []
    for directory in directories:
        for f in os.listdir(directory):
            if f.endswith(('.png', '.jpg', '.jpeg')): lst.append(os.path.join(directory, f))
    return lst

def composite_with_segmentation(background, cards, image_id, annotation_id):
    """
    Composites cards onto a background and generates segmentation annotations.

    Args:
        background (np.ndarray): Background image array (H, W, C).
        cards (list of str): List of file paths to card images.
        image_id (int): ID of the composite image for annotations.
        annotation_id (int): Starting ID for annotations.

    Returns:
        np.ndarray: Modified background with cards composited.
        list of dict: COCO-style annotations.
        int: Updated annotation ID.
    """
    bg_h, bg_w = background.shape[:2]
    annotations = []

    occupancy_mask = np.zeros((bg_h, bg_w), dtype=np.uint8)

    for card_path in cards:
        # Read card image
        card = cv2.imread(card_path, cv2.IMREAD_UNCHANGED)
        if card is None:
            print(f"Warning: Failed to load {card_path}. Skipping...")
            continue

        h_orig, w_orig = card.shape[:2]
        ratio = h_orig/w_orig

        card = cv2.resize(card, (int(500/ratio), 500))
        h, w = card.shape[:2]
        #print(f'H:{h}, W:{w}')
        try:
            card = add_alpha_channel(card)
        except:
            continue

        # Random skew transformation
        pts1 = np.float32([[0, 0], [w, 0], [0, h]])
        skew_x = random.uniform(-0.2, 0.2) * w
        skew_y = random.uniform(-0.2, 0.2) * h
        pts2 = np.float32([[skew_x, 0], [w - skew_x, skew_y], [0, h - skew_y]])
        transform_matrix = cv2.getAffineTransform(pts1, pts2)

        # Apply transformation
        transformed_card, x_min, y_min = safe_affine_transform(card, transform_matrix)
        h_trans, w_trans = transformed_card.shape[:2]


        # Try to find a valid position for the card
        max_attempts = 50  # Limit attempts to avoid infinite loops
        for attempt in range(max_attempts):
            x_offset = random.randint(0, max(0, bg_w - w_trans))
            y_offset = random.randint(0, max(0, bg_h - h_trans))

            # Check if the region is unoccupied
            card_region = occupancy_mask[y_offset:y_offset + h_trans, x_offset:x_offset + w_trans]
            overlap = np.sum(card_region)
            # print(overlap)
            if overlap < 10000:  # Somewhat minimal overlap
                break
        else:
            # print(f"Warning: Could not find non-overlapping position for card after {max_attempts} attempts.")
            continue

        # Generate Segmentation mask edges
        edges = generate_card_edges(w, h, transform_matrix, x_offset, y_offset, x_min, y_min)

        # Update the occupancy mask
        cv2.fillConvexPoly(
            occupancy_mask,
            np.int32(edges).reshape((-1, 2)),
            1
        )

        # Blend the card onto the background
        for i in range(h_trans):
            for j in range(w_trans):
                # Check if the transformed pixel has transparency (alpha channel)
                if transformed_card[i, j, 3] > 0:  # Ensure the alpha channel exists and is valid
                    y = y_offset + i
                    x = x_offset + j
                    if 0 <= y < bg_h and 0 <= x < bg_w:
                        background[y, x] = transformed_card[i, j, :3]


        """for i in range(0, len(edges), 2):
            cv2.circle(background, (int(edges[i]), int(edges[i + 1])), 5, (0, 255, 0), -1)
        cv2.imshow('Debug Segmentation Points', background)
        cv2.waitKey(0)"""

        # Add annotation
        annotations.append({
            "id": annotation_id,
            "image_id": image_id,
            "category_id": 1,
            "segmentation": [edges],
            "bbox": [
                min(edges[::2]),  # x_min
                min(edges[1::2]),  # y_min
                max(edges[::2]) - min(edges[::2]),  # width
                max(edges[1::2]) - min(edges[1::2])  # height
            ],
            "area": int((max(edges[::2]) - min(edges[::2])) * (max(edges[1::2]) - min(edges[1::2]))),
            "iscrowd": 0
        })
        annotation_id += 1

    return background, annotations, annotation_id

# Affine Transform without cutting off the corners of the card
def safe_affine_transform(card, transform_matrix):
    h, w = card.shape[:2]

    # Compute the transformed corners
    corners = np.float32([[0, 0], [w, 0], [w, h], [0, h]])
    transformed_corners = cv2.transform(np.array([corners]), transform_matrix)[0]

    # Calculate the bounding box of the transformed corners
    x_min = int(min(transformed_corners[:, 0]))
    y_min = int(min(transformed_corners[:, 1]))
    x_max = int(max(transformed_corners[:, 0]))
    y_max = int(max(transformed_corners[:, 1]))

    # Calculate the size of the expanded canvas
    expanded_w = x_max - x_min
    expanded_h = y_max - y_min

    # Adjust the transformation matrix to account for the shift
    adjusted_matrix = transform_matrix.copy()
    adjusted_matrix[0, 2] -= x_min
    adjusted_matrix[1, 2] -= y_min

    # Apply the transformation to the expanded canvas
    transformed_card = cv2.warpAffine(
        card,
        adjusted_matrix,
        (expanded_w, expanded_h),
        flags=cv2.INTER_LINEAR,
        borderMode=cv2.BORDER_CONSTANT,
        borderValue=(0, 0, 0)  # Black background for missing areas
    )

    return transformed_card, x_min, y_min

def add_alpha_channel(image):
    if image.shape[-1] != 3:
        raise ValueError("Input image must have 3 channels (RGB).")

    # Create an alpha channel filled with maximum opacity (255)
    alpha_channel = np.ones((image.shape[0], image.shape[1]), dtype=np.uint8) * 255

    # Stack the RGB channels with the alpha channel
    return np.dstack((image, alpha_channel))


def generate_card_edges(w, h, transform_matrix, x_offset, y_offset, x_min, y_min):
    """
    Generates the edges of the card after applying transformations and positioning.
    
    Args:
        w (int): Width of the card.
        h (int): Height of the card.
        transform_matrix (np.ndarray): Affine transformation matrix.
        x_offset (int): X offset of the card on the background.
        y_offset (int): Y offset of the card on the background.
        x_min (int): X shift due to canvas expansion.
        y_min (int): Y shift due to canvas expansion.

    Returns:
        list: Flattened list of edge coordinates [x1, y1, x2, y2, ..., x4, y4].
    """
    # Original corner points of the card
    corners = np.float32([
        [0, 0],        # Top-left
        [w, 0],        # Top-right
        [w, h],        # Bottom-right
        [0, h]         # Bottom-left
    ])

    # Transform the corners
    transformed_corners = cv2.transform(np.array([corners]), transform_matrix)[0]

    # Adjust for canvas expansion and offsets
    transformed_corners[:, 0] += (x_offset - x_min)
    transformed_corners[:, 1] += (y_offset - y_min)

    # Flatten into a list
    return transformed_corners.flatten().tolist()

def create_detection_dataset(output_image_dir, output_annotations_dir, num_images, card_images, background_images, output_filename):
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
        cv2.imwrite(os.path.join(output_image_dir, image_filename), composited_image)

        # Add image metadata
        images.append({
            "id": image_id,
            "file_name": image_filename,
            "width": composited_image.shape[1],
            "height": composited_image.shape[0]
        })

    # Save annotations to a JSON file
    dataset = {
        "images": images,
        "annotations": image_annotations,
        "categories": [{"id": 1, "name": "card"}]
    }
    with open(os.path.join(output_annotations_dir, f'instances_{output_filename}.json'), "w") as f:
        json.dump(dataset, f, indent=4)

card_dirs = ["../sample_images/yugioh", "../sample_images/mtg", "../sample_images/pokemon"]
card_images = load_detection_training_images(card_dirs)
background_imgs = load_detection_training_images(["../sample_images/background"])
create_detection_dataset("../data/cards/train", "../data/cards/annotations", int(sys.argv[1]), card_images, background_imgs, "train")
create_detection_dataset("../data/cards/val", "../data/cards/annotations", int(sys.argv[2]), card_images, background_imgs, "val")