# nomodei
Norm Monitoring on Dynamic Electronic Institutions project

## Installation
### Requirements
[Leinegen](https://github.com/technomancy/leiningen)

[MongoDB](https://www.mongodb.org/)

[ALIVE Framework](http://sourceforge.net/projects/ict-alive/)

[Neo4j](http://neo4j.com/download/)
### Configuration
Configure mongodb with user **nomodei** password **1981zemogi** and database **nomodei**
For instance:
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
Run the following command from console:
```javascript
lein run -m edu.upc.igomez.nomodei.viz.mockups.norm
```


## Version history
0.0: Project skeleton stub

0.1: Mockup Visualization for provenance event graph-lines. Using SWING

0.2: Mockup Visualization for norms. No event based evolution. Using SWING

0.3: Mockup Visualization for norms.Event based evolution. Simple use-case. Using persistent DB to keep track of graph. First data model able to be connected with better visualization components. Using SWING

