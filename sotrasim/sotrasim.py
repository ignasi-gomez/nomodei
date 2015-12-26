"""Module docstring.

Starts a social simulation populating the data in the visualization collections.

Ussage: python sceneParser.py path
Ex: python sotrasim.py
"""
import sys
import getopt
import re
from pymongo import MongoClient
import time
from random import randint


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
    simulate()

def simulate():
    print ("Simulating ...")

    #Manage DB collections
    client = MongoClient('localhost', 27017)
    db = client["nomodei"]
    activeModel = "_RO-mgBZiEeS0xuVGn_VuJw"
    data = db.sceneSimData.find_one({"modelId":activeModel}, {'_id': False})   
    nodes = data["nodes"]
    i = 0
    for node in nodes:
        if (randint(0,9) > 4):
            node["runningAgentsHistory"] = node["runningAgentsHistory"] + randint(1,3)
            if (randint(0,9) > 4):
                node["runningAgentsCurrent"] = node["runningAgentsCurrent"] + randint(1,3)
        nodes[i] = node
        i = i + 1
    data["nodes"] = nodes

    links = data["links"]
    i = 0
    for link in links:
        if (randint(0,9) > 4):
            link["transitions"] = link["transitions"] + randint(1,3)
        links[i] = link
        i = i + 1
    data["links"] = links

    db.sceneSimData.delete_one({"modelId": activeModel})
    db.sceneSimData.insert_one(data)

#Main Function wrapper
if __name__ == "__main__":
    main()