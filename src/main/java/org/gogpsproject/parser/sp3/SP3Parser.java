/*
 * Copyright (c) 2011, Lorenzo Patocchi. All Rights Reserved.
 *
 * This file is part of goGPS Project (goGPS).
 *
 * goGPS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * goGPS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with goGPS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.gogpsproject.parser.sp3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
/**
 * <p>
 * This file parses SP3c satellite positioning files
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.gogpsproject.NavigationProducer;
import org.gogpsproject.SatellitePosition;
import org.gogpsproject.Time;

/**
 * @author Lorenzo
 *
 */
public class SP3Parser implements NavigationProducer{
	private File fileSP3;
	private FileInputStream fileInputStream;
	private InputStreamReader inputStreamReader;
	private BufferedReader bufferedReader;

	private int gpsWeek=0;
	private int secondsOfWeek=0;
	private int epochInterval=0;
	private int nepocs=0;
	private int numSats=0;
	private String coordSys=null;
	private String orbitType=null;
	private String agency=null;

	private ArrayList<HashMap<String,SatellitePosition>> epocs;
	private ArrayList<Time> epocTimestamps;
	private ArrayList<String> satIDs;
	private HashMap<String,Long> accuracy;

	private double posStDevBase;
	private double clockStDevBase;

	public static void main(String[] args){
		File f = new File("./data/igu15231_00.sp3");
		SP3Parser sp3fp = new SP3Parser(f);
		sp3fp.init();
	}

	// RINEX Read constructors
	public SP3Parser(File fileSP3) {
		this.fileSP3 = fileSP3;
	}

	// RINEX Read constructors
	public SP3Parser(InputStream is) {
		this.inputStreamReader = new InputStreamReader(is);
	}


	public void init() {
		open();
		if(parseHeader()){
			parseData();
		}
		close();

		//System.out.println("Found "+epocs.size()+" epocs");
	}


	/**
	 *
	 */
	public void open() {
		try {
			if(fileSP3!=null) fileInputStream = new FileInputStream(fileSP3);
			if(fileInputStream!=null) inputStreamReader = new InputStreamReader(fileInputStream);
			if(inputStreamReader!=null) bufferedReader = new BufferedReader(inputStreamReader);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	public void close() {
		try {
			if(fileInputStream!=null) fileInputStream.close();
			if(inputStreamReader!=null) inputStreamReader.close();
			if(bufferedReader!=null) bufferedReader.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * @see org.gogpsproject.Navigation#release()
	 */
	public void release() {

	}

	/**
	 *
	 */
	public boolean parseHeader() {

		try {
			int nline=0;
			int nsat=0;
			while (bufferedReader.ready()) {

				try {
					String line = bufferedReader.readLine();
					System.out.println(line);
					nline++;

					if(nline == 22){
						return true;
					}
					switch(nline){
						case 1:
							// line 1
							String typeField = line.substring(1,2);
							typeField = typeField.trim();
							if(!typeField.equals("c")){
								System.err.println("SP3c file identifier is missing in file " + fileInputStream.toString() + " header");
								return false;
							}
							int year = Integer.parseInt(line.substring(3,7).trim());
							int month = Integer.parseInt(line.substring(8,10).trim());
							int day = Integer.parseInt(line.substring(11,13).trim());
							int hh = Integer.parseInt(line.substring(14,16).trim());
							int mm = Integer.parseInt(line.substring(17,19).trim());
							double ss = Double.parseDouble(line.substring(20,31).trim());
							nepocs = Integer.parseInt(line.substring(32,39).trim());
							String dataUsed = line.substring(40,45).trim();
							coordSys = line.substring(46,51).trim();
							orbitType = line.substring(52,55).trim();
							agency = line.substring(56,60).trim();
							break;
						case 2:
							gpsWeek = Integer.parseInt(line.substring(3,7).trim());
							secondsOfWeek = Integer.parseInt(line.substring(8,14).trim());
							epochInterval = Integer.parseInt(line.substring(24,29).trim())*1000; // transform to ms
							long modJulDayStart = Long.parseLong(line.substring(39,44).trim());
							double factionalDay = Double.parseDouble(line.substring(45,60).trim());
							break;
						case 3:
							numSats = Integer.parseInt(line.substring(4,6).trim());
							satIDs = new ArrayList<String>(numSats);
							accuracy = new HashMap<String, Long>(numSats);
						case 4:case 5:case 6:case 7:
							for(int c=0;c<17;c++){
								String sat=line.substring(9+c*3, 12+c*3).trim();
								if(!sat.equals("0")){
									sat = sat.replace(' ', '0');
									satIDs.add(sat);
								}
							}
							break;
						case 8:case 9:case 10:case 11:case 12:
							for(int c=0;c<17;c++){
								String acc=line.substring(9+c*3, 12+c*3).trim();
								String sat=nsat<satIDs.size()?satIDs.get(nsat):null;
								if(sat!=null){
									if(!acc.equals("0")){
										accuracy.put(sat, new Long((long)Math.pow(2, Integer.parseInt(acc))));
									}else{
										accuracy.put(sat, null); // unknown
									}
								}
								nsat++;
							}
							break;
						case 13:
							String fileType = line.substring(3,5).trim();
							String timeSystem = line.substring(9,12).trim();
							break;
						case 15:
							posStDevBase = Double.parseDouble(line.substring(3,13).trim());
							clockStDevBase = Double.parseDouble(line.substring(14,26).trim());
							break;
					}

				} catch (StringIndexOutOfBoundsException e) {
					// Skip over blank lines
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void parseData() {

		try {
			Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone("UTC"));

			epocs = new ArrayList<HashMap<String,SatellitePosition>>();
			epocTimestamps = new ArrayList<Time>();

			HashMap<String,SatellitePosition> epoc = null;
			Time ts = null;

			while (bufferedReader.ready()) {

				try {
					String line = bufferedReader.readLine();
					//System.out.println(line);
					if(line == null || line.toUpperCase().startsWith("EOF")){
						return;
					}
					if(line.charAt(0) == '*'){
						int year = Integer.parseInt(line.substring(3,7).trim());
						int month = Integer.parseInt(line.substring(8,10).trim());
						int day = Integer.parseInt(line.substring(11,13).trim());
						int hh = Integer.parseInt(line.substring(14,16).trim());
						int mm = Integer.parseInt(line.substring(17,19).trim());
						double ss = Double.parseDouble(line.substring(20,31).trim());


						c.set(Calendar.YEAR, year);
						c.set(Calendar.MONTH, month-1);
						c.set(Calendar.DAY_OF_MONTH, day);
						c.set(Calendar.HOUR_OF_DAY, hh);
						c.set(Calendar.MINUTE, mm);
						c.set(Calendar.SECOND, (int)ss);
						int ms = (int)((ss-((int)ss))*1000.0);
						c.set(Calendar.MILLISECOND, ms);

						epoc = new HashMap<String, SatellitePosition>(numSats);
						ts = new Time(c.getTimeInMillis());

						epocs.add(epoc);
						epocTimestamps.add(ts);

					}else
					if(epoc != null && line.charAt(0) == 'P'){
						String satid = line.substring(1,4).trim();
						satid = satid.replace(' ', '0');
						double x = Double.parseDouble(line.substring(4, 18).trim())*1000.0;
						double y = Double.parseDouble(line.substring(18, 32).trim())*1000.0;
						double z = Double.parseDouble(line.substring(32, 46).trim())*1000.0;
						double clock = Double.parseDouble(line.substring(46, 60).trim());

						int xStDev = -1;
						int yStDev = -1;
						int zStDev = -1;
						int clkStDev = -1;
						try{
							xStDev = (int)Math.pow(posStDevBase, (double)Integer.parseInt(line.substring(61, 63).trim()));
						}catch(NumberFormatException nfe){}
						try{
							yStDev = (int)Math.pow(posStDevBase, (double)Integer.parseInt(line.substring(64, 66).trim()));
						}catch(NumberFormatException nfe){}
						try{
							zStDev = (int)Math.pow(posStDevBase, (double)Integer.parseInt(line.substring(67, 69).trim()));
						}catch(NumberFormatException nfe){}
						try{
							clkStDev = (int)Math.pow(clockStDevBase, (double)Integer.parseInt(line.substring(70, 73).trim()));
						}catch(NumberFormatException nfe){}
						boolean clockEventFlag = line.length()>74 && line.charAt(74) == 'E';
						boolean clockPredFlag = line.length()>75 && line.charAt(75) == 'P';
						boolean maneuverFlag = line.length()>78 && line.charAt(78) == 'M';
						boolean orbitPredFlag = line.length()>79 && line.charAt(79) == 'P';


						SatellitePosition sp = new SatellitePosition(ts.getMsec(), Integer.parseInt(satid.substring(1).trim()), x, y, z);
						sp.setTimeCorrection(clock);
						sp.setPredicted(orbitPredFlag||clockPredFlag);
						sp.setManeuver(maneuverFlag);
						// TODO map all the values
						epoc.put(satid, sp);

						//System.out.println(""+satid+" "+(new Date(sp.getTime())));
					}


				} catch (StringIndexOutOfBoundsException e) {
					// Skip over blank lines
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
	 */
	@Override
	public SatellitePosition getGpsSatPosition(long time, int satID, double range) {
		if(isTimestampInEpocsRange(time)){
			for(int i=0;i<epocTimestamps.size();i++){
				if(epocTimestamps.get(i).getMsec()<=time && time < epocTimestamps.get(i).getMsec()+epochInterval){
					return epocs.get(i).get("G"+(satID<10?"0":"")+satID);
				}
			}
		}
		return null;
	}

	public boolean isTimestampInEpocsRange(long time){
		return epocTimestamps.size()>0 &&
				epocTimestamps.get(0).getMsec() <= time &&
				time < epocTimestamps.get(epocTimestamps.size()-1).getMsec()+epochInterval;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getIono(int)
	 */
	@Override
	public double getIono(int i) {

		return 0;
	}



	/**
	 * @return the gpsWeek
	 */
	public int getGpsWeek() {
		return gpsWeek;
	}


	/**
	 * @return the secondsOfWeek
	 */
	public int getSecondsOfWeek() {
		return secondsOfWeek;
	}


	/**
	 * @return the epochInterval
	 */
	public int getEpochInterval() {
		return epochInterval;
	}


	/**
	 * @return the nepocs
	 */
	public int getNumEpocs() {
		return nepocs;
	}


	/**
	 * @return the numSats
	 */
	public int getNumSats() {
		return numSats;
	}


	/**
	 * @return the coordSys
	 */
	public String getCoordSys() {
		return coordSys;
	}


	/**
	 * @return the orbitType
	 */
	public String getOrbitType() {
		return orbitType;
	}


	/**
	 * @return the agency
	 */
	public String getAgency() {
		return agency;
	}


	/**
	 * @return the accuracy
	 */
	public HashMap<String, Long> getAccuracy() {
		return accuracy;
	}


	/**
	 * @return the posStDevBase
	 */
	public double getPosStDevBase() {
		return posStDevBase;
	}


	/**
	 * @return the clockStDevBase
	 */
	public double getClockStDevBase() {
		return clockStDevBase;
	}
}