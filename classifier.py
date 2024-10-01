import requests
import random
import os
import time

def getYugiohSample(numCards):
    url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
    resp = requests.get(url)
    if resp.status_code == 200:
        cardData = resp.json()
        cards = cardData["data"]

        random.shuffle(cards)
        sample = cards[:numCards]

        counter = 0

        for i,card in enumerate(sample):
            img_url = card["card_images"][0]["image_url"]
            card_name = card["name"].replace(' ', '_').replace('/', '_')
            filename = f'sample_images/yugioh/{card_name}.jpg'

            img = requests.get(img_url)
            if img.status_code == 200:
                counter += 1
                with open(filename) as img_file:
                    img_file.write(img.content)
                print(f'Downloaded {card_name} Num: {i}/{numCards} Total Downloaded: {counter}')
            else:
                print(f'Failed to get Image of: {card_name}')
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
            img_url = cardData["image_uris"]["large"]
            card_name = cardData["name"].replace(' ', '_').replace('/', '_')
            filename = f'sample_images/mtg/{card_name}.jpg'

            img = requests.get(img_url)
            if img.status_code == 200:
                counter += 1
                with open(filename) as img_file:
                    img_file.write(img.content)
                print(f'Downloaded {card_name} Total Downloaded: {counter}')
            else:
                print(f'Failed to get Image of: {card_name}')
        else:
            print(f'API Gave Error Status Code: {resp.status_code}')
        
        time.sleep(0.2)
