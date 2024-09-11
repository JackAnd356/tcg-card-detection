from flask import Flask, request, jsonify
import requests
from fuzzywuzzy import fuzz

app = Flask(__name__)

#Dummy Test Data
testCardImageData = [{'imageid': 1, 'name': 'Dark Magician', 'cardid': '46986414', 'setcode': 'LOB-EN005'},
        {'imageid': 2, 'name': 'Blue-Eyes White Dragon', 'cardid': '89631139', 'setcode': 'LOB-EN001'},
        {'imageid': 3, 'name': 'Red-Eyes Black Dragon', 'cardid': '74677422', 'setcode': 'LOB-EN070'}]

testCardContentData = [{'cardID': '46986414', 'card-name': 'Dark Magician', 'card-text': 'The ultimate wizard in terms of attack and defense.', 'atk': 2500, 'def': 2100},
                       {'cardID': '89631139', 'card-name': 'Blue-Eyes White Dragon', 'card-text': 'This legendary dragon is a powerful engine of destruction. Virtualy invincible, very few have faced this awesome creature and lived to tell the tale.', 'atk': 3000, 'def': 2500},
                       {'cardID': '74677422', 'card-name': 'Red-Eyes Black Dragon', 'card-text': 'A ferocious dragon with a deadly attack', 'atk': 2400, 'def': 2000}]

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

#Methods

def processCardImage(cardImageData):
    #Replace enclosed with actual card image parsing
    cardId = cardImageData['cardid']
    cardSetCode = cardImageData['setcode']
    cardName = cardImageData['name']
    setName = ""
    #End dummy code
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
