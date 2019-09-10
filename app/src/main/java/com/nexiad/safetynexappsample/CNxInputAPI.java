package com.nexiad.safetynexappsample;
public class CNxInputAPI {
	private float mTime;
	private float mAccelX;
	private float mAccelY;
	private float mAccelZ;
	private float mLat;
	private float mLon;
	private float mSpeed;
	private float mCap;
	private float mTimeDiffGPS;
	private int nbOfSat;
	private long gpsTimeLong;

	private Boolean locationUpdated = Boolean.FALSE;

	public boolean ParseData(String prmLine) {
		boolean isData = false;
	    	if(prmLine != null) {
		    	String line[] = prmLine.split(";");
	    		if(line.length > 14) {
	    			try {
						mTime = Float.parseFloat(line[0]);
			    		mAccelX = Float.parseFloat(line[1]);
				    	mAccelY = Float.parseFloat(line[2]);
				    	mAccelZ = Float.parseFloat(line[3]);
				    	mLat = Float.parseFloat(line[10]);
				    	mLon = Float.parseFloat(line[11]);
						mCap = Float.parseFloat(line[12]);
			    		mSpeed = Float.parseFloat(line[13]);
			    		mTimeDiffGPS = Float.parseFloat(line[14]);
				    	isData = true;
	    			} catch (NumberFormatException e) {
						e.printStackTrace();
		    		}
		    	}
	    	}
    	return isData;
	}
	public void setDataFromGps(){

	}

	public float getmTime() {
		return mTime;
	}

	public void setmTime(float mTime) {
		this.mTime = mTime;
	}

	public float getmAccelX() {
		return mAccelX;
	}

	public void setmAccelX(float mAccelX) {
		this.mAccelX = mAccelX;
	}

	public float getmAccelY() {
		return mAccelY;
	}

	public void setmAccelY(float mAccelY) {
		this.mAccelY = mAccelY;
	}

	public float getmAccelZ() {
		return mAccelZ;
	}

	public void setmAccelZ(float mAccelZ) {
		this.mAccelZ = mAccelZ;
	}

	public float getmLat() {
		return mLat;
	}

	public void setmLat(float mLat) {
		this.mLat = mLat;
	}

	public float getmLon() {
		return mLon;
	}

	public void setmLon(float mLon) {
		this.mLon = mLon;
	}

	public float getmSpeed() {
		return mSpeed;
	}

	public void setmSpeed(float mSpeed) {
		this.mSpeed = mSpeed;
	}

	public float getmCap() {
		return mCap;
	}

	public void setmCap(float mCap) {
		this.mCap = mCap;
	}

	public float getmTimeDiffGPS() {
		return mTimeDiffGPS;
	}

	public void setmTimeDiffGPS(float mTimeDiffGPS) {
		this.mTimeDiffGPS = mTimeDiffGPS;
	}


	public int getNbOfSat() {
		return nbOfSat;
	}

	public void setNbOfSat(int nbOfSat) {
		this.nbOfSat = nbOfSat;
	}

	public Boolean getLocationUpdated() {
		return locationUpdated;
	}

	public void setLocationUpdated(Boolean locationUpdated) {
		this.locationUpdated = locationUpdated;
	}

	public long getGpsTimeLong() {
		return gpsTimeLong;
	}

	public void setGpsTimeLong(long gpsTimeLong) {
		this.gpsTimeLong = gpsTimeLong;
	}
}