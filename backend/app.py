from ast import List
import base64
from genericpath import exists
from re import sub
from turtle import home
import bson
import json
import requests
import os
import bcrypt
import datetime
import Cards
import cv2
import urllib
from datetime import date, timezone
from dotenv import load_dotenv, find_dotenv
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
load_dotenv(find_dotenv())


uri = os.getenv('MONGODB_STRING')


app = Flask(__name__)

#Dummy Test Data
testCardImageData = [{'imageid': 1, 'name': 'Dark Magician', 'cardid': '46986414', 'setcode': 'LOB-EN005'},
        {'imageid': 2, 'name': 'Blue-Eyes White Dragon', 'cardid': '89631139', 'setcode': 'LOB-EN001'},
        {'imageid': 3, 'name': 'Red-Eyes Black Dragon', 'cardid': '74677422', 'setcode': 'LOB-EN070'}]

testCardContentData = [{'cardID': '46986414', 'card-name': 'Dark Magician', 'card-text': 'The ultimate wizard in terms of attack and defense.', 'atk': 2500, 'def': 2100},
                       {'cardID': '89631139', 'card-name': 'Blue-Eyes White Dragon', 'card-text': 'This legendary dragon is a powerful engine of destruction. Virtualy invincible, very few have faced this awesome creature and lived to tell the tale.', 'atk': 3000, 'def': 2500},
                       {'cardID': '74677422', 'card-name': 'Red-Eyes Black Dragon', 'card-text': 'A ferocious dragon with a deadly attack', 'atk': 2400, 'def': 2000}]


UPLOAD_FOLDER = 'uploads/'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

#MongoDB Integration
    
def parse_json(data):
    return json.loads(json_util.dumps(data))

client = MongoClient(uri, server_api=ServerApi('1'))

#Flask Initialization
def create_app():

    app = Flask(__name__)
    
    #Web API Endpoints

    @app.get('/testCardImageData')
    def get_testCardImageData():
        return jsonify(testCardImageData)

    @app.get('/testCardContentData')
    def get_testCardContentData():
        return jsonify(testCardContentData)

    @app.post('/getCardInfo')
    def post_getCardInfo():
        if 'image' not in request.files:
            return {'error': 'No image part'}, 400
    
        image_file = request.files['image']

        image_path = os.path.join(UPLOAD_FOLDER, image_file.filename)
        image_file.save(image_path)

        cards = processCardImage(image_path)
        return jsonify(cards), 201
    
    @app.post('/createNewUser')
    def post_createNewUser():
        if request.is_json:
            userCreateInfo = request.get_json()
            username = userCreateInfo['username']
            authenticator = userCreateInfo['authenticationToken']
            email = userCreateInfo['email']
            storefront = userCreateInfo['storefront']
            if type(username) != str or type(authenticator) != str or type(email) != str or type(storefront) != int:
                return {'error' : 'Incorrect data type passed for one or more inputs'}, 201
            database = client['card_detection_info']
            collection = database['user_data']
            dupCheck = collection.find_one({'username' : username})
            if dupCheck != None:
                return {'error' : 'Username already in use'}, 201
            coreUserInfo = collection.find_one({'coreuser' : 1})
            userid = str(int(coreUserInfo['currentmaxuserid']) + 1)
            updateOp = { '$set' : 
                                { 'currentmaxuserid' : userid }
                            }
            collection.update_one({'coreuser' : 1}, updateOp)
            payload = {'username' : username, 'userid' : userid, 'password' : bcrypt.hashpw(authenticator.encode('UTF-8'),bcrypt.gensalt(rounds=15)), 'email' : email, 'storefront' : storefront}
            insertRes = collection.insert_one(payload)
            return {'success' : insertRes.acknowledged, 'userid' : userid}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/authenticateUser')
    def post_authenticateUser():
        if request.is_json:
            userLoginInfo = request.get_json()
            username = userLoginInfo['username']
            authenticator = userLoginInfo['authenticationToken']
            database = client['card_detection_info']
            collection = database['user_data']
            userData = collection.find_one({'username' : username})
            if userData == None:
                return {'error': 'Incorrect Username and/or Password', 'success' : 0}, 201
            if bcrypt.checkpw(authenticator.encode('UTF-8'), userData['password']):
                cardCollection = database['card_collection']
                userid = userData['userid']
                results = cardCollection.find({'userid' : userid})
                results = parse_json(results)
                for res in results :
                    game = res['game']
                    cardId = res['cardid']
                    cardSetCode = res['setcode']
                    oldPriceDate = datetime.strptime(res['pricedate']['$date'], '%Y-%m-%dT%H:%M:%S.%fZ')
                    oldPriceDate = oldPriceDate.replace(tzinfo=timezone.utc)
                    if (datetime.now(timezone.utc) - oldPriceDate).days == 0:
                        continue
                    price = ''
                    purchaseURL = ''
                    if game == 'yugioh':
                        url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
                        payload = {'id': cardId, 'tcgplayer_data': None}
                        payload = '&'.join([k if v is None else f'{k}={v}' for k, v in payload.items()])
                        resp = requests.get(url, params=payload)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            for cardSet in cardData['data'][0]['card_sets']:
                                if cardSet['set_code'] == cardSetCode:
                                    price = cardSet['set_price']
                                    purchaseURL = urllib.parse.unquote(cardSet['set_url'].split('u=')[1], encoding='utf-8', errors='replace')
                                    break
                    elif game == 'mtg':
                        url = 'https://api.scryfall.com/cards/'
                        headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
                        url += cardId
                        resp = requests.get(url, headers=headers)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            price = cardData['prices']['usd']
                            purchaseURL = urllib.parse.unquote(cardData['purchase_uris']['tcgplayer'].split('u=')[1], encoding='utf-8', errors='replace')
                    elif game == 'pokemon':
                        apikey = os.getenv('POKEMON_API_KEY')
                        url = 'https://api.pokemontcg.io/v2/cards/'
                        headers = {'X-Api-Key' : apikey}
                        url += cardId
                        resp = requests.get(url, headers=headers)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            if res['rarity'] in list(cardData['data']['tcgplayer']['prices'].keys()):
                                price = cardData['data']['tcgplayer']['prices'][res['rarirty']]['market']
                            elif "normal" in list(cardData['data']['tcgplayer']['prices'].keys()):
                                price = cardData['data']['tcgplayer']['prices']['normal']['market']
                            else:
                                dictKey = list(cardData['data']['tcgplayer']['prices'].keys())[0]
                                price = cardData['data']['tcgplayer']['prices'][dictKey]['market']
                            purchaseURL = cardData['data']['tcgplayer']['url']
                    payload = {'cardid' : cardId, 'setcode' : cardSetCode, 'game' : game, 'rarity' : res['rarity']}
                    if (not 'purchaseurl' in res) and purchaseURL != '':
                        updateOp = updateOp = { '$set' : 
                                { 'price' : price,
                                  'pricedate' : datetime.now(timezone.utc),
                                  'purchaseurl': purchaseURL
                                }
                            }
                    else:
                        updateOp = updateOp = { '$set' : 
                                    { 'price' : price,
                                      'pricedate' : datetime.now(timezone.utc)
                                    }
                                }
                    cardCollection.update_many(payload, updateOp)
                userData['success'] = 1
                userData.pop("password")
                userData.pop("_id")
                return userData, 201
            else :
                print("Test")
                return {'error': 'Incorrect Username and/or Password', 'success' : 0}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201

    @app.post('/authenticateGoogleUser')
    def post_authenticateGoogleUser():
        if request.is_json:
            userLoginInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['user_data']
            email = userLoginInfo['email']
            userData = collection.find_one({'email' : email})
            if userData == None:
                userCreateInfo = request.get_json()
                username = ""
                authenticator = ""
                googleid = userLoginInfo['googleid']
                storefront = 1
                if type(email) != str:
                    return {'error' : 'Incorrect data type passed for one or more inputs'}, 201
                coreUserInfo = collection.find_one({'coreuser' : 1})
                userid = str(int(coreUserInfo['currentmaxuserid']) + 1)
                updateOp = { '$set' : 
                                    { 'currentmaxuserid' : userid }
                                }
                collection.update_one({'coreuser' : 1}, updateOp)
                payload = {'username' : username, 'userid' : userid, 'password' : authenticator, 'email' : email, 'storefront' : storefront, 'googleid' : googleid}
                insertRes = collection.insert_one(payload)
                userData = collection.find_one({'googleid' : googleid})
                userData['success'] = 1
                userData.pop("password")
                userData.pop("_id")
                userData.pop("googleid")
                return userData, 201
            else:
                cardCollection = database['card_collection']
                userid = userData['userid']
                results = cardCollection.find({'userid' : userid})
                results = parse_json(results)
                for res in results :
                    game = res['game']
                    cardId = res['cardid']
                    cardSetCode = res['setcode']
                    oldPriceDate = datetime.strptime(res['pricedate']['$date'], '%Y-%m-%dT%H:%M:%S.%fZ')
                    oldPriceDate = oldPriceDate.replace(tzinfo=timezone.utc)
                    if (datetime.now(timezone.utc) - oldPriceDate).days == 0:
                        continue
                    price = ''
                    purchaseURL = ''
                    if game == 'yugioh':
                        url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
                        payload = {'id': cardId, 'tcgplayer_data': None}
                        payload = '&'.join([k if v is None else f'{k}={v}' for k, v in payload.items()])
                        resp = requests.get(url, params=payload)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            for cardSet in cardData['data'][0]['card_sets']:
                                if cardSet['set_code'] == cardSetCode:
                                    price = cardSet['set_price']
                                    purchaseURL = urllib.parse.unquote(cardSet['set_url'].split('u=')[1], encoding='utf-8', errors='replace')
                                    break
                    elif game == 'mtg':
                        url = 'https://api.scryfall.com/cards/'
                        headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
                        url += cardId
                        resp = requests.get(url, headers=headers)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            price = cardData['prices']['usd']
                            purchaseURL = urllib.parse.unquote(cardData['purchase_uris']['tcgplayer'].split('u=')[1], encoding='utf-8', errors='replace')
                    elif game == 'pokemon':
                        apikey = os.getenv('POKEMON_API_KEY')
                        url = 'https://api.pokemontcg.io/v2/cards/'
                        headers = {'X-Api-Key' : apikey}
                        url += cardId
                        resp = requests.get(url, headers=headers)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            if res['rarity'] in list(cardData['data']['tcgplayer']['prices'].keys()):
                                price = cardData['data']['tcgplayer']['prices'][res['rarirty']]['market']
                            elif "normal" in list(cardData['data']['tcgplayer']['prices'].keys()):
                                price = cardData['data']['tcgplayer']['prices']['normal']['market']
                            else:
                                dictKey = list(cardData['data']['tcgplayer']['prices'].keys())[0]
                                price = cardData['data']['tcgplayer']['prices'][dictKey]['market']
                            purchaseURL = cardData['data']['tcgplayer']['url']
                    payload = {'cardid' : cardId, 'setcode' : cardSetCode, 'game' : game, 'rarity' : res['rarity']}
                    if (not 'purchaseurl' in res) and purchaseURL != '':
                        updateOp = updateOp = { '$set' : 
                                { 'price' : price,
                                  'pricedate' : datetime.now(timezone.utc),
                                  'purchaseurl': purchaseURL
                                }
                            }
                    else:
                        updateOp = updateOp = { '$set' : 
                                    { 'price' : price,
                                      'pricedate' : datetime.now(timezone.utc)
                                    }
                                }
                    cardCollection.update_many(payload, updateOp)
                userData['success'] = 1
                userData.pop("password")
                userData.pop("_id")
                userData.pop("googleid")
                return userData, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201

    @app.post('/authenticateFacebookUser')
    def post_authenticateFacebookUser():
        if request.is_json:
            userLoginInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['user_data']
            fbid = userLoginInfo['fbid']
            userData = collection.find_one({'fbid' : fbid})
            if userData == None:
                userCreateInfo = request.get_json()
                username = ""
                authenticator = ""
                email = userLoginInfo['email']
                storefront = 1
                if type(email) != str:
                    return {'error' : 'Incorrect data type passed for one or more inputs'}, 201
                coreUserInfo = collection.find_one({'coreuser' : 1})
                userid = str(int(coreUserInfo['currentmaxuserid']) + 1)
                updateOp = { '$set' : 
                                    { 'currentmaxuserid' : userid }
                                }
                collection.update_one({'coreuser' : 1}, updateOp)
                payload = {'username' : username, 'userid' : userid, 'password' : authenticator, 'email' : email, 'storefront' : storefront, 'fbid' : fbid}
                insertRes = collection.insert_one(payload)
                userData = collection.find_one({'fbid' : fbid})
                userData['success'] = 1
                userData.pop("password")
                userData.pop("_id")
                userData.pop("fbid")
                return userData, 201
            else:
                cardCollection = database['card_collection']
                userid = userData['userid']
                results = cardCollection.find({'userid' : userid})
                results = parse_json(results)
                for res in results :
                    game = res['game']
                    cardId = res['cardid']
                    cardSetCode = res['setcode']
                    oldPriceDate = datetime.strptime(res['pricedate']['$date'], '%Y-%m-%dT%H:%M:%S.%fZ')
                    oldPriceDate = oldPriceDate.replace(tzinfo=timezone.utc)
                    if (datetime.now(timezone.utc) - oldPriceDate).days == 0:
                        continue
                    price = ''
                    purchaseURL = ''
                    if game == 'yugioh':
                        url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
                        payload = {'id': cardId, 'tcgplayer_data': None}
                        payload = '&'.join([k if v is None else f'{k}={v}' for k, v in payload.items()])
                        resp = requests.get(url, params=payload)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            for cardSet in cardData['data'][0]['card_sets']:
                                if cardSet['set_code'] == cardSetCode:
                                    price = cardSet['set_price']
                                    purchaseURL = urllib.parse.unquote(cardSet['set_url'].split('u=')[1], encoding='utf-8', errors='replace')
                                    break
                    elif game == 'mtg':
                        url = 'https://api.scryfall.com/cards/'
                        headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
                        url += cardId
                        resp = requests.get(url, headers=headers)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            price = cardData['prices']['usd']
                            purchaseURL = urllib.parse.unquote(cardData['purchase_uris']['tcgplayer'].split('u=')[1], encoding='utf-8', errors='replace')
                    elif game == 'pokemon':
                        apikey = os.getenv('POKEMON_API_KEY')
                        url = 'https://api.pokemontcg.io/v2/cards/'
                        headers = {'X-Api-Key' : apikey}
                        url += cardId
                        resp = requests.get(url, headers=headers)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            if res['rarity'] in list(cardData['data']['tcgplayer']['prices'].keys()):
                                price = cardData['data']['tcgplayer']['prices'][res['rarirty']]['market']
                            elif "normal" in list(cardData['data']['tcgplayer']['prices'].keys()):
                                price = cardData['data']['tcgplayer']['prices']['normal']['market']
                            else:
                                dictKey = list(cardData['data']['tcgplayer']['prices'].keys())[0]
                                price = cardData['data']['tcgplayer']['prices'][dictKey]['market']
                            purchaseURL = cardData['data']['tcgplayer']['url']
                    payload = {'cardid' : cardId, 'setcode' : cardSetCode, 'game' : game, 'rarity' : res['rarity']}
                    if (not 'purchaseurl' in res) and purchaseURL != '':
                        updateOp = updateOp = { '$set' : 
                                { 'price' : price,
                                  'pricedate' : datetime.now(timezone.utc),
                                  'purchaseurl': purchaseURL
                                }
                            }
                    else:
                        updateOp = updateOp = { '$set' : 
                                    { 'price' : price,
                                      'pricedate' : datetime.now(timezone.utc)
                                    }
                                }
                    cardCollection.update_many(payload, updateOp)
                userData['success'] = 1
                userData.pop("password")
                userData.pop("_id")
                userData.pop("fbid")
                return userData, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/saveUsername')
    def post_saveUsername():
        if request.is_json:
            userPassInfo = request.get_json()
            userid = userPassInfo['userid']
            username = userPassInfo['username']
            database = client['card_detection_info']
            collection = database['user_data']
            userData = collection.find_one({'userid' : userid})
            if userData == None:
                return {'error': 'Incorrect UserID', 'success' : 0}, 201
            updateOp = { '$set' : 
                                { 'username' : username }
                            }
            collection.update_one({'userid' : userid}, updateOp)
            return {'success' : 1}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/saveUserPass')
    def post_saveUserPass():
        if request.is_json:
            userPassInfo = request.get_json()
            userid = userPassInfo['userid']
            authenticator = userPassInfo['authenticationToken']
            database = client['card_detection_info']
            collection = database['user_data']
            userData = collection.find_one({'userid' : userid})
            if userData == None:
                return {'error': 'Incorrect UserID', 'success' : 0}, 201
            updateOp = { '$set' : 
                                { 'password' : bcrypt.hashpw(authenticator.encode('UTF-8'),bcrypt.gensalt(rounds=15)) }
                            }
            collection.update_one({'userid' : userid}, updateOp)
            return {'success' : 1}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201

    @app.post('/saveUserEmail')
    def post_saveUserEmail():
        if request.is_json:
            userEmailInfo = request.get_json()
            userid = userEmailInfo['userid']
            email = userEmailInfo['email']
            database = client['card_detection_info']
            collection = database['user_data']
            userData = collection.find_one({'userid' : userid})
            if userData == None:
                return {'error': 'Incorrect UserID', 'success' : 0}, 201
            updateOp = { '$set' : 
                                { 'email' : email }
                            }
            collection.update_one({'userid' : userid}, updateOp)
            return {'success' : 1}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/saveUserStorefront')
    def post_saveUserStorefront():
        if request.is_json:
            userStoreInfo = request.get_json()
            userid = userStoreInfo['userid']
            storefront = userStoreInfo['storefront']
            if type(storefront) != int:
                return {'error': 'Incorrect Storefront Value Type', 'success' : 0}, 201
            if storefront > 2 or storefront < 0:
                return {'error': 'Storefront Value Not Supported', 'success' : 0}, 201
            database = client['card_detection_info']
            collection = database['user_data']
            userData = collection.find_one({'userid' : userid})
            if userData == None:
                return {'error': 'Incorrect UserID', 'success' : 0}, 201
            updateOp = { '$set' : 
                                { 'storefront' : storefront }
                            }
            collection.update_one({'userid' : userid}, updateOp)
            return {'success' : 1}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201

    @app.post('/deleteUser')
    def post_deleteUser():
        if request.is_json:
            userDeleteInfo = request.get_json()
            userid = userDeleteInfo['userid']
            database = client['card_detection_info']
            userCollection = database['user_data']
            cardCollection = database['card_collection']
            subcolCollection = database['subcollection_info']
            userData = userCollection.find_one({'userid' : userid})
            if userData == None:
                return {'error': 'Incorrect UserID', 'success' : 0}, 201
            payload = {'userid' : userid}
            userDel = userCollection.delete_one(payload)
            cardDel = cardCollection.delete_many(payload)
            subDel = subcolCollection.delete_many(payload)
            return {'Message' : 'User removed', 'remUserCnt' : userDel.deleted_count, 'remCardCnt' : cardDel.deleted_count, 'remSubcolCount': subDel.deleted_count, 'success' : 1}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/getUserCollection')
    def post_getUserCollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            results = collection.find({'userid' : clientUserInfo['userid']})
            results = parse_json(results)
            for card in results:
                card.pop('_id')
                card.pop('pricedate')
            return results, 201
        return {'error': 'Request must be JSON'}, 201
    
    @app.post('/getCardImage')
    def post_getCardImage():
        if request.is_json:
            clientUserInfo = request.get_json()
            game = clientUserInfo['game']
            cardId = clientUserInfo['cardid']
            response = {}
            if game == 'yugioh':
                filename = './backend/cardImages/' + cardId + '_yugioh.jpg'
                if not os.path.isfile(filename):
                    url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
                    payload = {'id': cardId, 'tcgplayer_data': None}
                    payload = '&'.join([k if v is None else f'{k}={v}' for k, v in payload.items()])
                    resp = requests.get(url, params=payload)
                    if resp.status_code == 200:
                        cardData = resp.json()
                        print(filename)
                        img = requests.get(cardData['data'][0]['card_images'][0]['image_url'])
                        if img.status_code == 200:
                            with open(filename, 'wb') as img_file:
                                img_file.write(img.content)
                            response['image'] = base64.b64encode(img.content).decode()
                        else:
                            print(f'Failed to get Image of: {cardId}')
                else:
                    with open(filename, "rb") as image_file:
                        response['image'] = base64.b64encode(image_file.read()).decode()
            elif game == 'mtg':
                filename = './backend/cardImages/' + cardId + '_mtg.jpg'
                if not os.path.isfile(filename):
                    url = 'https://api.scryfall.com/cards/'
                    headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
                    url += cardId
                    resp = requests.get(url, headers=headers)
                    if resp.status_code == 200:
                        cardData = resp.json()
                        if 'image_uris' in cardData:
                            img_url = cardData['image_uris']['large']
                            
                            img = requests.get(img_url)
                            if img.status_code == 200:
                                with open(filename, "wb") as img_file:
                                    img_file.write(img.content)
                                    response['image'] = base64.b64encode(img.content).decode()
                            else:
                                print(f'Failed to get Image of: {cardId}')
                else:
                    with open(filename, "rb") as image_file:
                        response['image'] = base64.b64encode(image_file.read()).decode()
            elif game == 'pokemon':
                filename = './backend/cardImages/' + cardId + '_pokemon.jpg'
                if not os.path.isfile(filename):
                    apikey = os.getenv('POKEMON_API_KEY')
                    url = 'https://api.pokemontcg.io/v2/cards/'
                    headers = {'X-Api-Key' : apikey}
                    url += cardId
                    resp = requests.get(url, headers=headers)
                    if resp.status_code == 200:
                        cardData = resp.json()
                        image_url = cardData['data']['images']['large']
                            
                        img_response = requests.get(image_url)
                        if img_response.status_code == 200:
                            with open(filename, "wb") as img_file:
                                img_file.write(img_response.content)
                                response['image'] = base64.b64encode(img_response.content).decode()
                        else:
                            print(f"Failed to download image for card: {cardId}")
                else:
                    with open(filename, "rb") as image_file:
                        response['image'] = base64.b64encode(image_file.read()).decode()
            return response, 201
        return {'error': 'Request must be JSON'}, 201

    @app.post('/getUserSubcollection')
    def post_getUserSubcollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            results = collection.find({'userid' : clientUserInfo['userid'], 'subcollections' : clientUserInfo['subcollection']})
            results = parse_json(results)
            return results, 201
        return {'error': 'Request must be JSON'}, 201
    
    @app.post('/addToUserCollection')
    def post_addToUserCollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            payload = {'userid' : clientUserInfo['userid'], 'cardid' : clientUserInfo['cardid'], 'setcode' : clientUserInfo['setcode'], 'game' : clientUserInfo['game'], 'cardname' : clientUserInfo['cardname']}
            if 'rarity' in clientUserInfo:
                payload['rarity'] = clientUserInfo['rarity']
            else :
                payload['rarity'] = ''
            result = collection.find_one(payload)
            if payload['game'] == 'yugioh':
                if "level" in clientUserInfo:
                    payload['level'] = clientUserInfo['level']
                if "attribute" in clientUserInfo:
                    payload['attribute'] = clientUserInfo['attribute']
                if "type" in clientUserInfo:                    
                    payload['type'] = clientUserInfo['type']
                if "description" in clientUserInfo:                    
                    payload['description'] = clientUserInfo['description']
                if "atk" in clientUserInfo:                    
                    payload['atk'] = clientUserInfo['atk']
                if "def" in clientUserInfo:    
                    payload['def'] = clientUserInfo['def']
            elif payload['game'] == 'mtg':
                if "cost" in clientUserInfo:
                    payload['cost'] = clientUserInfo['cost']
                if "type" in clientUserInfo:
                    payload['type'] = clientUserInfo['type']
                if "description" in clientUserInfo:
                    payload['description'] = clientUserInfo['description'] #This is the effect text
                if "attribute" in clientUserInfo:
                    payload['attribute'] = clientUserInfo['attribute'] #This is the color
                if "atk" in clientUserInfo:
                    payload['atk'] = clientUserInfo['atk'] #This is the power
                if "def" in clientUserInfo:
                    payload['def'] = clientUserInfo['def'] #This is the toughness
            else:
                if "cost" in clientUserInfo:
                    payload['cost'] = clientUserInfo['attribute'] #This is the energy type
                if "type" in clientUserInfo:
                    payload['type'] = clientUserInfo['type'] #This is the stage
                if "attacks" in clientUserInfo:
                    payload['attacks'] = clientUserInfo['attacks']
                if "weakenesses" in clientUserInfo:
                    payload['weakenesses'] = clientUserInfo['weaknesses']
                if "hp" in clientUserInfo:
                    payload['hp'] = clientUserInfo['hp']
                if "retreat" in clientUserInfo:
                    payload['retreat'] = clientUserInfo['retreat']
            if result == None:
                payload['quantity'] = clientUserInfo['quantity']
                payload['price'] = clientUserInfo['price']
                payload['pricedate'] = datetime.now(timezone.utc)
                insertRes = collection.insert_one(payload)
                if insertRes.acknowledged: return {'success' : 1}, 201
                else: return {'success' : 0}, 201
            else :
                currCount = result['quantity']
                currCount += clientUserInfo['quantity']
                updateOp = { '$set' : 
                                { 'quantity' : currCount,
                                  'price' : clientUserInfo['price'],
                                  'pricedate' : datetime.now(timezone.utc)}
                            }
                upRes = collection.update_one(payload, updateOp)
                return {'success' : 1}, 201
        return {'error': 'Request must be JSON'}, 201
    
    @app.post('/addToUserSubcollection')
    def post_addToUserSubcollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            payload = {'userid' : clientUserInfo['userid'], 'cardid' : clientUserInfo['cardid'], 'setcode' : clientUserInfo['setcode'], 'game' : clientUserInfo['game']}
            if 'rarity' in clientUserInfo:
                payload['rarity'] = clientUserInfo['rarity']
            else :
                payload['rarity'] = ''
            result = collection.find_one(payload)
            if result == None:
                return {'error': 'You do not have this card'}, 201
            else :
                subColArr = []
                if 'subcollections' in result:
                    subColArr = result['subcollections']
                subCol = clientUserInfo['subcollection']
                subColArr.append(subCol)
                updateOp = { '$set' : 
                                { 'subcollections' : subColArr}
                            }
                upRes = collection.update_one(payload, updateOp)
                return {'Message' : 'Successful Update!', 'count' : upRes.modified_count}, 201
        return {'error': 'Request must be JSON'}, 201

    @app.post('/removeFromUserSubcollection')
    def post_removeFromUserSubcollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            payload = {'userid' : clientUserInfo['userid'], 'cardid' : clientUserInfo['cardid'], 'setcode' : clientUserInfo['setcode'], 'game' : clientUserInfo['game']}
            if 'rarity' in clientUserInfo:
                payload['rarity'] = clientUserInfo['rarity']
            else :
                payload['rarity'] = ''
            result = collection.find_one(payload)
            if result == None:
                return {'error': 'You do not have this card'}, 201
            if 'subcollections' in result:
                subColArr = result['subcollections']
            else:
                return {'error': 'This card is not in any subcollections'}, 201
            subCol = clientUserInfo['subcollection']
            if subCol in subColArr:
                subColArr.remove(subCol)
                updateOp = { '$set' : 
                            { 'subcollections' : subColArr}
                        }
                upRes = collection.update_one(payload, updateOp)
                return {'Message' : 'Successful Update!', 'count' : upRes.modified_count}, 201
            else:
                return {'error': 'Not a part of that subcollection'}, 201
            
        return {'error': 'Request must be JSON'}, 201
    
    @app.post('/removeFromUserCollection')
    def post_removeFromUserCollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            payload = {'userid' : clientUserInfo['userid'], 'cardid' : clientUserInfo['cardid'], 'setcode' : clientUserInfo['setcode']}
            if 'rarity' in clientUserInfo:
                payload['rarity'] = clientUserInfo['rarity']
            else :
                payload['rarity'] = ''
            results = collection.find_one(payload)
            if results == None:
                return {'error': 'Tried to remove a card that you do not have'}, 201
            if clientUserInfo['quantity'] == 'all' or results['quantity'] <= clientUserInfo['quantity']:
                delRes = collection.delete_one(payload)
                return {'Message' : 'Card removed from collection', 'remCnt' : delRes.deleted_count}, 201
            currCount = results['quantity']
            currCount -= clientUserInfo['quantity']
            updateOp = { '$set' : 
                            { 'quantity' : currCount }
                        }
            upRes = collection.update_one(payload, updateOp)
            return {'Message' : 'Successful Update!', 'count' : upRes.modified_count}, 201
        return {'error': 'Request must be JSON'}, 201
    
    @app.post('/getUserSubcollectionInfo')
    def post_getUserSubcollectionInfo():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['subcollection_info']
            payload = {'userid' : clientUserInfo['userid']}
            subCols = collection.find(payload)
            subCols = parse_json(subCols)
            return {'success' : 1, 'subcollections' : subCols}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/createUserSubcollection')
    def post_createUserSubcollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['subcollection_info']
            payload = {'userid' : clientUserInfo['userid'], 'name' : clientUserInfo['name'], 'isDeck' : clientUserInfo['isDeck'], 'game' : clientUserInfo['game'], 'physLoc' : clientUserInfo['physLoc']}
            coreSubcollectionInfo = collection.find_one({'coresubcollection' : 1})
            subcollectionid = str(int(coreSubcollectionInfo['currentmaxid']) + 1)
            updateOp = { '$set' : 
                                { 'currentmaxid' : subcollectionid }
                            }
            collection.update_one({'coresubcollection' : 1}, updateOp)
            payload['subcollectionid'] = subcollectionid
            insertRes = collection.insert_one(payload)
            return {'success' : 1, 'subcollectionid' : subcollectionid}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/updateUserSubcollection')
    def post_updateUserSubcollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['subcollection_info']
            payload = {'subcollectionid' : clientUserInfo['subcollectionid']}
            result = collection.find_one(payload)
            if result == None:
                return {'error': 'Subcollection not found', 'success' : 0}
            updateOp = { '$set' :
                        {
                            'name' : clientUserInfo['name'], 
                            'isDeck' : clientUserInfo['isDeck'], 
                            'physLoc' : clientUserInfo['physLoc']
                            }
                }
            collection.update_one(payload, updateOp)
            return {'success' : 1}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/deleteUserSubcollection')
    def post_deleteUserSubcollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['subcollection_info']
            subcollectionid = clientUserInfo['subcollectionid']
            subCollectionInfo = collection.find_one({'subcollectionid' : subcollectionid})
            if subCollectionInfo == None:
                return {'error': 'Subcollection not found', 'success' : 0}
            cardCollection = database['card_collection']
            collection.delete_one({'subcollectionid' : subcollectionid})
            cardCollection.update_many({}, {'$pull' : {'subcollections' : subcollectionid}})
            return {'success' : 1}, 201
        return {'error': 'Request must be JSON'}, 201

    return app


#Methods

def processCardImage(cardImagePath):
    cardImage = cv2.imread(cardImagePath)
    cardImageData = Cards.process_image(cardImage)
    cards = cardImageData['cards']

    scannedCards = []
    for card in cards:
        cardId = card['cardid']
        cardSetCode = card['setcode']
        cardName = card['name']
        cardGame = card['game']
        scryfallid = ""
        if 'scryfallid' in card:
            scryfallid = card['scryfallid']
        setName = ''
        if cardGame == 'yugioh':
            url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
            payload = {'id': cardId, 'tcgplayer_data': None}
            payload = '&'.join([k if v is None else f'{k}={v}' for k, v in payload.items()])
            resp = requests.get(url, params=payload)
            if resp.status_code == 200:
                ygoCard = resp.json()
                cardData = Cards.ygoprodeck_to_card_data(ygoCard, cardSetCode)
                if cardData is None:
                    scannedCards.append({'error': 'scanned set code does not match a valid printing. Card could be fake or try scanning again'})
                    continue
                
                scannedCards.append(cardData)
                continue
            scannedCards.append({'error': 'card not found'})
            continue
        if cardGame == 'mtg':
            if scryfallid == "":
                url = 'https://api.scryfall.com/cards/named'
                headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
                payload = {'exact' : cardName, 'set' : cardSetCode}
                resp = requests.get(url, params=payload, headers=headers)
            else:
                url = 'https://api.scryfall.com/cards/'
                headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
                url += scryfallid
                resp = requests.get(url, headers=headers)
            if resp.status_code == 200:
                scryfallCard = resp.json()
                cardData = Cards.scryfall_to_card_data(scryfallCard)
                if fuzz.ratio(cardData['collector_number'], cardId) < 90:
                   print('Fuzzy ratio is: ', fuzz.ratio(cardData['collector_number'], cardId))
                   scannedCards.append({'error': 'card search returned wrong card. please try scanning again'})
                   continue

                scannedCards.append(cardData)
                continue
            scannedCards.append({'error': 'card not found'})
            continue
        if cardGame == 'pokemon':
            apikey = os.getenv('POKEMON_API_KEY')
            url = 'https://api.pokemontcg.io/v2/cards/'
            headers = {'X-Api-Key' : apikey}
            url += cardId
            resp = requests.get(url, headers=headers)
            if resp.status_code == 200:
                cardData = resp.json()
                scannedCards.append(cardData)
                continue
            scannedCards.append({'error': 'card not found'})
            continue
    return scannedCards

#Main Run Method
if __name__ == '__main__':
    app = create_app()

    app.run()