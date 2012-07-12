package edu.berkeley.cs.amplab.carat.android.storage;

import java.io.Serializable;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;

/**
 * Simple container class for Hog/Bug data to save memory.
 * @author Eemil Lagerspetz
 *
 */
public class SimpleHogBug implements Serializable{
    /**
     * Auto-generated UID for serialization
     */
    private static final long serialVersionUID = 8272459694607111058L;
    
    private Type type = null;
    
    public Type getType (){
        return type;
    }
    
    public boolean isBug(){ return type == Type.BUG; }
    
    public SimpleHogBug(String appName, Type type){
        this.type = type;
        if (type == Type.OS)
            appPriority = CaratApplication.importanceString(CaratApplication.IMPORTANCE_SUGGESTION);
        this.appName = appName;
    }
    
    
    private String appName; // optional
    /**
     * @return the appName
     */
    public String getAppName() {
        return appName;
    }
    /**
     * @param appName the appName to set
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    /**
     * @return the wDistance
     */
    public double getwDistance() {
        return wDistance;
    }
    /**
     * @param wDistance the wDistance to set
     */
    public void setwDistance(double wDistance) {
        this.wDistance = wDistance;
    }
    /**
     * @return the xVals
     */
    public double[] getxVals() {
        return xVals;
    }
    /**
     * @param xVals the xVals to set
     */
    public void setxVals(double[] xVals) {
        this.xVals = xVals;
    }
    /**
     * @return the yVals
     */
    public double[] getyVals() {
        return yVals;
    }
    /**
     * @param yVals the yVals to set
     */
    public void setyVals(double[] yVals) {
        this.yVals = yVals;
    }
    /**
     * @return the xValsWithout
     */
    public double[] getxValsWithout() {
        return xValsWithout;
    }
    /**
     * @param xValsWithout the xValsWithout to set
     */
    public void setxValsWithout(double[] xValsWithout) {
        this.xValsWithout = xValsWithout;
    }
    /**
     * @return the yValsWithout
     */
    public double[] getyValsWithout() {
        return yValsWithout;
    }
    /**
     * @param yValsWithout the yValsWithout to set
     */
    public void setyValsWithout(double[] yValsWithout) {
        this.yValsWithout = yValsWithout;
    }
    /**
     * @return the expectedValue
     */
    public double getExpectedValue() {
        return expectedValue;
    }
    /**
     * @param expectedValue the expectedValue to set
     */
    public void setExpectedValue(double expectedValue) {
        this.expectedValue = expectedValue;
    }
    /**
     * @return the expectedValueWithout
     */
    public double getExpectedValueWithout() {
        return expectedValueWithout;
    }
    /**
     * @param expectedValueWithout the expectedValueWithout to set
     */
    public void setExpectedValueWithout(double expectedValueWithout) {
        this.expectedValueWithout = expectedValueWithout;
    }
    /**
     * @return the appLabel
     */
    public String getAppLabel() {
        return appLabel;
    }
    /**
     * @param appLabel the appLabel to set
     */
    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }
    /**
     * @return the appPriority
     */
    public String getAppPriority() {
        return appPriority;
    }
    /**
     * @param appPriority the appPriority to set
     */
    public void setAppPriority(String appPriority) {
        this.appPriority = appPriority;
    }
    private double wDistance; // optional
    private double[] xVals; // optional
    private double[] yVals; // optional
    private double[] xValsWithout; // optional
    private double[] yValsWithout; // optional
    private double expectedValue; // optional
    private double expectedValueWithout; // optional
    private String appLabel; // optional
    private String appPriority; // optional
}
