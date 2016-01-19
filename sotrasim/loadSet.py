"""Module docstring.

Starts a social simulation populating the data in the visualization collections.

Ussage: python sceneParser.py path
Ex: python sotrasim.py
"""
import sys
import getopt
import re
import json
from pprint import pprint
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
    load()

def load():
    print ("Loading ...")

    #Manage DB collections
    client = MongoClient('localhost', 27017)
    db = client["nomodei"]
    with open("mongoExport/sceneSimData.json") as data_file:    
        data = json.load(data_file)
    #pprint(data)
    activeModel = data["modelId"]
    db.sceneSimData.delete_one({"modelId": activeModel})
    db.sceneSimData.insert_one(data)
    print ("Data imported. Visualization ready")
#Main Function wrapper
if __name__ == "__main__":
    main()