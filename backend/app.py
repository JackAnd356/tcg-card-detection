from ast import List
import bson
import json
import requests
import os
import bcrypt
import datetime
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
        if request.is_json:
            clientCardInfo = request.get_json()
            cards = processCardImage(clientCardInfo)
            retData = {'cards' : cards}
            return retData, 201
        return {'error': 'Request must be JSON'}, 201
    
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
            return {'success' : insertRes.acknowledged}, 201
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
                    oldPriceDate = datetime.strptime(res['pricedate']['$date'], "%Y-%m-%dT%H:%M:%S.%fZ")
                    oldPriceDate = oldPriceDate.replace(tzinfo=timezone.utc)
                    print((datetime.now(timezone.utc) - oldPriceDate).days)
                    if (datetime.now(timezone.utc) - oldPriceDate).days == 0:
                        continue
                    price = ""
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
                                    break
                    elif game == 'mtg':
                        url = 'https://api.scryfall.com/cards/'
                        headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
                        url += cardId
                        resp = requests.get(url, headers=headers)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            price = cardData['prices']['usd']
                    elif game == 'pokemon':
                        apikey = os.getenv('POKEMON_API_KEY')
                        url = 'https://api.pokemontcg.io/v2/cards/'
                        headers = {'X-Api-Key' : apikey}
                        url += cardId
                        resp = requests.get(url, headers=headers)
                        if resp.status_code == 200:
                            cardData = resp.json()
                            price = cardData['tcgplayer']['prices']['normal']['market']
                    payload = {'cardid' : cardId, 'setcode' : cardSetCode, 'game' : game, 'rarity' : res['rarity']}
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
            userData = userCollection.find_one({'userid' : userid})
            if userData == None:
                return {'error': 'Incorrect UserID', 'success' : 0}, 201
            payload = {'userid' : userid}
            userDel = userCollection.delete_one(payload)
            cardDel = cardCollection.delete_many(payload)
            return {'Message' : 'User removed', 'remUserCnt' : userDel.deleted_count, 'remCardCnt' : cardDel.deleted_count}, 201
        return {'error': 'Request must be JSON', 'success' : 0}, 201
    
    @app.post('/getUserCollection')
    def post_getUserCollection():
        if request.is_json:
            clientUserInfo = request.get_json()
            database = client['card_detection_info']
            collection = database['card_collection']
            results = collection.find({'userid' : clientUserInfo['userid']})
            results = parse_json(results)
            return results, 201
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
            payload = {'userid' : clientUserInfo['userid'], 'cardid' : clientUserInfo['cardid'], 'setcode' : clientUserInfo['setcode'], 'game' : clientUserInfo['game']}
            if 'rarity' in clientUserInfo:
                payload['rarity'] = clientUserInfo['rarity']
            else :
                payload['rarity'] = ''
            result = collection.find_one(payload)
            if result == None:
                payload['quantity'] = clientUserInfo['quantity']
                payload['price'] = clientUserInfo['price']
                payload['pricedate'] = datetime.now(tz=datetime.timezone.utc)
                insertRes = collection.insert_one(payload)
                return {'success' : insertRes.acknowledged}, 201
            else :
                currCount = result['quantity']
                currCount += clientUserInfo['quantity']
                updateOp = { '$set' : 
                                { 'quantity' : currCount,
                                  'price' : clientUserInfo['price'],
                                  'pricedate' : datetime.now(tz=datetime.timezone.utc)}
                            }
                upRes = collection.update_one(payload, updateOp)
                return {'Message' : 'Successful Update!', 'count' : upRes.modified_count}, 201
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
                if subCol in subColArr:
                    return {'error': 'Already a part of subcollection'}, 201
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
            collection.delete_one({'subcollectionid' : subcollectionid})
            return {'success' : 1}, 201
        return {'error': 'Request must be JSON'}, 201

    return app


#Methods

def processCardImage(cardImageData):
    #Replace enclosed with actual card image parsing
    cards = cardImageData['cards']
    #End dummy code
    scannedCards = []
    for card in cards:
        cardId = card['cardid']
        cardSetCode = card['setcode']
        cardName = card['name']
        cardGame = card['game']
        setName = ''
        if cardGame == 'yugioh':
            url = 'https://db.ygoprodeck.com/api/v7/cardinfo.php'
            payload = {'id': cardId, 'tcgplayer_data': None}
            payload = '&'.join([k if v is None else f'{k}={v}' for k, v in payload.items()])
            resp = requests.get(url, params=payload)
            if resp.status_code == 200:
                cardData = resp.json()
                if fuzz.ratio(cardData['data'][0]['name'], cardName) < 90:
                    print('Fuzzy ratio is: ', fuzz.ratio(cardData['data'][0]['name'], cardName))
                    scannedCards.append({'error': 'card search returned wrong card. please try scanning again'})
                    continue
                print('Fuzzy ratio is: ', fuzz.ratio(cardData['data'][0]['name'], cardName))
                for setData in cardData['data'][0]['card_sets']:
                    if fuzz.ratio(setData['set_code'], cardSetCode) > 95:
                       setName = setData['set_name']
                       continue
                if setName == '':
                    scannedCards.append({'error': 'scanned set code does not match a valid printing. Card could be fake or try scanning again'})
                    continue
                scannedCards.append(cardData)
                continue
            scannedCards.append({'error': 'card not found'})
            continue
        if cardGame == 'mtg':
            url = 'https://api.scryfall.com/cards/named'
            headers = {'User-Agent' : 'TCG Card Detection App 0.1', 'Accept' : '*/*'}
            payload = {'exact' : cardName, 'set' : cardSetCode}
            resp = requests.get(url, params=payload, headers=headers)
            if resp.status_code == 200:
                cardData = resp.json()
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