package edu.berkeley.cs.amplab.carat.android.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.thrift.meta_data.FieldMetaData;

import android.util.Log;
import edu.berkeley.cs.amplab.carat.thrift.BatteryDetails;
import edu.berkeley.cs.amplab.carat.thrift.CpuStatus;
import edu.berkeley.cs.amplab.carat.thrift.Feature;
import edu.berkeley.cs.amplab.carat.thrift.NetworkDetails;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import edu.berkeley.cs.amplab.carat.thrift.Sample._Fields;

/**
 * 
 * This class reads HashMaps from the database and outputs a Sample with fields
 * filled in.
 * 
 * TODO: FIXME: This class needs to be modified when the protocol is changed. Otherwise new fields will not be included in Samples.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class SampleReader {
	
	public static final String TAG = "SampleReader"; 

    /**
     * Library class, instantiation prohibited.
     */
    private SampleReader() {
    }
    
    /**
     * For simplicity, this method relies on the fact that
     * only the piList of Sample has any List type elements.
     * This will fail to record those from substructs (NetworkDetails, BatteryDetails, CpuStatus),
     * and will need to be changed if those are added.
     * 
     * Does not record CallInfo, CellInfo, or CallMonth types.
     */
    public static final HashMap<String, String> writeSample(Sample s) {
        HashMap<String, String> m = new HashMap<String, String>();
        for (_Fields sf: Sample.metaDataMap.keySet()){
            FieldMetaData md = Sample.metaDataMap.get(sf);
            switch (md.valueMetaData.type) {
            case org.apache.thrift.protocol.TType.STRING:
            case org.apache.thrift.protocol.TType.I32:
            case org.apache.thrift.protocol.TType.DOUBLE:
                m.put(sf.getFieldName(), s.getFieldValue(sf).toString().replaceAll("\n", " ").replaceAll(";", ","));
                break;
            case org.apache.thrift.protocol.TType.STRUCT:
                if (md.fieldName.equals(Sample._Fields.NETWORK_DETAILS.getFieldName()) && s.networkDetails != null) {
                    int len = NetworkDetails._Fields.values().length;
                    StringBuilder b = new StringBuilder();
                    for (int i = 1; i <= len; i++){
                        b.append((""+s.networkDetails.getFieldValue(NetworkDetails._Fields.findByThriftId(i))).replaceAll("\n", " ").replaceAll(";", ","));
                        if ( i < len)
                            b.append("\n");
                    }
                    m.put(sf.getFieldName(), b.toString());
                } else if (md.fieldName.equals(Sample._Fields.BATTERY_DETAILS.getFieldName()) && s.batteryDetails != null) {
                    int len = BatteryDetails._Fields.values().length;
                    StringBuilder b = new StringBuilder();
                    for (int i = 1; i <= len; i++){
                        b.append((""+s.batteryDetails.getFieldValue(BatteryDetails._Fields.findByThriftId(i))).replaceAll("\n", " ").replaceAll(";", ","));
                        if ( i < len)
                            b.append("\n");
                    }
                    m.put(sf.getFieldName(), b.toString());
                } else if (md.fieldName.equals(Sample._Fields.CPU_STATUS.getFieldName()) && s.cpuStatus != null) {
                    int len = CpuStatus._Fields.values().length;
                    StringBuilder b = new StringBuilder();
                    for (int i = 1; i <= len; i++){
                        b.append((""+s.cpuStatus.getFieldValue(CpuStatus._Fields.findByThriftId(i))).replaceAll("\n", " ").replaceAll(";", ","));
                        if ( i < len)
                            b.append("\n");
                    }
                    m.put(sf.getFieldName(), b.toString());
                }/*
                  * else if (md.fieldName.equals("CallInfo")){ }
                  */
                break;
            case org.apache.thrift.protocol.TType.LIST:
                if (md.fieldName.equals(Sample._Fields.EXTRA.getFieldName()) && s.extra != null) {
                    StringBuilder b = new StringBuilder();
                    for (Feature f : s.extra) {
                        b.append(f.key.replaceAll("\n", " ").replaceAll(";", ",") + ";" + f.value.replaceAll("\n", " ").replaceAll(";", ",") + "\n");
                    }
                    if (b.length() > 1)
                    	b.deleteCharAt(b.lastIndexOf("\n"));
                    m.put(sf.getFieldName(), b.toString());
                } else if (md.fieldName.equals(Sample._Fields.LOCATION_PROVIDERS.getFieldName()) && s.locationProviders != null) {
                    StringBuilder b = new StringBuilder();
                    for (String lp : s.locationProviders)
                        b.append(lp + "\n");
                    if (b.length() > 1)
                    	b.deleteCharAt(b.lastIndexOf("\n"));
                    m.put(sf.getFieldName(), b.toString());
                } else if (md.fieldName.equals(Sample._Fields.PI_LIST.getFieldName()) && s.piList != null) {
                    StringBuilder b = new StringBuilder();
                    for (ProcessInfo pi : s.piList) {
                        int len = ProcessInfo._Fields.values().length;
                        for (int i = 1; i <= len; i++) {
                            ProcessInfo._Fields pif = ProcessInfo._Fields
                                    .findByThriftId(i);
                            FieldMetaData pmd = ProcessInfo.metaDataMap
                                    .get(pif);
                            if (pmd.valueMetaData.type == org.apache.thrift.protocol.TType.LIST) {
                                if (pi.appSignatures != null) {
                                    for (int j = 0; j < pi.appSignatures.size(); j++) {
                                        String sig = pi.appSignatures.get(j);
                                        b.append(sig);
                                        if (j + 1 < len)
                                            b.append("#");
                                    }
                                }
                            } else {
                                b.append(("" + pi.getFieldValue(pif)).replaceAll("\n", " ").replaceAll(";", ","));
                            }
                            if (i < len)
                                b.append(";");
                        }
                        b.append("\n");
                    }
                    if (b.length() > 1)
                    	b.deleteCharAt(b.lastIndexOf("\n"));
                    m.put(sf.getFieldName(), b.toString());
                }
                break;
            default:
            }
        }
        return m;
    }

    /**
     * Read a Sample from a HashMap stored in the Carat Sample db.
     * @param data
     * @return
     */
    public static final Sample readSample(Object data) {
        Sample s = null;
        if (data != null && data instanceof HashMap<?, ?>) {
            HashMap<String, String> m = (HashMap<String, String>) data;
            s = new Sample();
            NetworkDetails n = new NetworkDetails();
            BatteryDetails bd = new BatteryDetails();
            // CellInfo ci = new CellInfo();
            // CallInfo calli = new CallInfo();
            // CallMonth cm = new CallMonth();
            CpuStatus cs = new CpuStatus();
            // Set single fields automatically:
            for (String k : m.keySet()) {
                _Fields sf = Sample._Fields.findByName(k);

                if (sf != null) {
                    // Top level Sample field.
                    FieldMetaData md = Sample.metaDataMap.get(sf);
                    String cleaned = m.get(k).replaceAll("\"", "");
                    switch (md.valueMetaData.type) {
                    case org.apache.thrift.protocol.TType.STRING:
                        s.setFieldValue(sf, cleaned);
                        break;
                    case org.apache.thrift.protocol.TType.I32:
						try {
							s.setFieldValue(sf, Integer.parseInt(cleaned));
						} catch (NumberFormatException e) {
							Log.e(TAG, "Could not read " + md.fieldName
									+ ": \"" + cleaned + "\" as an int");
						}
						break;
					case org.apache.thrift.protocol.TType.DOUBLE:
						try {
							s.setFieldValue(sf, Double.parseDouble(cleaned));
						} catch (NumberFormatException e) {
							Log.e(TAG, "Could not read " + md.fieldName
									+ ": \"" + cleaned + "\" as a double");
						}
                        break;
                    case org.apache.thrift.protocol.TType.STRUCT:
                        if (md.fieldName.equals(Sample._Fields.NETWORK_DETAILS.getFieldName())) {
                            fillNetworkDetails(m.get(k), n);
                            s.setNetworkDetails(n);
                        } else if (md.fieldName.equals(Sample._Fields.BATTERY_DETAILS.getFieldName())) {
                            fillBatteryDetails(m.get(k), bd);
                            s.setNetworkDetails(n);
                        } else if (md.fieldName.equals(Sample._Fields.CPU_STATUS.getFieldName())) {
                            fillCpuStatusDetails(m.get(k), cs);
                            s.setNetworkDetails(n);
                        }/*
                          * else if (md.fieldName.equals("CallInfo")){ }
                          */
                        break;
                    case org.apache.thrift.protocol.TType.LIST:
                        if (md.fieldName.equals(Sample._Fields.EXTRA.getFieldName())) {
                            List<Feature> list = new LinkedList<Feature>();
                            String[] extras = m.get(k).split("\n");
                            for (String e : extras) {
                                Feature f = new Feature();
                                String[] feat = e.split(";");
                                if (feat.length > 1) {
                                    f.setKey(feat[0]);
                                    f.setValue(feat[1]);
                                }
                                list.add(f);
                            }
                            s.setExtra(list);
                        } else if (md.fieldName.equals(Sample._Fields.LOCATION_PROVIDERS.getFieldName())) {
                            List<String> list = new LinkedList<String>();
                            String[] arr = m.get(k).split("\n");
                            for (String lp : arr)
                                list.add(lp);
                            s.setLocationProviders(list);
                        }else if (md.fieldName.equals(Sample._Fields.PI_LIST.getFieldName())){
                            // Set piList fields automatically:
                            LinkedList<ProcessInfo> piList = new LinkedList<ProcessInfo>();
                            String[] processes = m.get(md.fieldName).split("\n");
                            for (String process : processes) {
                                String[] items = process.split(";");
                                ProcessInfo pi = new ProcessInfo();
                                /*
                                 * Items are in the same order as they appear in ProcessInfo
                                 * protocol class, so I can use Thrift ID for setting the fields
                                 * automatically.
                                 */
                                for (int i = 1; i <= items.length; i++) {
                                	if (items[i-1] == null)
                                		continue;
                                    ProcessInfo._Fields pif = ProcessInfo._Fields
                                            .findByThriftId(i);
                                    FieldMetaData pmd = ProcessInfo.metaDataMap.get(pif);
                                    cleaned = items[i - 1].replaceAll("\"", "");
                                    switch (pmd.valueMetaData.type) {
                                    case org.apache.thrift.protocol.TType.STRING:
                                        pi.setFieldValue(pif, cleaned);
                                        break;
                                    case org.apache.thrift.protocol.TType.I32:
                                    	try{
                                        pi.setFieldValue(pif, Integer.parseInt(cleaned));
                                    	}catch (NumberFormatException e){
                                    		Log.e(TAG, "Could not read "+md.fieldName+": \""+cleaned+"\" as an int");
                                    	}
                                        break;
                                    case org.apache.thrift.protocol.TType.DOUBLE:
                                    	try{
                                        pi.setFieldValue(pif, Double.parseDouble(cleaned));
                                    	}catch (NumberFormatException e){
                                    		Log.e(TAG, "Could not read "+md.fieldName+": \""+cleaned+"\" as a double");
                                    	}
                                        break;
                                    case org.apache.thrift.protocol.TType.BOOL:
                                    	try{
                                        pi.setFieldValue(pif,
                                                Boolean.parseBoolean(cleaned));
                                    	}catch (NumberFormatException e){
                                    		Log.e(TAG, "Could not read "+md.fieldName+": \""+cleaned+"\" as a bool");
                                    	}
                                        break;
                                    case org.apache.thrift.protocol.TType.LIST:
                                        List<String> list = new LinkedList<String>();
                                        String[] arr = cleaned.split("#");
                                        for (String sig : arr)
                                            list.add(sig);
                                        pi.setFieldValue(pif, list);
                                        break;
                                    default:
                                    }
                                }
                                piList.add(pi);
                            }
                            s.setPiList(piList);
                        }
                        break;
                    default:
                    }
                }
            }
        }

        return s;
    }

    private static void fillCpuStatusDetails(String string, CpuStatus cs) {
        String[] items = string.split("\n");
        for (int i = 1; i <= items.length; i++) {
            CpuStatus._Fields pif = CpuStatus._Fields.findByThriftId(i);
            FieldMetaData md = CpuStatus.metaDataMap.get(pif);
            String cleaned = items[i - 1].replaceAll("\"", "");
            switch (md.valueMetaData.type) {
            case org.apache.thrift.protocol.TType.STRING:
                cs.setFieldValue(pif, cleaned);
                break;
            case org.apache.thrift.protocol.TType.I32:
            	try{
                cs.setFieldValue(pif, Integer.parseInt(cleaned));
            }catch (NumberFormatException e){
        		Log.e(TAG, "Could not read "+md.fieldName+": \""+cleaned+"\" as an int");
        	}
                break;
            case org.apache.thrift.protocol.TType.DOUBLE:
            	try{
                cs.setFieldValue(pif, Double.parseDouble(cleaned));
            }catch (NumberFormatException e){
        		Log.e(TAG, "Could not read "+md.fieldName+": \""+cleaned+"\" as a double");
        	}
                break;
            case org.apache.thrift.protocol.TType.BOOL:
            	try{
                cs.setFieldValue(pif, Boolean.parseBoolean(cleaned));
            	}catch (NumberFormatException e){
            		Log.e(TAG, "Could not read "+md.fieldName+": \""+cleaned+"\" as a bool");
            	}
                break;
            case org.apache.thrift.protocol.TType.LIST:
                List<String> list = new LinkedList<String>();
                String[] arr = items[i - 1].split(";");
                for (String sig : arr)
                    list.add(sig);
                cs.setFieldValue(pif, list);
                break;
            default:
            }
        }
    }

	private static void fillBatteryDetails(String string, BatteryDetails bd) {
		String[] items = string.split("\n");
		for (int i = 1; i <= items.length; i++) {
			BatteryDetails._Fields pif = BatteryDetails._Fields
					.findByThriftId(i);
			FieldMetaData md = BatteryDetails.metaDataMap.get(pif);
			String cleaned = items[i - 1].replaceAll("\"", "");
			switch (md.valueMetaData.type) {
			case org.apache.thrift.protocol.TType.STRING:
				bd.setFieldValue(pif, cleaned);
				break;
			case org.apache.thrift.protocol.TType.I32:
				try {
					bd.setFieldValue(pif, Integer.parseInt(cleaned));
				} catch (NumberFormatException e) {
					Log.e(TAG, "Could not read "+md.fieldName+": \"" + cleaned + "\" as an int");
				}
				break;
			case org.apache.thrift.protocol.TType.DOUBLE:
				try {
					bd.setFieldValue(pif, Double.parseDouble(cleaned));
				} catch (NumberFormatException e) {
					Log.e(TAG, "Could not read "+md.fieldName+": \"" + cleaned + "\" as a double");
				}
				break;
			case org.apache.thrift.protocol.TType.BOOL:
				try {
					bd.setFieldValue(pif, Boolean.parseBoolean(cleaned));
				} catch (NumberFormatException e) {
					Log.e(TAG, "Could not read "+md.fieldName+": \"" + cleaned + "\" as a bool");
				}
				break;
			case org.apache.thrift.protocol.TType.LIST:
				List<String> list = new LinkedList<String>();
				String[] arr = items[i - 1].split(";");
				for (String sig : arr)
					list.add(sig);
				bd.setFieldValue(pif, list);
				break;
			default:
			}
		}
	}

	private static void fillNetworkDetails(String string, NetworkDetails nd) {
		String[] items = string.split("\n");
		for (int i = 1; i <= items.length; i++) {
			NetworkDetails._Fields pif = NetworkDetails._Fields
					.findByThriftId(i);
			FieldMetaData md = NetworkDetails.metaDataMap.get(pif);
			String cleaned = items[i - 1].replaceAll("\"", "");
			switch (md.valueMetaData.type) {
			case org.apache.thrift.protocol.TType.STRING:
				nd.setFieldValue(pif, cleaned);
				break;
			case org.apache.thrift.protocol.TType.I32:
				try {
					nd.setFieldValue(pif, Integer.parseInt(cleaned));
				} catch (NumberFormatException e) {
					Log.e(TAG, "Could not read " + md.fieldName + ": \""
							+ cleaned + "\" as an int");
				}
				break;
			case org.apache.thrift.protocol.TType.DOUBLE:
				try {
					nd.setFieldValue(pif, Double.parseDouble(cleaned));
				} catch (NumberFormatException e) {
					Log.e(TAG, "Could not read " + md.fieldName + ": \""
							+ cleaned + "\" as a double");
				}
				break;
			case org.apache.thrift.protocol.TType.BOOL:
				try {
					nd.setFieldValue(pif, Boolean.parseBoolean(cleaned));
				} catch (NumberFormatException e) {
					Log.e(TAG, "Could not read " + md.fieldName + ": \""
							+ cleaned + "\" as a bool");
				}
				break;
			case org.apache.thrift.protocol.TType.LIST:
				List<String> list = new LinkedList<String>();
				String[] arr = items[i - 1].split(";");
				for (String sig : arr)
					list.add(sig);
				nd.setFieldValue(pif, list);
				break;
			default:
			}
        }
    }
	
	private static String cleanStr(String dirty){
		return dirty.replace('\n', ' ').replace(';', ',');
	}
}
