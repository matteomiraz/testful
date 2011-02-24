#!/bin/bash
schemagen -cp bin/ testful.model.xml.XmlClass testful.model.xml.behavior.Behavior

mv schema1.xsd behavior.xsd 
mv schema2.xsd testful.xsd
