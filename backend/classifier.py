import requests
import random
import os
import time
import re
import numpy as np
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D, MaxPooling2D, Flatten, Dense, Dropout, Input
import cv2
from dotenv import load_dotenv, find_dotenv
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
import json
from PIL import Image
import imagehash
from sklearn.utils import shuffle
from tensorflow.keras.preprocessing.image import ImageDataGenerator

load_dotenv(find_dotenv())

hash_size = 16

def getYugiohSample(numCards):
    url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
    resp = requests.get(url)
    if resp.status_code == 200:
        cardData = resp.json()
        cards = cardData["data"]

        counter = 0

        random.shuffle(cards)

        for i,card in enumerate(cards):
            img_url = card["card_images"][0]["image_url"]
            card_name = card["name"].replace(' ', '_').replace('/', '_')
            card_name = sanitize_filename(card_name)
            filename = f'../sample_images/yugioh/{card_name}.jpg'

            img = requests.get(img_url)
            if img.status_code == 200:
                counter += 1
                with open(filename, "wb") as img_file:
                    img_file.write(img.content)
                print(f'Downloaded {card_name} Num: {i}/{numCards} Total Downloaded: {counter}')
            else:
                print(f'Failed to get Image of: {card_name}')
            time.sleep(0.1)
            if counter >= numCards: return
    else:
        print(f'API Gave Error Status Code: {resp.status_code}')


def getMTGSample(numCards):
    url = 'https://api.scryfall.com/cards/random'
    headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
    

    counter = 0

    while counter < numCards:
        resp = requests.get(url, headers=headers)

        if resp.status_code == 200:
            cardData = resp.json()
            if not "image_uris" in cardData: continue
            img_url = cardData["image_uris"]["large"]
            card_name = cardData["name"].replace(' ', '_').replace('/', '_')
            card_name = sanitize_filename(card_name)
            filename = f'../sample_images/mtg/{card_name}.jpg'

            img = requests.get(img_url)
            if img.status_code == 200:
                counter += 1
                with open(filename, "wb") as img_file:
                    img_file.write(img.content)
                print(f'Downloaded {card_name} Total Downloaded: {counter}')
            else:
                print(f'Failed to get Image of: {card_name}')
        else:
            print(f'API Gave Error Status Code: {resp.status_code}')
        
        time.sleep(0.1)


def getMTGCard(scryfallId, filename):
    url = f'https://api.scryfall.com/cards/{scryfallId}'
    headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
    resp = requests.get(url, headers=headers)

    if resp.status_code == 200:
        cardData = resp.json()
        if not "image_uris" in cardData: return
        img_url = cardData["image_uris"]["normal"]
        card_name = cardData["name"]

        img = requests.get(img_url)
        if img.status_code == 200:
            with open(filename, "wb") as img_file:
                img_file.write(img.content)
            #print(f'Downloaded {card_name}')
            return cardData
        else:
            print(f'Failed to get Image of: {card_name}')
    else:
        print(f'API Gave Error Status Code: {resp.status_code}')


def getPokemonSample(numCards):
    apikey = os.getenv('POKEMON_API_KEY')
    url = 'https://api.pokemontcg.io/v2/cards'
    headers = {'X-Api-Key' : apikey}
    params = {'pageSize' : 250}

    counter = 0
    for i, filename in enumerate(os.listdir("../sample_images/pokemon_jsons")):
        with open(f'../sample_images/pokemon_jsons/{filename}', 'r', encoding='utf-8') as completed_json_file:
            pokemon_json = json.load(completed_json_file)
            for card in pokemon_json:
                image_url = card['images']['large']
                card_name = card['name'].replace(' ', '_').replace('/', '_').replace('δ', 'delta')
                card_name = sanitize_filename(card_name)
                filename = f"../sample_images/pokemon/{card_name}_{card['id']}.jpg"

                img_response = requests.get(image_url)

                if img_response.status_code == 200:
                    counter += 1
                    with open(filename, "wb") as img_file:
                        img_file.write(img_response.content)
                    print(f"Downloaded: {card_name} Total Downloaded: {counter}")
                else:
                    print(f"Failed to download image for card: {card_name}")
                
                if counter >= numCards: return

    """while (numCards > 0):
        resp = requests.get(url, headers=headers, params=params)

        if resp.status_code == 200:
            card_data = resp.json()
            cards = card_data["data"]

            random.shuffle(cards)
            counter = 0

            for i, card in enumerate(cards):
                image_url = card['images']['large']
                card_name = card['name'].replace(' ', '_').replace('/', '_').replace('δ', 'delta')
                card_name = sanitize_filename(card_name)
                filename = f"../sample_images/pokemon/{card_name}.jpg"

                img_response = requests.get(image_url)

                if img_response.status_code == 200:
                    counter += 1
                    with open(filename, "wb") as img_file:
                        img_file.write(img_response.content)
                    print(f"Downloaded: {card_name} Total Downloaded: {counter}")
                else:
                    print(f"Failed to download image for card: {card_name}")
                if counter >= numCards: break
            numCards -= counter
        else:
            print(f'API Gave Error Status Code: {resp.status_code}')"""

def getTrainingData():
    training_images = []
    training_labels = []
    test_images = []
    test_labels = []

    for i, filename in enumerate(os.listdir("../sample_images/pokemon")):
        filepath = os.path.join("../sample_images/pokemon", filename)
        print(filename)
        img = cv2.imread(filepath)
        imgSmall = cv2.resize(img, (128, 128), cv2.INTER_AREA)
        if i < 900: 
            training_images.append(imgSmall)
            training_labels.append(2)
        else: 
            test_images.append(imgSmall)
            test_labels.append(2)

    for i, filename in enumerate(os.listdir("../sample_images/mtg")):
        filepath = os.path.join("../sample_images/mtg", filename)
        print(filename)
        img = cv2.imread(filepath)
        imgSmall = cv2.resize(img, (128, 128), cv2.INTER_AREA)
        if i < 900: 
            training_images.append(imgSmall)
            training_labels.append(1)
        else: 
            test_images.append(imgSmall)
            test_labels.append(1)

    for i, filename in enumerate(os.listdir("../sample_images/yugioh")):
        filepath = os.path.join("../sample_images/yugioh", filename)
        print(filename)
        img = cv2.imread(filepath)
        imgSmall = cv2.resize(img, (128, 128), cv2.INTER_AREA)
        if i < 900: 
            training_images.append(imgSmall)
            training_labels.append(0)
        else: 
            test_images.append(imgSmall)
            test_labels.append(0)

    training_images = np.array(training_images)
    training_labels = np.array(training_labels)
    test_images = np.array(test_images)
    test_labels = np.array(test_labels)

    return (training_images, training_labels), (test_images, test_labels)

def sanitize_filename(filename):
    # Remove invalid characters for Windows file names
    return re.sub(r'[<>:"/\\|?*]', '', filename)

def trainModel(training_data, test_data):
    model = Sequential([
        Input(shape=(128, 128, 3)),
        Conv2D(32, (3, 3), activation='relu'),
        MaxPooling2D(pool_size=(2, 2)),
        
        Conv2D(64, (3, 3), activation='relu'),
        MaxPooling2D(pool_size=(2, 2)),

        Conv2D(128, (3, 3), activation='relu'),
        MaxPooling2D(pool_size=(2, 2)),

        Flatten(),

        Dense(128, activation='relu'),
        Dropout(0.5),

        Dense(3, activation='softmax')  # Output layer
    ])

    model.compile(optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy'])
    
    datagen = ImageDataGenerator(
        rotation_range=10,        # Random rotation up to 10 degrees
        width_shift_range=0.1,    # Shift width up to 10% of the image width
        height_shift_range=0.1,   # Shift height up to 10% of the image height
        zoom_range=0.1,           # Random zoom
        horizontal_flip=True,     # Random horizontal flip
        fill_mode='nearest'       # Fill strategy for missing pixels after augmentation
    )

    datagen.fit(training_data[0])
        
    epochs = 10
    history = model.fit(
        datagen.flow(training_data[0], training_data[1], batch_size=32),
        validation_data=(test_data[0], test_data[1]),
        epochs=10
    )

    val_loss, val_acc = model.evaluate(test_data[0], test_data[1])
    print(f"Validation Accuracy: {val_acc * 100:.2f}%")
    return model, history

def plot_training_history(history):
    acc = history.history['accuracy']
    val_acc = history.history['val_accuracy']
    loss = history.history['loss']
    val_loss = history.history['val_loss']

    epochs = 10
    epochs_range = range(epochs)

    plt.figure(figsize=(8, 8))
    plt.subplot(1, 2, 1)
    plt.plot(epochs_range, acc, label='Training Accuracy')
    plt.plot(epochs_range, val_acc, label='Validation Accuracy')
    plt.legend(loc='lower right')
    plt.title('Training and Validation Accuracy')

    plt.subplot(1, 2, 2)
    plt.plot(epochs_range, loss, label='Training Loss')
    plt.plot(epochs_range, val_loss, label='Validation Loss')
    plt.legend(loc='upper right')
    plt.title('Training and Validation Loss')
    plt.show()

def predict_card(model, img):
    predictions = model.predict(img)
    confidence = np.max(predictions)
    category = np.argmax(predictions)

    if confidence < 0.4:
        return "Other", confidence
    categories = ["Yugioh", "MagicTheGathering", "Pokemon"]
    return categories[category], confidence

def hash_image(img):
    # Convert the NumPy array to a PIL image
    img = Image.fromarray(img)

    img = img.convert('RGB')

    # Resize the image to the desired size
    #img = img.resize((image_size, image_size))

    # Compute the hash
    # ahash = str(imagehash.average_hash(img, hash_size))
    dhash = imagehash.dhash(img, hash_size)
    phash = imagehash.phash(img, hash_size)

    hash = f'{dhash}{phash}'

    return hash


"""getYugiohSample(2000)
getMTGSample(2000)
getPokemonSample(2000)"""

"""training_data, test_data = getTrainingData()
print(f"Training data: {training_data[0].shape}, Training labels: {training_data[1].shape}")
print(f"Test data: {test_data[0].shape}, Test labels: {test_data[1].shape}")
print(test_data[1])
model, history = trainModel(training_data, test_data)
plot_training_history(history)
model.save('card_classifier_model_ver3.h5')"""

(training_images, training_labels), (test_images, test_labels) = getTrainingData()

# Normalize and shuffle
training_images = training_images / 255.0
test_images = test_images / 255.0

training_images, training_labels = shuffle(training_images, training_labels)

# Build and train the model
model, history = trainModel((training_images, training_labels), (test_images, test_labels))

# Save the trained model
model.save("card_classifier_ver4.h5")

"""model = tf.keras.models.load_model('card_classifier_model_ver3.h5')
img = cv2.imread("../sample_images/yugioh/Deskbot_007.jpg")
imgSmall = cv2.resize(img, (128, 128), cv2.INTER_AREA)

img_array = tf.expand_dims(imgSmall, 0) # Create a batch of size 1

predictions = model.predict(img_array)
(predicted_class, confidence) = predict_card(model, img_array)
print(predicted_class)
print(confidence)
print(predictions)"""

def getAndHashAllMtgCards(mtgDataset, mtgCompleted, mtgMissed):
    counter = 0
    filename = "../sample_images/mtg/temp.jpg"
    mtgSets = mtgDataset["data"]

    try:
        for setcode in mtgSets.keys():
            cardList = mtgSets[setcode]["cards"]
            for card in cardList:
                counter += 1
                if counter % 5000 == 0:
                    with open("./cardHashes/mtg_dphash_16byte.json", "w") as f:
                        json.dump(mtgCompleted, f, indent=4)
                    with open("./cardHashes/missed_mtg_cards.json", "w") as f:
                        json.dump(mtgMissed, f, indent=4)
    
                name = card["name"]
                setcode = card["setCode"]
                if not "scryfallId" in card["identifiers"]:
                    mtgMissed[name] = {"name": name, "setcode": setcode, "scryfallId": "None"}
                    continue

                scryfallId = card["identifiers"]["scryfallId"]
                if scryfallId in mtgCompleted: 
                    print(f'Already have: {name}')
                    continue
                scryFallCard = getMTGCard(scryfallId, filename)
                if scryFallCard is not None:
                    image = cv2.imread(filename)
                    hash_val = hash_image(image)
                    mtgCompleted[scryfallId] = {"name": name, "setcode": setcode, "hash": hash_val, "scryfallId": scryfallId}
                else:
                    mtgMissed[name] = {"name": name, "setcode": setcode, "scryfallId": scryfallId}
                print(f'Downloaded {counter} Cards.  Last: {name}')
    except KeyboardInterrupt:
        print("Saving Hashes Now")
    finally:
        with open("./cardHashes/mtg_dphash_16byte.json", "w") as f:
            json.dump(mtgCompleted, f, indent=4)

        with open("./cardHashes/missed_mtg_cards.json", "w") as f:
            json.dump(mtgMissed, f, indent=4)

def getAndHashAllYugiohCards(yugiohCompleted, yugiohMissed):
    url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
    resp = requests.get(url)
    if resp.status_code == 200:
        cardData = resp.json()
        cards = cardData["data"]

        counter = 1
        try:
            for i,card in enumerate(cards):
                if counter % 5000 == 0:
                    with open("./cardHashes/yugioh_dphash_16byte.json", "w") as f:
                        json.dump(yugiohCompleted, f, indent=4)
                    with open("./cardHashes/missed_yugioh_cards.json", "w") as f:
                        json.dump(yugiohMissed, f, indent=4)

                img_url = card["card_images"][0]["image_url"]
                card_name = card["name"]
                card_id = card["id"]
                if str(card_id) in yugiohCompleted: continue

                if not "card_sets" in card: continue
                sets = card["card_sets"]
                setcodes = []
                for set in sets:
                    setcodes.append(set["set_code"])
                filename = f'../sample_images/yugioh/temp_img.jpg'

                img = requests.get(img_url)
                if img.status_code == 200:
                    counter += 1
                    with open(filename, "wb") as img_file:
                        img_file.write(img.content)
                    image = cv2.imread(filename)
                    hash = hash_image(image)
                    yugiohCompleted[card_id] = {"id": card_id, "setcodes": setcodes, "name": card_name, "hash": hash}
                else:
                    yugiohMissed[card_id] = {"id": card_id, "setcodes": setcodes, "name": card_name}
                print(f'Downloaded {i} Cards. Last: {card_name}')
        except KeyboardInterrupt:
            print("Saving Hashes")
        finally:
            with open("./cardHashes/yugioh_dphash_16byte.json", "w") as f:
                json.dump(yugiohCompleted, f, indent=4)

            with open("./cardHashes/missed_yugioh_cards.json", "w") as f:
                json.dump(yugiohMissed, f, indent=4)


"""with open("./cardHashes/yugioh_dphash_16byte.json", 'r', encoding='utf-8') as completed_json_file:
    yugiohCompleted = json.load(completed_json_file)

with open("./cardHashes/missed_yugioh_cards.json", 'r', encoding='utf-8') as missed_json_file:
    yugiohMissed = json.load(missed_json_file)

getAndHashAllYugiohCards(yugiohCompleted, yugiohMissed)"""