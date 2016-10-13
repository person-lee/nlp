package com.lbc.nlp_domain;

public class LevelCode {

	static final Float similarA = null;
	static final Float similarB = null;
	static final Float similarC = 0.4f;
	static final Float similarD = 0.7f;
	static final Float similarE = 0.9f;
	static final Float similarSharp = 0.7f; // 最后为#

	String code;
	byte a, b, c, d, e, flag;

	public LevelCode(String code) {
		this.code = code;
		a = (byte) code.charAt(0);
		b = (byte) code.charAt(1);
		c = Integer.valueOf(code.substring(2, 4)).byteValue();
		d = (byte) code.charAt(4);
		e = Integer.valueOf(code.substring(5, 7)).byteValue();
		flag = (byte) code.charAt(7);
	}

	/**
	 * 比较两个词的相似度
	 * 
	 * @param another
	 * @return
	 */
	public Float getSimilar(LevelCode another) {
		if (a != another.a) {
			return null;
		} else if (b != another.b) {
			return similarA;
		} else if (c != another.c) {
			return similarB;
		} else if (d != another.d) {
			return similarC;
		} else if (e != another.e) {
			return similarD;
		} else if (flag == '#' || another.flag == '#') {
			return similarSharp;
		} else {
			return similarE;
		}
	}

	public String getCode() {
		return code;
	}

	public byte getA() {
		return a;
	}

	public byte getB() {
		return b;
	}

	public byte getC() {
		return c;
	}

	public byte getD() {
		return d;
	}

	public byte getE() {
		return e;
	}

	public byte getFlag() {
		return flag;
	}
}
