/*     */ package com.android.internal.os;
/*     */ 
/*     */ import android.content.Context;
/*     */ import android.content.res.Resources;
/*     */ import android.content.res.XmlResourceParser;
/*     */ import com.android.internal.util.XmlUtils;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import org.xmlpull.v1.XmlPullParserException;
/*     */ 
/*     */ public class PowerProfile
/*     */ {
/*     */   public static final String POWER_NONE = "none";
/*     */   public static final String POWER_CPU_IDLE = "cpu.idle";
/*     */   public static final String POWER_CPU_AWAKE = "cpu.awake";
/*     */   public static final String POWER_CPU_ACTIVE = "cpu.active";
/*     */   public static final String POWER_WIFI_SCAN = "wifi.scan";
/*     */   public static final String POWER_WIFI_ON = "wifi.on";
/*     */   public static final String POWER_WIFI_ACTIVE = "wifi.active";
/*     */   public static final String POWER_GPS_ON = "gps.on";
/*     */   public static final String POWER_BLUETOOTH_ON = "bluetooth.on";
/*     */   public static final String POWER_BLUETOOTH_ACTIVE = "bluetooth.active";
/*     */   public static final String POWER_BLUETOOTH_AT_CMD = "bluetooth.at";
/*     */   public static final String POWER_SCREEN_ON = "screen.on";
/*     */   public static final String POWER_RADIO_ON = "radio.on";
/*     */   public static final String POWER_RADIO_SCANNING = "radio.scanning";
/*     */   public static final String POWER_RADIO_ACTIVE = "radio.active";
/*     */   public static final String POWER_SCREEN_FULL = "screen.full";
/*     */   public static final String POWER_AUDIO = "dsp.audio";
/*     */   public static final String POWER_VIDEO = "dsp.video";
/*     */   public static final String POWER_CPU_SPEEDS = "cpu.speeds";
/*     */   public static final String POWER_BATTERY_CAPACITY = "battery.capacity";
/* 143 */   static final HashMap<String, Object> sPowerMap = new HashMap();
/*     */   private static final String TAG_DEVICE = "device";
/*     */   private static final String TAG_ITEM = "item";
/*     */   private static final String TAG_ARRAY = "array";
/*     */   private static final String TAG_ARRAYITEM = "value";
/*     */   private static final String ATTR_NAME = "name";
/*     */ 
/*     */   public PowerProfile(Context context)
/*     */   {
/* 154 */     if (sPowerMap.size() == 0)
/* 155 */       readPowerValuesFromXml(context);
/*     */   }
/*     */ 
/*     */   private void readPowerValuesFromXml(Context context)
/*     */   {
/* 160 */     int id = 17760266;
/* 161 */     XmlResourceParser parser = context.getResources().getXml(id);
/* 162 */     boolean parsingArray = false;
/* 163 */     ArrayList array = new ArrayList();
/* 164 */     String arrayName = null;
/*     */     try
/*     */     {
/* 167 */       XmlUtils.beginDocument(parser, "device");
/*     */       while (true)
/*     */       {
/* 170 */         XmlUtils.nextElement(parser);
/*     */ 
/* 172 */         String element = parser.getName();
/* 173 */         if (element == null)
/*     */           break;
/* 175 */         if ((parsingArray) && (!element.equals("value")))
/*     */         {
/* 177 */           sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
/* 178 */           parsingArray = false;
/*     */         }
/* 180 */         if (element.equals("array")) {
/* 181 */           parsingArray = true;
/* 182 */           array.clear();
/* 183 */           arrayName = parser.getAttributeValue(null, "name");
/* 184 */         } else if ((element.equals("item")) || (element.equals("value"))) {
/* 185 */           String name = null;
/* 186 */           if (!parsingArray) name = parser.getAttributeValue(null, "name");
/* 187 */           if (parser.next() == 4) {
/* 188 */             String power = parser.getText();
/* 189 */             double value = 0.0D;
/*     */             try {
/* 191 */               value = Double.valueOf(power).doubleValue();
/*     */             } catch (NumberFormatException nfe) {
/*     */             }
/* 194 */             if (element.equals("item"))
/* 195 */               sPowerMap.put(name, Double.valueOf(value));
/* 196 */             else if (parsingArray) {
/* 197 */               array.add(Double.valueOf(value));
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/* 202 */       if (parsingArray)
/* 203 */         sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
/*     */     }
/*     */     catch (XmlPullParserException e) {
/* 206 */       throw new RuntimeException(e);
/*     */     } catch (IOException e) {
/* 208 */       throw new RuntimeException(e);
/*     */     } finally {
/* 210 */       parser.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   public double getAveragePower(String type)
/*     */   {
/* 220 */     if (sPowerMap.containsKey(type)) {
/* 221 */       Object data = sPowerMap.get(type);
/* 222 */       if ((data instanceof Double[])) {
/* 223 */         return ((Double[])(Double[])data)[0].doubleValue();
/*     */       }
/* 225 */       return ((Double)sPowerMap.get(type)).doubleValue();
/*     */     }
/*     */ 
/* 228 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public double getAveragePower(String type, int level)
/*     */   {
/* 241 */     if (sPowerMap.containsKey(type)) {
/* 242 */       Object data = sPowerMap.get(type);
/* 243 */       if ((data instanceof Double[])) {
/* 244 */         Double[] values = (Double[])(Double[])data;
/* 245 */         if ((values.length > level) && (level >= 0))
/* 246 */           return values[level].doubleValue();
/* 247 */         if (level < 0) {
/* 248 */           return 0.0D;
/*     */         }
/* 250 */         return values[(values.length - 1)].doubleValue();
/*     */       }
/*     */ 
/* 253 */       return ((Double)data).doubleValue();
/*     */     }
/*     */ 
/* 256 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public double getBatteryCapacity()
/*     */   {
/* 266 */     return getAveragePower("battery.capacity");
/*     */   }
/*     */ 
/*     */   public int getNumSpeedSteps()
/*     */   {
/* 274 */     Object value = sPowerMap.get("cpu.speeds");
/* 275 */     if ((value != null) && ((value instanceof Double[]))) {
/* 276 */       return ((Double[])(Double[])value).length;
/*     */     }
/* 278 */     return 1;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\Leo\My Documents\Downloads\android-4.0.3_r1.jar
 * Qualified Name:     com.android.internal.os.PowerProfile
 * JD-Core Version:    0.6.0
 */