import bson
import json
import requests
import os
from pymongo import database
from flask import Flask, request, jsonify, current_app, g, Blueprint, render_template
from fuzzywuzzy import fuzz
from werkzeug.local import LocalProxy
from flask_pymongo import PyMongo
from pymongo.errors import DuplicateKeyError, OperationFailure
from bson.objectid import ObjectId
from bson.errors import InvalidId
from flask_cors import CORS
from json import JSONEncoder
from bson import json_util, ObjectId
from datetime import datetime, timedelta
from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi

uri = "mongodb+srv://jaredanderson676:lYo67dNxPpT6RPAW@tcg-card-detection-dev.szrau.mongodb.net/?retryWrites=true&w=majority&appName=TCG-Card-Detection-Dev"


app = Flask(__name__)

#Dummy Test Data
testCardImageData = [{'imageid': 1, 'name': 'Dark Magician', 'cardid': '46986414', 'setcode': 'LOB-EN005'},
        {'imageid': 2, 'name': 'Blue-Eyes White Dragon', 'cardid': '89631139', 'setcode': 'LOB-EN001'},
        {'imageid': 3, 'name': 'Red-Eyes Black Dragon', 'cardid': '74677422', 'setcode': 'LOB-EN070'}]

testCardContentData = [{'cardID': '46986414', 'card-name': 'Dark Magician', 'card-text': 'The ultimate wizard in terms of attack and defense.', 'atk': 2500, 'def': 2100},
                       {'cardID': '89631139', 'card-name': 'Blue-Eyes White Dragon', 'card-text': 'This legendary dragon is a powerful engine of destruction. Virtualy invincible, very few have faced this awesome creature and lived to tell the tale.', 'atk': 3000, 'def': 2500},
                       {'cardID': '74677422', 'card-name': 'Red-Eyes Black Dragon', 'card-text': 'A ferocious dragon with a deadly attack', 'atk': 2400, 'def': 2000}]



#MongoDB Integration
    
def parse_json(data):
    return json.loads(json_util.dumps(data))

client = MongoClient(uri, server_api=ServerApi('1'))

#Flask Initialization
def create_app():

    app = Flask(__name__)
    
    #Web API Endpoints

    @app.get("/testCardImageData")
    def get_testCardImageData():
        return jsonify(testCardImageData)

    @app.get("/testCardContentData")
    def get_testCardContentData():
        return jsonify(testCardContentData)

    @app.post("/getCardInfo")
    def post_getCardInfo():
        if request.is_json:
            clientCardInfo = request.get_json()
            card = processCardImage(clientCardInfo)
            if 'error' in card:
                return {"error": card['error']}, 415
            return card, 201
        return {"error": "Request must be JSON"}, 415
    
    @app.post("/getUserCollection")
    def post_getUserCollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            results = collection.find({'userid' : clientUserInfo['userid']})
            results = parse_json(results)
            return results, 201
        return {"error": "Request must be JSON"}, 415
    @app.post("/addToUserCollection")
    def post_addToUserCollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            payload = {'userid' : clientUserInfo['userid'], 'cardid' : clientUserInfo['cardid'], 'setcode' : clientUserInfo['setcode']}
            if 'rarity' in clientUserInfo:
                payload['rarity'] = clientUserInfo['rarity']
            else :
                payload['rarity'] = ""
            results = collection.find_one(payload)
            if results == None:
                payload['quantity'] = clientUserInfo['quantity']
                insertRes = collection.insert_one(payload)
                return {'success' : insertRes.acknowledged}, 201
            else :
                results = parse_json(results)
                currCount = results['quantity']
                currCount += clientUserInfo['quantity']
                updateOp = { "$set" : 
                                { "quantity" : currCount }
                            }
                upRes = collection.update_one(payload, updateOp)
                return {'Message' : 'Successful Update!', 'count' : upRes.modified_count}, 201
        return {"error": "Request must be JSON"}, 415
    @app.post("/removeFromUserCollection")
    def post_removeFromUserCollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            payload = {'userid' : clientUserInfo['userid'], 'cardid' : clientUserInfo['cardid'], 'setcode' : clientUserInfo['setcode']}
            if 'rarity' in clientUserInfo:
                payload['rarity'] = clientUserInfo['rarity']
            else :
                payload['rarity'] = ""
            results = collection.find_one(payload)
            if results == None:
                return {"error": "Tried to remove a card that you don't have"}, 415
            if clientUserInfo['quantity'] == 'all' or results['quantity'] <= clientUserInfo['quantity']:
                delRes = collection.delete_one(payload)
                return {'Message' : 'Card removed from collection', 'remCnt' : delRes.deleted_count}, 201
            currCount = results['quantity']
            currCount -= clientUserInfo['quantity']
            updateOp = { "$set" : 
                            { "quantity" : currCount }
                        }
            upRes = collection.update_one(payload, updateOp)
            return {'Message' : 'Successful Update!', 'count' : upRes.modified_count}, 201
        return {"error": "Request must be JSON"}, 415

    return app

#Methods

def processCardImage(cardImageData):
    #Replace enclosed with actual card image parsing
    cardId = cardImageData['cardid']
    cardSetCode = cardImageData['setcode']
    cardName = cardImageData['name']
    cardGame = cardImageData['game']
    setName = ""
    #End dummy code
    if cardGame == 'yugioh':
        url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
        payload = {'id': cardId, 'tcgplayer_data': None}
        payload = '&'.join([k if v is None else f"{k}={v}" for k, v in payload.items()])
        resp = requests.get(url, params=payload)
        if resp.status_code == 200:
            cardData = resp.json()
            if fuzz.ratio(cardData['data'][0]['name'], cardName) < 90:
                print("Fuzzy ratio is: ", fuzz.ratio(cardData['data'][0]['name'], cardName))
                return {'error': 'card search returned wrong card. please try scanning again'}
            print("Fuzzy ratio is: ", fuzz.ratio(cardData['data'][0]['name'], cardName))
            for setData in cardData['data'][0]['card_sets']:
                if fuzz.ratio(setData['set_code'], cardSetCode) > 95:
                   setName = setData['set_name']
                   break
            if setName == "":
                return {'error': 'scanned set code does not match a valid printing. Card could be fake or try scanning again'}
            return cardData
        return {'error': 'card not found'} 
    if cardGame == 'mtg':
        url = 'https://api.scryfall.com/cards/named'
        headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
        payload = {'exact' : cardName}
        resp = requests.get(url, params=payload, headers=headers)
        print(resp.url)
        if resp.status_code == 200:
            cardData = resp.json()
            return cardData
        return {'error': 'card not found'} 
    if cardGame == 'pokemon':
        return {'error': 'card not found'} 
    return {'error': 'card not found'} 

#Main Run Method
if __name__ == "__main__":
    app = create_app()

    app.run()