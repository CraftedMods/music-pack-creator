package craftedMods.utils.data;

import java.util.Map;

public interface PrimitiveProperties extends Map<Object, Object> {

	public boolean getBoolean(String key, boolean defaultVal);

	public void setBoolean(String key, boolean value);

	public byte getByte(String key, byte defaultVal);

	public void setByte(String key, byte value);

	public short getShort(String key, short defaultVal);

	public void setShort(String key, short value);

	public int getInteger(String key, int defaultVal);

	public void setInteger(String key, int value);

	public long getLong(String key, long defaultVal);

	public void setLong(String key, long value);

	public float getFloat(String key, float defaultVal);

	public void setFloat(String key, float value);

	public double getDouble(String key, double defaultVal);

	public void setCharacter(String key, char value);

	public char getCharacter(String key, char defaultValue);

	public void setDouble(String key, double value);

	public String getString(String key, String defaultVal);

	public void setString(String key, String value);

}
