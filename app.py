from flask import Flask, request, jsonify

app = Flask(__name__)

#Dummy Test Data
testCardImageData = [{'imageid': 1, 'data': 'Dark Magician'},
        {'imageid': 2, 'data': 'Blue-Eyes White Dragon'},
        {'imageid': 3, 'data': 'Red-Eyes Black Dragon'}]

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
            return {"error": "Card Not Found"}, 415
        return card, 201
    return {"error": "Request must be JSON"}, 415

#Methods

def processCardImage(cardImageData):
    #Replace enclosed with actual card image parsing
    cardName=cardImageData['data']
    #End dummy code
    #ADD YGOPRODECK API LOOKUP HERE
    for card in testCardContentData:
        if card['card-name'] == cardName:
            return card
    #End dummy lookup
    return {'error': 'card not found'}    

