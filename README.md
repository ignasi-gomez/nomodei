# nomodei
Norm Monitoring on Dynamic Electronic Institutions project

## Installation
### Requirements
[Leinegen](https://github.com/technomancy/leiningen)

[MongoDB](https://www.mongodb.org/)

[ALIVE Framework](http://sourceforge.net/projects/ict-alive/)
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
## Usage
lein clean

lein ring server

Initial page shows testing information.

## Version history
0.0: Project skeleton stub

