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

load_dotenv(find_dotenv())

def getYugiohSample(numCards):
    url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
    resp = requests.get(url)
    if resp.status_code == 200:
        cardData = resp.json()
        cards = cardData["data"]

        random.shuffle(cards)

        counter = 0

        for i,card in enumerate(cards):
            img_url = card["card_images"][0]["image_url"]
            card_name = card["name"].replace(' ', '_').replace('/', '_')
            card_name = sanitize_filename(card_name)
            filename = f'sample_images/yugioh/{card_name}.jpg'

            img = requests.get(img_url)
            if img.status_code == 200:
                counter += 1
                with open(filename, "wb") as img_file:
                    img_file.write(img.content)
                print(f'Downloaded {card_name} Num: {i}/{numCards} Total Downloaded: {counter}')
            else:
                print(f'Failed to get Image of: {card_name}')
            if counter >= numCards: break
            time.sleep(0.25)
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
            filename = f'sample_images/mtg/{card_name}.jpg'

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
        
        time.sleep(0.25)

def getPokemonSample(numCards):
    apikey = os.getenv('POKEMON_API_KEY')
    url = 'https://api.pokemontcg.io/v2/cards'
    headers = {'X-Api-Key' : apikey}
    params = {'pageSize' : 250}

    resp = requests.get(url, headers=headers, params=params)

    if resp.status_code == 200:
        card_data = resp.json()
        cards = card_data["data"]

        random.shuffle(cards)
        counter = 0

        for i, card in enumerate(cards):
            image_url = card['images']['large']
            card_name = card['name'].replace(' ', '_').replace('/', '_')
            card_name = sanitize_filename(card_name)
            filename = f"sample_images/pokemon/{card_name}.jpg"

            img_response = requests.get(image_url)

            if img_response.status_code == 200:
                counter += 1
                with open(filename, "wb") as img_file:
                    img_file.write(img_response.content)
                print(f"Downloaded: {card_name} Total Downloaded: {counter}")
            else:
                print(f"Failed to download image for card: {card_name}")
            if counter >= numCards: break
    else:
        print(f'API Gave Error Status Code: {resp.status_code}')

def getTrainingData():
    training_images = []
    training_labels = []
    test_images = []
    test_labels = []

    "getYugiohSample(200)"
    "getMTGSample(200)"
    "getPokemonSample(200)"

    for i, filename in enumerate(os.listdir("./sample_images/yugioh")):
        filepath = os.path.join("./sample_images/yugioh", filename)
        img = cv2.imread(filepath)
        imgSmall = cv2.resize(img, (128, 128), cv2.INTER_AREA)
        if i < 150: 
            training_images.append(imgSmall)
            training_labels.append(0)
        else: 
            test_images.append(imgSmall)
            test_labels.append(0)

    for i, filename in enumerate(os.listdir("./sample_images/mtg")):
        filepath = os.path.join("./sample_images/mtg", filename)
        img = cv2.imread(filepath)
        imgSmall = cv2.resize(img, (128, 128), cv2.INTER_AREA)
        if i < 150: 
            training_images.append(imgSmall)
            training_labels.append(1)
        else: 
            test_images.append(imgSmall)
            test_labels.append(1)

    for i, filename in enumerate(os.listdir("./sample_images/pokemon")):
        filepath = os.path.join("./sample_images/pokemon", filename)
        img = cv2.imread(filepath)
        imgSmall = cv2.resize(img, (128, 128), cv2.INTER_AREA)
        if i < 120: 
            training_images.append(imgSmall)
            training_labels.append(2)
        else: 
            test_images.append(imgSmall)
            test_labels.append(2)

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
        
    epochs = 10
    history = model.fit(
        training_data[0],
        training_data[1],
        validation_data=(test_data[0], test_data[1]),
        epochs=epochs
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

training_data, test_data = getTrainingData()
print(f"Training data: {training_data[0].shape}, Training labels: {training_data[1].shape}")
print(f"Test data: {test_data[0].shape}, Test labels: {test_data[1].shape}")
print(test_data[1])
model, history = trainModel(training_data, test_data)
plot_training_history(history)
model.save('card_classifier_model.h5')