package test.regression;

/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Dummy class with getter and setters for primitive and complex types. 
 * @author matteo
 */
public class Dummy {

	private boolean boolean_;
	private char char_;
	private byte byte_;
	private short short_;
	private int int_;
	private long long_;
	private float float_;
	private double double_;

	private String string;
	private Dummy dummy;

	public Dummy() {
	}

	public boolean isBoolean() {
		return boolean_;
	}

	public void setBoolean(boolean boolean_) {
		this.boolean_ = boolean_;
	}

	public char getChar() {
		return char_;
	}

	public void setChar(char char_) {
		this.char_ = char_;
	}

	public byte getByte() {
		return byte_;
	}

	public void setByte(byte byte_) {
		this.byte_ = byte_;
	}

	public short getShort() {
		return short_;
	}

	public void setShort(short short_) {
		this.short_ = short_;
	}

	public int getInt() {
		return int_;
	}

	public void setInt(int int_) {
		this.int_ = int_;
	}

	public long getLong() {
		return long_;
	}

	public void setLong(long long_) {
		this.long_ = long_;
	}

	public float getFloat() {
		return float_;
	}

	public void setFloat(float float_) {
		this.float_ = float_;
	}

	public double getDouble() {
		return double_;
	}

	public void setDouble(double double_) {
		this.double_ = double_;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Dummy getDummy() {
		return dummy;
	}

	public void setDummy(Dummy dummy) {
		this.dummy = dummy;
	}
}
