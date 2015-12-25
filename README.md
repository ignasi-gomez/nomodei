# nomodei
Norm Monitoring on Dynamic Electronic Institutions project

# Components
The root project contains nomodei for norm monitoring
The sotrasim folder contains a project branch for visualisations on social simulations for the tragedy of the commons

# sotrasim
## scenePattern
Proof of concept tests for the scene pattern visualizations

## testing
Running the following code:
```javascript
cd nomodei/sotrasim/scenePattern

python manage.py runserver
```

Open: 
```javascript
http://localhost:8000/sceneViz/
```

Initialize the mongoDB Collection with visualization parameters.
Please notice, you can adapt visualization parameters to your likings:
```
db.sceneVizConfig.drop();
db.sceneVizConfig.insert({"nodeRadius":50,
						  "canvasWidth":1800,
						  "canvasHeight":800,
						  "linkDistance":300,
						  "forceCharge": 6000,
						  "linkSize":100,
						  "buildLoops":1,
						  "blockedPathColor": "#d62728",
						  "activePathColor": "#17becf",
						  "blockedPathShape":  "M 0,0 m -5,-5 L 5,-5 L 5,5 L -5,5 Z",
						  "activePathShape": "M 0,0 m -1,-5 L 1,-5 L 1,5 L -1,5 Z"});
db.sceneVizConfig.find("").pretty();		

db.sceneNormStateConfig.drop();
db.sceneNormStateConfig.insert({"_id":1,
						  "id":1,
						  "name":"Violated",
						  "description":"Scene contains active violated norms",
						  "color":"#F5D0A9"});
db.sceneNormStateConfig.insert({"_id":2,
						  "id":2,
						  "name":"Active",
						  "description":"Scene contains active non-violated norms",
						  "color":"#9FF781"});
db.sceneNormStateConfig.insert({"_id":3,
						  "id":3,
						  "name":"Inactive",
						  "description":"Scene does not contain active norms",
						  "color":"#CEECF5"});						  						  
db.sceneNormStateConfig.find("").pretty();		

db.sceneParticipantStateConfig.drop();
db.sceneParticipantStateConfig.insert({"_id":1,
						  "id":1,
						  "name":"Blocked",
						  "description":"Scene has never contained participating agents",
						  "color":"#F5D0A9"});
db.sceneParticipantStateConfig.insert({"_id":2,
						  "id":2,
						  "name":"Active",
						  "description":"Scene contains participating agents now",
						  "color":"#9FF781"});
db.sceneParticipantStateConfig.insert({"_id":3,
						  "id":3,
						  "name":"Inactive",
						  "description":"Scene does not contain participating agents now",
						  "color":"#CEECF5"});						  						  
db.sceneParticipantStateConfig.find("").pretty();	

db.sceneSimData.drop();	
db.sceneSimData.insert({"_id":-666,
						  "modelId":-666,
						  "name":"Testing ALIVE model",
						  "nodes":[	 
						  	{"id":0, "name":"Pelusso", "fixed": "true", "x":80, "y":350, "runningAgentsCurrent" : 0, "runningAgentsHistory" : 0, "normsActive" : 0, "normsViolated" : 0},
    					 	{"id":1, "name":"Maldades", "fixed": "true", "x":230, "y":50, "runningAgentsCurrent" : 5, "runningAgentsHistory" : 5, "normsActive" : 0, "normsViolated" : 0},
    						{"id":2, "name":"now", "fixed": "true", "x":380, "y":350, "runningAgentsCurrent" : 0, "runningAgentsHistory" : 5, "normsActive" : 0, "normsViolated" : 0}],
    					  "links":  [
    					  	{"source":1,"target":0, "fixed": "true", "transitions": 0},
					   		{"source":2,"target":1, "fixed": "true", "transitions": 5}]
    					});
db.sceneSimData.find("").pretty();	 

db.parseNode.find("").pretty();
db.parseLocation.find("").pretty();
db.parseLink.find("").pretty();

```
Import a more complex scene by running:
```
 cd sotrasim
 python sceneParser.py Test.opera
```
##TBD
- Improve Visualization refresh method
- Include list of scenes to visualize and choose one
- Populate 'sceneSimData' with data from simulator
- Discuss how to represent norms inside scenes

# nomodei project
## Installation
### Requirements
[Leinegen](https://github.com/technomancy/leiningen)

[MongoDB](https://www.mongodb.org/)

[ALIVE Framework](http://sourceforge.net/projects/ict-alive/)

[Neo4j](http://neo4j.com/download/)
### Configuration
Configure mongodb with user **nomodei** password **1981zemogi** and database **nomodei**
Running the following code:
```javascript
use nomodei;
db.bunny.insert({"name":"Pelusso Maldades”});
db.addUser( { "user" : "nomodei",
                 "pwd": "1981zemogi",
                  roles: ["admin"]})
```

Populate the collection time-line-type via by running the following code on the **nomodei** database:
 ```javascript
db.getCollection("time-line-type").insert({"id":9999, "type":"Other"});
db.getCollection("time-line-type").insert({"id":8999, "type":"Norm State"});
db.getCollection("time-line-type").insert({"id":7999, "type":"Constitutive Entailment"});
db.getCollection("time-line-type").insert({"id":6999, "type":"Norm Operation"});

db.getCollection("time-line-type").insert({"id":6900, "type":"Prospective Promulgation"});
db.getCollection("time-line-type").insert({"id":6800, "type":"Retroactive Promulgation"});
db.getCollection("time-line-type").insert({"id":6700, "type":"Abrogation"});
db.getCollection("time-line-type").insert({"id":6600, "type":"Annulment"});

db.getCollection("time-line-type").insert({"id”:5999, "type":"Event”});

db.getCollection("time-line-type").insert({"id":0, "type":"Test"});

db.getCollection("time-line-type”).find()
```

Create indexes  by running the following code on the **nomodei** database:
 ```javascript
db.getCollection("norm-instance").createIndex( { "norm-instance-id": 1 } )
db.getCollection("norm-instance-graph-node").createIndex( { "norm-instance-id": 1 } )
db.getCollection("time-line-mock").createIndex( { "time": 1 } )
```
## Usage
### Generic test
lein clean

lein ring server

Initial page shows testing information.
### Mockup Visualization for provenance event graph-lines
Run the following command from console:
```javascript
lein run -m edu.upc.igomez.nomodei.viz.mockups.timeline
```

### Mockup Visualization for norm evolution
Open four terminals.
Run the following commands from terminal1 and wait the command to finish:
```javascript
lein run -m edu.upc.igomez.nomodei.viz.norm.parser
```
Run the following commands from terminal2 and wait until the full norm is visualized:
```javascript
lein run -m edu.upc.igomez.nomodei.viz.norm.drawer -6665
```
Run the following commands from terminal3 and wait until text output starts to appear:
```javascript
lein run -m edu.upc.igomez.nomodei.viz.norm.watcher -6665
```
Run the following commands from terminal4 and wait until text output starts to appear:
```javascript
lein run -m edu.upc.igomez.nomodei.viz.norm.mock
```
You should see events injected on terminal4. As events are injected the visualization evolves. You can see events being captured and analysed on terminal3. Terminal2 will react to event analysis, effectively updating the visualization.

For a slight performance improvement, if multiple tests are run in a row, clean the time-line-mock connection by running the following code on the **nomodei** database:
```javascript
db.getCollection("time-line-mock").remove()
```

## Version history
0.0: Project skeleton stub

0.1: Mockup Visualization for provenance event graph-lines. Using SWING

0.2: Mockup Visualization for norms. No event based evolution. Using SWING

0.3: Mockup Visualization for norms.Event based evolution. Simple use-case. Using persistent DB to keep track of graph. First data model able to be connected with better visualization components. Using SWING

0.4: Decoupling norm instance population from visualization. Improving use of constants from properties file.

0.5: Decoupling visualization from mockups and control loops. Done for norm visualizations. Improving data model. Improving visualization. Improving tests.