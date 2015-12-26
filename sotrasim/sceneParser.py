"""Module docstring.

This parses an ALIVE model obtaining information from the scene pattern diagram.

Ussage: python sceneParser.py path
Ex: python sceneParser.py Test.opera
"""
import sys
import getopt
import re
from pymongo import MongoClient
import time

def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], "h", ["help"])
    except getopt.error, msg:
        print msg
        print "for help use --help"
        sys.exit(2)
    # process options
    for o, a in opts:
        if o in ("-h", "--help"):
            print __doc__
            sys.exit(0)
    # process arguments
    for arg in args:
        parse(arg)

def parse(file):
    print ("Parsing the ALIVE model in file '" + file + "'")
    #Initialize
    insideLocation = False
    id = 0
    modelName = ""
    modelId = ""

    #Manage DB collections
    client = MongoClient('localhost', 27017)
    db = client["nomodei"]
    db.parseNode.drop()
    db.parseLocation.drop()
    db.parseLink.drop()

    #Open file
    f = open(file, 'r')

    #Parse file. The process uses some aux db collections we dropped before
    for line in f:
        if (isName(line)):
            modelName = re.search('Name="(.+?)"', line).group(1)
            modelId = re.search('xmi:id="(.+?)"', line).group(1)
        if (isScene(line)):
            sceneId = re.search('xmi:id="(.+?)"', line).group(1)
            sceneName = re.search('sceneID="(.+?)"', line).group(1)
            db.parseNode.insert_one({"id":id, 
                          "name":sceneName, 
                          "sceneId" : sceneId})
            id = id + 1
        if (isLocation(line) and not insideLocation):
            sceneId = re.search('element="(.+?)"', line).group(1)
            insideLocation = True
        if (insideLocation and isSubLocation(line)):
            sceneX = re.search('x="(.+?)"', line).group(1)
            sceneY = re.search('y="(.+?)"', line).group(1)
            db.parseLocation.insert_one({
                          "sceneId" : sceneId,
                          "x":sceneX, 
                          "y":sceneY})
            insideLocation = False
        if (isLink(line)):
            source = re.search('from="(.+?)"', line).group(1)
            target = re.search('to="(.+?)"', line).group(1)
            db.parseLink.insert_one({
                         "sourceId":source,
                         "targetId":target})
    f.close()

    #Generate data
    nodes = []
    links = []
    x= 80
    y = 50
    for node in db.parseNode.find({}):
        sceneId = node["sceneId"]
        position = db.parseLocation.find_one({'sceneId' : sceneId}, {'_id': False})
        link = db.parseLink.find_one({'sourceId' : sceneId}, {'_id': False})
        linkHop = link['targetId']
        link2 = db.parseLink.find_one({'sourceId' : linkHop}, {'_id': False})
        destNode =  db.parseNode.find_one({'sceneId' : link2['targetId']}, {'_id': False})
        nodes.append({"id": node["id"], 
                      "name": node["name"],
                      "sceneId" : node["sceneId"],
                      "fixed": "true", 
                      "x": x, 
                      "y": y, 
                      "runningAgentsCurrent" : 0, 
                      "runningAgentsHistory" : 0, 
                      "normsActive" : 0, 
                      "normsViolated" : 0})
        x = x + 80
        y = y + 100
        if (y > 600):
            y = 50
        if (not destNode is None):
            links.append({"source":node["id"],
                          "target":destNode["id"], 
                          "fixed": "true", 
                          "transitions": 0})

    #Insert data into DB
    db.sceneSimData.delete_one({"modelId": modelId})
    db.sceneSimData.insert_one({
                          "modelId":modelId,
                          "name":modelName,
                          "nodes":nodes,
                          "links":links})
#Finds Model Name
def isName(line):
    str1 = "<net.sf.ictalive.operetta:OperAModel"
    return ( (str1 in line))

#Detects a scene
def isScene(line):
    str1 = "<scenes"
    str2 = 'xmi:type="net.sf.ictalive.operetta:Scene"'
    str3 = 'type="start"'
    str4 = 'type="end"'
    return ( (str1 in line) and (str2 in line) 
            and (not str3 in line) and (not str4 in line))

#Detects a scene location in diagram
def isLocation(line):
    str1 = "<children"
    str2 = 'xmi:type="notation:Shape"'
    return ( (str1 in line) and (str2 in line))

#Finds positions of scene location in diagram
def isSubLocation(line):
    str1 = "<layoutConstraint"
    str2 = 'xmi:type="notation:Bounds"'
    str3 = 'x="'
    str4 = 'y="'
    return ( (str1 in line) and (str2 in line) 
            and (str3 in line) and (str4 in line))

#Detects links between scenes
def isLink(line):
    str1 = "<arcs"
    str2 = 'xmi:type="net.sf.ictalive.operetta:SceneToTransitionArc'
    str3 = 'xmi:type="net.sf.ictalive.operetta:TransitionToSceneArc'
    str4 = 'from="'
    str5 = 'to="'
    return ( (str1 in line) and (str4 in line) and (str5 in line)
            and ( (str2 in line) or (str3 in line) ))

#Main Function wrapper
if __name__ == "__main__":
    main()