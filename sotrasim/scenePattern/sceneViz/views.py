from django.shortcuts import render

from django.http import HttpResponse
import datetime
from django.utils import timezone
import time
from django.http import JsonResponse
from pymongo import MongoClient
import json



def index(request):	
	activeModel = "_RO-mgBZiEeS0xuVGn_VuJw"
	# Connect to database
	client = MongoClient('localhost', 27017)
	db = client["nomodei"]
	data = {}
	now = datetime.datetime.now().strftime('%H:%M:%S')

	#Retrieve configuration information
	sceneVizConfig = db.sceneVizConfig
	sceneVizConfigData = sceneVizConfig.find_one({}, {'_id': False})	
	data.update(sceneVizConfigData)
	sceneParticipantStateConfig = db.sceneParticipantStateConfig
	sceneNormStateConfigData = sceneParticipantStateConfig.find_one({"name":"Blocked"}, {'_id': False})
	data.update({"BlockedScene":sceneNormStateConfigData["color"]})
	sceneNormStateConfigData = sceneParticipantStateConfig.find_one({"name":"Active"}, {'_id': False})
	data.update({"ActiveScene":sceneNormStateConfigData["color"]})
	sceneNormStateConfigData = sceneParticipantStateConfig.find_one({"name":"Inactive"}, {'_id': False})
	data.update({"InactiveScene":sceneNormStateConfigData["color"]})

	#Retrieve scene data
	sceneSimData = db.sceneSimData
	sceneSimDataData = sceneSimData.find_one({"modelId":activeModel}, {'_id': False})	
	data.update({"nodes":sceneSimDataData["nodes"]})
	data.update({"links":sceneSimDataData["links"]})
	#print (data)

	#Dump to json to remove stupid unicode tag javascript does not understand
	data = json.dumps(data)

	#Pass data to javascript
	context = {}
	context['data'] = data
	return render(request, 'sceneViz/index.html', context)

def test_data(request):
    data = { "nodes":[	 {"id":0, "name":"Pelusso","stateColor":"#F5D0A9", "fixed": "true", "x":80, "y":350},
    					 {"id":1, "name":"Maldades","stateColor":"#9FF781", "fixed": "true", "x":230, "y":50},
    					 {"id":2, "name":now,"stateColor":"#CEECF5", "fixed": "true", "x":380, "y":350},
					 ],
			"links":  [{"source":1,"target":0, "fixed": "true", "transitions": 5},
					   {"source":2,"target":1, "fixed": "true", "transitions": 0}],
			"nodeRadius":50,
			"canvasWidth":1800,
			"canvasHeight":800,
			"linkDistance":300,
			"forceCharge": 6000,
			"linkSize":100,
			"buildLoops":1,
			"blockedPathColor": "#d62728",
			"activePathColor": "#17becf",
			"blockedPathShape":  "M 0,0 m -5,-5 L 5,-5 L 5,5 L -5,5 Z",
			"activePathShape": "M 0,0 m -1,-5 L 1,-5 L 1,5 L -1,5 Z",}      
    return JsonResponse(data, safe=False)