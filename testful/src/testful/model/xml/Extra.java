package testful.model.xml;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import testful.model.xml.behavior.Behavior;

@XmlSeeAlso( { Behavior.class })
@XmlType(namespace = "http://home.dei.polimi.it/miraz/testful", name = "extra")
public abstract class Extra {}
