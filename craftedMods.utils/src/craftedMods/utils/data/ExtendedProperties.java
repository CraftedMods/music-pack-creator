package craftedMods.utils.data;

import java.util.Properties;

public class ExtendedProperties extends Properties implements PrimitiveProperties {

	private static final long serialVersionUID = -4781890307859454177L;

	public boolean getBoolean(String key, boolean defaultVal) {
		return Boolean.parseBoolean(this.getProperty(key, Boolean.toString(defaultVal)));
	}

	public void setBoolean(String key, boolean value) {
		this.setProperty(key, Boolean.toString(value));
	}

	public byte getByte(String key, byte defaultVal) {
		return Byte.parseByte(this.getProperty(key, Byte.toString(defaultVal)));
	}

	public void setByte(String key, byte value) {
		this.setProperty(key, Byte.toString(value));
	}

	public short getShort(String key, short defaultVal) {
		return Short.parseShort(this.getProperty(key, Short.toString(defaultVal)));
	}

	public void setShort(String key, short value) {
		this.setProperty(key, Short.toString(value));
	}

	public int getInteger(String key, int defaultVal) {
		return Integer.parseInt(this.getProperty(key, Integer.toString(defaultVal)));
	}

	public void setInteger(String key, int value) {
		this.setProperty(key, Integer.toString(value));
	}

	public long getLong(String key, long defaultVal) {
		return Long.parseLong(this.getProperty(key, Long.toString(defaultVal)));
	}

	public void setLong(String key, long value) {
		this.setProperty(key, Long.toString(value));
	}

	public float getFloat(String key, float defaultVal) {
		return Float.parseFloat(this.getProperty(key, Float.toString(defaultVal)));
	}

	public void setFloat(String key, float value) {
		this.setProperty(key, Float.toString(value));
	}

	public double getDouble(String key, double defaultVal) {
		return Double.parseDouble(this.getProperty(key, Double.toString(defaultVal)));
	}

	public void setCharacter(String key, char value) {
		this.setProperty(key, Character.toString(value));
	}

	public char getCharacter(String key, char defaultValue) {
		return this.getProperty(key, Character.toString(defaultValue)).charAt(0);
	}

	public void setDouble(String key, double value) {
		this.setProperty(key, Double.toString(value));
	}

	public String getString(String key, String defaultVal) {
		return this.getProperty(key, defaultVal);
	}

	public void setString(String key, String value) {
		this.setProperty(key, value);
	}
}
