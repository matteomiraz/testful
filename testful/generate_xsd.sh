#!/bin/bash
schemagen -cp bin/classes/ testful.model.xml.XmlClass
mv schema1.xsd testful.xsd
