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