/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.teiid.client.batch;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamConstants;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.komodo.spi.runtime.version.TeiidVersion;
import org.teiid.client.BatchSerializer;
import org.teiid.client.ResizingArrayList;
import org.teiid.core.types.ArrayImpl;
import org.teiid.core.types.BinaryTypeImpl;
import org.teiid.core.types.BlobType;
import org.teiid.core.types.ClobType;
import org.teiid.core.types.DefaultDataTypeManager;
import org.teiid.core.types.DefaultDataTypeManager.DefaultDataTypes;
import org.teiid.core.types.XMLType;
import org.teiid.runtime.client.Messages;



/**
 *
 *
 * <ul>
 * <li>version 0: starts with 7.1 and uses simple serialization too broadly
 * <li>version 1: starts with 8.0 uses better string, blob, clob, xml, etc.
 *   add varbinary support.
 *   however was possibly silently truncating date/time values that were
 *   outside of jdbc allowed values
 * <li>version 2: starts with 8.2 and adds better array serialization and
 *   uses a safer date/time serialization
 * </ul>
 */
public class Batch3Serializer extends BatchSerializer {

    static final byte CURRENT_VERSION = (byte)3;

    private final ColumnSerializer defaultSerializer = new ColumnSerializer();

    private final Map<String, ColumnSerializer[]> serializers = new HashMap<String, ColumnSerializer[]>(128);

    /**
     * @param teiidVersion
     */
    public Batch3Serializer(TeiidVersion teiidVersion) {
        super(teiidVersion);

        serializers.put(DefaultDataTypeManager.DefaultDataTypes.BIG_DECIMAL.getId(),   new ColumnSerializer[] {new BigDecimalColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.BIG_INTEGER.getId(),   new ColumnSerializer[] {new BigIntegerColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.getId(),       new ColumnSerializer[] {new BooleanColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.BYTE.getId(),          new ColumnSerializer[] {new ByteColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.CHAR.getId(),          new ColumnSerializer[] {new CharColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.DATE.getId(),          new ColumnSerializer[] {new DateColumnSerializer(), new DateColumnSerializer1(), new DateColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.DOUBLE.getId(),        new ColumnSerializer[] {new DoubleColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.FLOAT.getId(),         new ColumnSerializer[] {new FloatColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.INTEGER.getId(),       new ColumnSerializer[] {new IntColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.LONG.getId(),          new ColumnSerializer[] {new LongColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.SHORT.getId(),         new ColumnSerializer[] {new ShortColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.TIME.getId(),          new ColumnSerializer[] {new TimeColumnSerializer(), new TimeColumnSerializer1(), new TimeColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.TIMESTAMP.getId(),     new ColumnSerializer[] {new TimestampColumnSerializer()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.STRING.getId(),     	new ColumnSerializer[] {defaultSerializer, new StringColumnSerializer1(), new StringColumnSerializer1(), new StringColumnSerializer3()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.CLOB.getId(),  	   	new ColumnSerializer[] {defaultSerializer, new ClobColumnSerializer1()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.BLOB.getId(),     		new ColumnSerializer[] {defaultSerializer, new BlobColumnSerializer1()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.XML.getId(),     		new ColumnSerializer[] {defaultSerializer, new XmlColumnSerializer1()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.NULL.getId(),     		new ColumnSerializer[] {defaultSerializer, new NullColumnSerializer1()});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId(),     	new ColumnSerializer[] {defaultSerializer, new ObjectColumnSerializer(DefaultDataTypeManager.DefaultDataTypes.VARBINARY.ordinal(), (byte)1)});
        serializers.put(DefaultDataTypeManager.DefaultDataTypes.VARBINARY.getId(),    	new ColumnSerializer[] {new BinaryColumnSerializer(), new BinaryColumnSerializer1()});
    }

    private ColumnSerializer arrayColumnSerializer = new ColumnSerializer() {

    	@Override
    	protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache)
    			throws IOException {
    		try {
				super.writeObject(out, ((java.sql.Array)obj).getArray(), cache);
			} catch (SQLException e) {
				throw new IOException(e);
			}
    	}

    	@Override
    	protected Object readObject(ObjectInput in, List<Object> cache) throws IOException,
    			ClassNotFoundException {
    		return new ArrayImpl(getTeiidVersion(), (Object[]) in.readObject());
    	}

    };

    private ColumnSerializer arrayColumnSerialier2 = new ArrayColumnSerializer2(new ObjectColumnSerializer(DefaultDataTypeManager.DefaultDataTypes.VARBINARY.ordinal(), (byte)2));

	private class ArrayColumnSerializer2 extends ColumnSerializer {

		ObjectColumnSerializer ser;

		public ArrayColumnSerializer2(ObjectColumnSerializer ser) {
			this.ser = ser;
		}

		@Override
    	protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache)
    			throws IOException {
			Object[] values = null;
    		try {
    			values = (Object[]) ((Array)obj).getArray();
    		} catch (SQLException e) {
    			out.writeInt(-1);
    			return;
    		}
			out.writeInt(values.length);
            DefaultDataTypes dataType = getDataTypeManager().getDataType(values.getClass().getComponentType());
            int code = dataType.ordinal();
    		out.writeByte((byte)code);
    		for (int i = 0; i < values.length;) {
    			writeIsNullData(out, i, values);
    			int end = Math.min(values.length, i+8);
    			for (; i < end; i++) {
    				if (values[i] != null) {
						this.ser.writeObject(out, values[i], code, cache);
					}
    			}
    		}
    		out.writeBoolean((obj instanceof ArrayImpl && ((ArrayImpl)obj).isZeroBased()));
    	}

		@Override
    	protected Object readObject(ObjectInput in, List<Object> cache) throws IOException,
    			ClassNotFoundException {
    		int length = in.readInt();
    		if (length == -1) {
        		return new ArrayImpl(getTeiidVersion(), null);
    		}
    		int code = in.readByte();
            DefaultDataTypes dataType = DefaultDataTypeManager.DefaultDataTypes.valueOf(getTeiidVersion(), code);
    		Object[] vals = (Object[])java.lang.reflect.Array.newInstance(dataType.getTypeClass(), length);
    		for (int i = 0; i < length;) {
    			byte b = in.readByte();
    			int end = Math.min(length, i+8);
    			for (; i < end; i++) {
					if (!isNullObject(i, b)) {
						vals[i] = this.ser.readObject(in, cache, code);
					}
    			}
    		}
    		ArrayImpl result = new ArrayImpl(getTeiidVersion(), vals);
    		result.setZeroBased(in.readBoolean());
    		return result;
    	}
		
		@Override
		public boolean usesCache(byte version) {
			return version >= 3;
		}
	}

	private class BinaryColumnSerializer1 extends ColumnSerializer {
		@Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache)
				throws IOException {
			byte[] bytes = ((BinaryTypeImpl)obj).getBytes();
			out.writeInt(bytes.length); //in theory this could be a short, but we're not strictly enforcing the length
			out.write(bytes);
		}

		@Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException,
				ClassNotFoundException {
			int length = in.readInt();
			byte[] bytes = new byte[length];
			in.readFully(bytes);
			return new BinaryTypeImpl(bytes);
		}
	}

    private class BinaryColumnSerializer extends ColumnSerializer {
		@Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache)
				throws IOException {
			//uses object serialization for compatibility with legacy clients
			super.writeObject(out, ((BinaryTypeImpl)obj).getBytesDirect(), cache);
		}

		@Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException,
				ClassNotFoundException {
			//won't actually be used
			byte[] bytes = (byte[])super.readObject(in, cache);
			return new BinaryTypeImpl(bytes);
		}
	}

	private class ObjectColumnSerializer extends ColumnSerializer {

		int highestKnownCode;
		byte version;

    	public ObjectColumnSerializer(int highestKnownCode, byte version) {
    		this.highestKnownCode = highestKnownCode;
    		this.version = version;
		}

		@Override
    	protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache)
    			throws IOException {
		    DefaultDataTypes dataType =  getDataTypeManager().getDataType(obj.getClass());
    		int code = dataType.ordinal();
    		out.writeByte((byte)code);
    		writeObject(out, obj, code, cache);
    	}

		protected void writeObject(ObjectOutput out, Object obj, int code, Map<Object, Integer> cache)
				throws IOException {
			if (code == DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.ordinal()) {
    			if (Boolean.TRUE.equals(obj)) {
    				out.write((byte)1);
    			} else {
    				out.write((byte)0);
    			}
    		} else if (code <= this.highestKnownCode && code != DefaultDataTypeManager.DefaultDataTypes.OBJECT.ordinal()) {
    		    DefaultDataTypes dataType = DefaultDataTypeManager.DefaultDataTypes.valueOf(getTeiidVersion(), code);
    			ColumnSerializer s = getSerializer(dataType.getId(), this.version);
    			s.writeObject(out, obj, cache);
    		} else {
    			super.writeObject(out, obj, cache);
    		}
    	}

    	@Override
    	protected Object readObject(ObjectInput in, List<Object> cache) throws IOException,
    			ClassNotFoundException {
    		int code = in.readByte();
    		return readObject(in, cache, code);
    	}

		private Object readObject(ObjectInput in, List<Object> cache, int code) throws IOException,
				ClassNotFoundException {
			if (code == DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.ordinal()) {
    			if (in.readByte() == (byte)0) {
    				return Boolean.FALSE;
    			}
    			return Boolean.TRUE;
    		}
    		if (code != DefaultDataTypeManager.DefaultDataTypes.OBJECT.ordinal()) {
    		    DefaultDataTypes dataType = DefaultDataTypeManager.DefaultDataTypes.valueOf(getTeiidVersion(), code);
    			ColumnSerializer s = getSerializer(dataType.getId(), this.version);
    			return s.readObject(in, cache);
    		}
			return super.readObject(in, cache);
    	}
		
		@Override
		public boolean usesCache(byte version) {
			return version >= 3;
		}

    }

    private int MAX_UTF = 0xFFFF/3; //this is greater than the expected max length of Teiid Strings

    private class StringColumnSerializer1 extends ColumnSerializer {
    	@Override
    	protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
    		String str = (String)obj;
        	if (str.length() <= MAX_UTF) {
        		//skip object serialization if we have a short string
        	    out.writeByte(ObjectStreamConstants.TC_STRING);
        	    out.writeUTF(str);
        	} else {
        		out.writeByte(ObjectStreamConstants.TC_LONGSTRING);
        		out.writeObject(obj);
        	}
        }

    	@Override
    	protected Object readObject(ObjectInput in, List<Object> cache) throws IOException,
    			ClassNotFoundException {
    		if (in.readByte() == ObjectStreamConstants.TC_STRING) {
    			return in.readUTF();
    		}
    		return super.readObject(in, cache);
    	}
    	
    }
    
    private class StringColumnSerializer3 extends StringColumnSerializer1 {
    	private int MAX_INLINE_STRING_LENGTH = 5;
    	private byte REPEATED_STRING = 0;
    	@Override
    	protected Object readObject(ObjectInput in, List<Object> cache)
    			throws IOException, ClassNotFoundException {
    		byte b = in.readByte();
    		if (b == ObjectStreamConstants.TC_STRING) {
    			String val = in.readUTF();
    			if (val.length() > MAX_INLINE_STRING_LENGTH) {
    				cache.add(val);
    			}
    			return val;
    		}
    		if (b == REPEATED_STRING) {
    			Integer val = in.readInt();
    			return cache.get(val);
    		}
    		String val = (String) in.readObject();
    		if (val.length() > MAX_INLINE_STRING_LENGTH) {
				cache.add(val);
			}
    		return val;
    	}
    	
    	@Override
    	protected void writeObject(ObjectOutput out, Object obj,
    			Map<Object, Integer> cache) throws IOException {
    		String str = (String)obj;
    		Integer val = cache.get(str);
    		if (val != null) {
    			out.writeByte(REPEATED_STRING);
    			out.writeInt(val);
    			return;
    		} 
    		if (str.length() > MAX_INLINE_STRING_LENGTH) {
    			cache.put(str, cache.size());
    		}
    		super.writeObject(out, obj, cache);
    	}
    	
    	@Override
    	public boolean usesCache(byte version) {
    		return true;
    	}
    }

    private class NullColumnSerializer1 extends ColumnSerializer {
    	@Override
    	public void writeColumn(ObjectOutput out, int col,
    			List<? extends List<?>> batch, Map<Object, Integer> cache) throws IOException {
    	}

    	@Override
    	public void readColumn(ObjectInput in, int col,
    			List<List<Object>> batch, byte[] isNull, List<Object> cache) throws IOException,
    			ClassNotFoundException {
    	}
    }

    private class ClobColumnSerializer1 extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
        	((Externalizable)obj).writeExternal(out);
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException, ClassNotFoundException {
        	ClobType ct = new ClobType();
        	ct.readExternal(in);
            return ct;
        }
    }

    private class BlobColumnSerializer1 extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
        	((Externalizable)obj).writeExternal(out);
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException, ClassNotFoundException {
        	BlobType bt = new BlobType();
        	bt.readExternal(in);
            return bt;
        }
    }

    private class XmlColumnSerializer1 extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
        	((XMLType)obj).writeExternal(out, CURRENT_VERSION);
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException, ClassNotFoundException {
        	XMLType xt = new XMLType();
        	xt.readExternal(in, CURRENT_VERSION);
            return xt;
        }
    }

    /**
     * Packs the (boolean) information about whether data values in the column are null
     * into bytes so that we send ~n/8 instead of n bytes.
     * @param out
     * @param col
     * @param batch
     * @throws IOException
     *
     */
    static void writeIsNullData(ObjectOutput out, int col, List<? extends List<?>> batch) throws IOException {
        int numBytes = batch.size() / 8, row = 0, currentByte = 0;
        for (int byteNum = 0; byteNum < numBytes; byteNum++, row+=8) {
            currentByte  = (batch.get(row).get(col) == null) ? 0x80 : 0;
            if (batch.get(row+1).get(col) == null) {
				currentByte |= 0x40;
			}
            if (batch.get(row+2).get(col) == null) {
				currentByte |= 0x20;
			}
            if (batch.get(row+3).get(col) == null) {
				currentByte |= 0x10;
			}
            if (batch.get(row+4).get(col) == null) {
				currentByte |= 0x08;
			}
            if (batch.get(row+5).get(col) == null) {
				currentByte |= 0x04;
			}
            if (batch.get(row+6).get(col) == null) {
				currentByte |= 0x02;
			}
            if (batch.get(row+7).get(col) == null) {
				currentByte |= 0x01;
			}
            out.write(currentByte);
        }
        if (batch.size() % 8 > 0) {
            currentByte = 0;
            for (int mask = 0x80; row < batch.size(); row++, mask >>= 1) {
                if (batch.get(row).get(col) == null) {
					currentByte |= mask;
				}
            }
            out.write(currentByte);
        }
    }

    static void writeIsNullData(ObjectOutput out, int offset, Object[] batch) throws IOException {
        int currentByte = 0;
        for (int mask = 0x80; offset < batch.length; offset++, mask >>= 1) {
            if (batch[offset] == null) {
				currentByte |= mask;
			}
        }
        out.write(currentByte);
    }

    /**
     * Reads the isNull data into a byte array
     * @param in
     * @param isNullBytes
     * @throws IOException
     *
     */
    static void readIsNullData(ObjectInput in, byte[] isNullBytes) throws IOException {
        for (int i = 0; i < isNullBytes.length; i++) {
            isNullBytes[i] = in.readByte();
        }
    }

    /**
     * Gets whether a data value is null based on a packed byte array containing boolean data
     * @param isNull
     * @param row
     * @return
     *
     */
    static final boolean isNullObject(byte[] isNull, int row) {
        //              byte number           mask     bits to shift mask
        return (isNull [ row / 8 ]         & (0x01 << (7 - (row % 8))))   != 0;
    }

	private boolean isNullObject(int row, byte b) {
		return (b         & (0x01 << (7 - (row % 8))))   != 0;
	}

    /**
     * An abstract serializer for native types
     *
     */
    private class ColumnSerializer {
        public void writeColumn(ObjectOutput out, int col, List<? extends List<?>> batch, Map<Object, Integer> cache) throws IOException {
            writeIsNullData(out, col, batch);
            Object obj = null;
            for (int i = 0; i < batch.size(); i++) {
                obj = batch.get(i).get(col);
                if (obj != null) {
                    writeObject(out, obj, cache);
                }
            }
        }

        public void readColumn(ObjectInput in, int col, List<List<Object>> batch, byte[] isNull, List<Object> cache) throws IOException, ClassNotFoundException {
            readIsNullData(in, isNull);
            for (int i = 0; i < batch.size(); i++) {
                if (!isNullObject(isNull, i)) {
                    batch.get(i).set(col, readObject(in, cache));
                }
            }
        }

        protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
        	out.writeObject(obj);
        }
        protected Object readObject(ObjectInput in, List<Object> cache) throws IOException, ClassNotFoundException {
        	return in.readObject();
        }
        
        public boolean usesCache(byte version) {
        	return false;
        }
    }

    private class IntColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeInt(((Integer)obj).intValue());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return Integer.valueOf(in.readInt());
        }
    }

    private class LongColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeLong(((Long)obj).longValue());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return Long.valueOf(in.readLong());
        }
    }

    private class FloatColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeFloat(((Float)obj).floatValue());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return new Float(in.readFloat());
        }
    }

    private class DoubleColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeDouble(((Double)obj).doubleValue());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return new Double(in.readDouble());
        }
    }

    private class ShortColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeShort(((Short)obj).shortValue());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return Short.valueOf(in.readShort());
        }
    }

    private class BooleanColumnSerializer extends ColumnSerializer {
        /* This implementation compacts the isNull and boolean data for non-null values into a byte[]
         * by using a 8 bit mask that is bit-shifted to mask each value.
         */
    	@Override
        public void writeColumn(ObjectOutput out, int col, List<? extends List<?>> batch, Map<Object, Integer> cache) throws IOException {
            int currentByte = 0;
            int mask = 0x80;
            Object obj;
            for (int row = 0; row < batch.size(); row++) {
                // Write the isNull value
                obj = batch.get(row).get(col);
                if (obj == null ) {
                    currentByte |= mask;
                }
                mask >>= 1; // Shift the mask to the next bit
                if (mask == 0) {
                    // If the current byte has been used up, write it and reset.
                    out.write(currentByte);
                    currentByte = 0;
                    mask = 0x80;
                }
                if (obj != null) {
                    // Write the boolean value if it's not null
                    if (((Boolean)obj).booleanValue()) {
                        currentByte |= mask;
                    }
                    mask >>= 1;
                    if (mask == 0) {
                        out.write(currentByte);
                        currentByte = 0;
                        mask = 0x80;
                    }
                }
            }
            // Invariant mask != 0
            // If we haven't reached the eight-row mark then the loop would not have written this byte
            // Write the final byte containing data for th extra rows, if it exists.
            if (mask != 0x80) {
                out.write(currentByte);
            }
        }
    	
    	@Override
    	public void readColumn(ObjectInput in, int col,
    			List<List<Object>> batch, byte[] isNull, List<Object> cache) throws IOException,
        		ClassNotFoundException {
            int currentByte = 0, mask = 0; // Initialize the mask so that it is reset in the loop
            boolean isNullVal;
            for (int row = 0; row < batch.size(); row++) {
                if (mask == 0) {
                    // If we used up the byte, read the next one, and reset the mask
                    currentByte = in.read();
                    mask = 0x80;
                }
                isNullVal = (currentByte & mask) != 0;
                mask >>= 1; // Shift the mask to the next bit
                if (!isNullVal) {
                    if (mask == 0) {
                        currentByte = in.read();
                        mask = 0x80;
                    }
                    batch.get(row).set(col, ((currentByte & mask) == 0) ? Boolean.FALSE : Boolean.TRUE);
                    mask >>= 1;
                }
            }
        }
    }

    private class ByteColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeByte(((Byte)obj).byteValue());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return Byte.valueOf(in.readByte());
        }
    }

    private class CharColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeChar(((Character)obj).charValue());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return Character.valueOf(in.readChar());
        }
    }

    private class BigIntegerColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            BigInteger val = (BigInteger)obj;
            byte[] bytes = val.toByteArray();
            out.writeInt(bytes.length);
            out.write(bytes);
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            return new BigInteger(bytes);
        }
    }

    private class BigDecimalColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            BigDecimal val = (BigDecimal)obj;
            out.writeInt(val.scale());
            BigInteger unscaled = val.unscaledValue();
            byte[] bytes = unscaled.toByteArray();
            out.writeInt(bytes.length);
            out.write(bytes);
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            int scale = in.readInt();
            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            return new BigDecimal(new BigInteger(bytes), scale);
        }
    }

    private class DateColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeLong(((java.sql.Date)obj).getTime());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return new java.sql.Date(in.readLong());
        }
    }

    private class TimeColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            out.writeLong(((Time)obj).getTime());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return new Time(in.readLong());
        }
    }

    static int DATE_NORMALIZER = 0;
    public final static long MIN_DATE_32;
    public final static long MAX_DATE_32;
    public final static long MIN_TIME_32;
    public final static long MAX_TIME_32;

	static {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		c.set(1900, 0, 1, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		MIN_DATE_32 = c.getTimeInMillis();
		MAX_DATE_32 = MIN_DATE_32 + ((1l<<32)-1)*60000;
		DATE_NORMALIZER = -(int)(MIN_DATE_32/60000); //support a 32 bit range starting at this value
		MAX_TIME_32 = Integer.MAX_VALUE*1000l;
		MIN_TIME_32 = Integer.MIN_VALUE*1000l;
	}

    private class DateColumnSerializer1 extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            long time = ((java.sql.Date)obj).getTime();
            if (time < MIN_DATE_32 || time > MAX_DATE_32) {
            	throw new IOException(Messages.gs(Messages.TEIID.TEIID20029, obj.getClass().getName()));
            }
			out.writeInt((int)(time/60000) + DATE_NORMALIZER);
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return new java.sql.Date(((in.readInt()&0xffffffffL) - DATE_NORMALIZER)*60000);
        }
    }

    private class TimeColumnSerializer1 extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            long time = ((Time)obj).getTime();
            if (time < MIN_TIME_32 || time > MAX_TIME_32) {
            	throw new IOException(Messages.gs(Messages.TEIID.TEIID20029, obj.getClass().getName()));
            }
			out.writeInt((int)(time/1000));
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            return new Time((in.readInt()&0xffffffffL)*1000);
        }
    }

    private class TimestampColumnSerializer extends ColumnSerializer {
        @Override
		protected void writeObject(ObjectOutput out, Object obj, Map<Object, Integer> cache) throws IOException {
            Timestamp ts =  (Timestamp)obj;
            out.writeLong(ts.getTime());
            out.writeInt(ts.getNanos());
        }
        @Override
		protected Object readObject(ObjectInput in, List<Object> cache) throws IOException {
            Timestamp ts = new Timestamp(in.readLong());
            ts.setNanos(in.readInt());
            return ts;
        }
    }

    private ColumnSerializer getSerializer(String type, byte version) {
    	ColumnSerializer[] sers = serializers.get(type);
    	if (sers == null) {
    		if (DefaultDataTypeManager.isArrayType(type)) {
    			if (version < 2) {
    				return arrayColumnSerializer;
    			}
    			//TODO: make this scalable with version
    			return arrayColumnSerialier2;
    		}
    		return defaultSerializer;
    	}
    	return sers[Math.min(version, sers.length - 1)];
    }

    @Override
    public void writeBatch(ObjectOutput out, String[] types, List<? extends List<?>> batch) throws IOException {
    	writeBatch(out, types, batch, CURRENT_VERSION);
    }

    @Override
    public void writeBatch(ObjectOutput out, String[] types, List<? extends List<?>> batch, byte version) throws IOException {
        if (batch == null) {
            out.writeInt(-1);
        } else {
        	if (version > 0 && batch.size() > 0) {
                out.writeInt(-batch.size() -1);
                out.writeByte(version);
        	} else {
                out.writeInt(batch.size());
        	}
            if (batch.size() > 0) {
	            int columns = types.length;
	            out.writeInt(columns);
	            Map<Object, Integer> cache = null;
	            for(int i = 0; i < columns; i++) {
	            	ColumnSerializer serializer = getSerializer(types[i], version);
	            	
	            	if (cache == null && serializer.usesCache(version)) {
	            		cache = new HashMap<Object, Integer>();
	            	}
	                try {
	                    serializer.writeColumn(out, i, batch, cache);
	                } catch (ClassCastException e) {
	                    Object obj = null;
	                    String objectClass = null;
	                    objectSearch: for (int row = 0; row < batch.size(); row++) {
	                        obj = batch.get(row).get(i);
	                        if (obj != null) {
	                            objectClass = obj.getClass().getName();
	                            break objectSearch;
	                        }
	                    }
	                     throw new RuntimeException(Messages.gs(Messages.TEIID.TEIID20001, new Object[] {types[i], new Integer(i), objectClass}), e);
	                }
	            }
            }
        }
    }

    @Override
    public List<List<Object>> readBatch(ObjectInput in, String[] types) throws IOException, ClassNotFoundException {
    	int rows = 0;
    	try {
    		rows = in.readInt();
    	} catch (IOException e) {
    		//7.4 compatibility
    		if (types == null || types.length == 0) {
                List<Object>[] result = (List[])in.readObject();
                ArrayList<List<Object>> batch = new ArrayList<List<Object>>();
                batch.addAll(Arrays.asList(result));
                return batch;
            }
    		throw e;
    	}
        if (rows == 0) {
            return new ArrayList<List<Object>>(0);
        }
        if (rows == -1) {
        	return null;
        }
        byte version = (byte)0;
        if (rows < 0) {
        	rows = -(rows+1);
        	version = in.readByte();
        }
        int columns = in.readInt();
        List<List<Object>> batch = new ResizingArrayList<List<Object>>(rows);
        int numBytes = rows/8;
        int extraRows = rows % 8;
        for (int currentRow = 0; currentRow < rows; currentRow++) {
            batch.add(currentRow, Arrays.asList(new Object[columns]));
        }
        byte[] isNullBuffer = new byte[(extraRows > 0) ? numBytes + 1: numBytes];
        List<Object> cache = null;
        for (int col = 0; col < columns; col++) {
            ColumnSerializer serializer = getSerializer(types[col], version);
            if (cache == null && serializer.usesCache(version)) {
        		cache = new ArrayList<Object>();
        	}
            serializer.readColumn(in, col, batch, isNullBuffer, cache);
        }
        return batch;
    }
}
