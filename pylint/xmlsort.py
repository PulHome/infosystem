import sys

from lxml import etree
from lxml.etree import XMLSyntaxError

if len(sys.argv) != 3:
    print("error in args")
    sys.exit()

inputXmlPath = sys.argv[1]
settingsXmlPath = sys.argv[2]

try:
    with open(settingsXmlPath) as settingsXml:
        settingsTree = etree.XML(settingsXml.read())
except FileNotFoundError:
    print("settings file not found")
    sys.exit()
except XMLSyntaxError:
    print("error in settings file")
    sys.exit()

try:
    arrayName = settingsTree.xpath("//array/@name")[0]
except IndexError:
    print("name attribute not found for tag array")
    sys.exit()

try:
    attributeName = settingsTree.xpath("//attributeName/@value")[0]
except IndexError:
    print("value attribute not found for tag attributeName")
    sys.exit()

try:
    with open(inputXmlPath) as inputXml:
        inputTree = etree.XML(inputXml.read())
except FileNotFoundError:
    print("input file not found")
    sys.exit()
except XMLSyntaxError:
    print("error in input file")
    sys.exit()

inputSearchTagExpression = "//{}".format(arrayName)
try:
    tagForSort = inputTree.xpath(inputSearchTagExpression)[0]
except IndexError:
    print(arrayName + " tag not found in input xml file")
    sys.exit()

tagForSort[:] = sorted(tagForSort, key=lambda child: (child.attrib[attributeName]))

with open('output.xml', 'wb') as xmlFile:
    etree.ElementTree(inputTree).write(xmlFile, encoding='utf-8',
                                       xml_declaration=True,
                                       pretty_print=True)
