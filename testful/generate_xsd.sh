#!/bin/bash
schemagen -cp bin/classes testful.model.xml.XmlClass testful.model.xml.behavior.Behavior

mv schema1.xsd testful.xsd
mv schema2.xsd behavior.xsd 
