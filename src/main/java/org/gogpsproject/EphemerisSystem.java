/*
 * Copyright (c) 2011 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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
package org.gogpsproject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.TimeZone;

import org.gogpsproject.Time;
import org.ejml.simple.SimpleMatrix; 

/**
 * <p>
 *
 * </p>
 *
 * @author Eugenio Realini, Lorenzo Patocchi (code architecture)
 */

public abstract class EphemerisSystem {

	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param satID
	 * @param range
	 * @param approxPos
	 */
	
//	double[] pos ;
	
	
	
	protected SatellitePosition computePositionGps(long unixTime, int satID, char satType, EphGps eph, double obsPseudorange, double receiverClockError) {

//		char satType = eph.getSatType() ;
		if(satType != 'R'){  // other than GLONASS
			
//					System.out.println("### other than GLONASS data");
			
					// Compute satellite clock error
					double satelliteClockError = computeSatelliteClockError(unixTime, eph, obsPseudorange);
			
					// Compute clock corrected transmission time
					double tGPS = computeClockCorrectedTransmissionTime(unixTime, satelliteClockError, obsPseudorange);
			
					// Compute eccentric anomaly
					double Ek = computeEccentricAnomaly(tGPS, eph);
			
					// Semi-major axis
					double A = eph.getRootA() * eph.getRootA();
			
					// Time from the ephemerides reference epoch
					double tk = checkGpsTime(tGPS - eph.getToe());
			
					// Position computation
					double fk = Math.atan2(Math.sqrt(1 - Math.pow(eph.getE(), 2))
							* Math.sin(Ek), Math.cos(Ek) - eph.getE());
					double phi = fk + eph.getOmega();
					phi = Math.IEEEremainder(phi, 2 * Math.PI);
					double u = phi + eph.getCuc() * Math.cos(2 * phi) + eph.getCus()
							* Math.sin(2 * phi);
					double r = A * (1 - eph.getE() * Math.cos(Ek)) + eph.getCrc()
							* Math.cos(2 * phi) + eph.getCrs() * Math.sin(2 * phi);
					double ik = eph.getI0() + eph.getiDot() * tk + eph.getCic() * Math.cos(2 * phi)
							+ eph.getCis() * Math.sin(2 * phi);
					double Omega = eph.getOmega0()
							+ (eph.getOmegaDot() - Constants.EARTH_ANGULAR_VELOCITY) * tk
							- Constants.EARTH_ANGULAR_VELOCITY * eph.getToe();
					Omega = Math.IEEEremainder(Omega + 2 * Math.PI, 2 * Math.PI);
					double x1 = Math.cos(u) * r;
					double y1 = Math.sin(u) * r;
			
					// Coordinates
			//			double[][] data = new double[3][1];
			//			data[0][0] = x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega);
			//			data[1][0] = x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega);
			//			data[2][0] = y1 * Math.sin(ik);
			
					// Fill in the satellite position matrix
					//this.coord.ecef = new SimpleMatrix(data);
					//this.coord = Coordinates.globalXYZInstance(new SimpleMatrix(data));
					SatellitePosition sp = new SatellitePosition(unixTime,satID, x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
							x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
							y1 * Math.sin(ik));
					sp.setSatelliteClockError(satelliteClockError);
			
					// Apply the correction due to the Earth rotation during signal travel time
					SimpleMatrix R = computeEarthRotationCorrection(unixTime, receiverClockError, tGPS);
					sp.setSMMultXYZ(R);
			
					return sp;
			//		this.setXYZ(x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
			//				x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
			//				y1 * Math.sin(ik));

		} else {   // GLONASS 
						
					System.out.println("### GLONASS computation");
					satID = eph.getSatID();
					double X = eph.getX();  // satellite X coordinate at ephemeris reference time
					double Y = eph.getY();  // satellite Y coordinate at ephemeris reference time
					double Z = eph.getZ();  // satellite Z coordinate at ephemeris reference time
					double Xv = eph.getXv();  // satellite velocity along X at ephemeris reference time
					double Yv = eph.getYv();  // satellite velocity along Y at ephemeris reference time
					double Zv = eph.getZv();  // satellite velocity along Z at ephemeris reference time
					double Xa = eph.getXa();  // acceleration due to lunar-solar gravitational perturbation along X at ephemeris reference time
					double Ya = eph.getYa();  // acceleration due to lunar-solar gravitational perturbation along Y at ephemeris reference time
					double Za = eph.getZa();  // acceleration due to lunar-solar gravitational perturbation along Z at ephemeris reference time
					/* NOTE:  Xa,Ya,Za are considered constant within the integration interval (i.e. toe ?}15 minutes) */
				
					double tn = eph.getTauN();    
					float gammaN = eph.getGammaN();
					double tk = eph.gettk();   
					double En = eph.getEn();
					double toc = eph.getToc();
					double toe = eph.getToe();
					
					/*
					String refTime = eph.getRefTime().toString();
//					refTime = refTime.substring(0,10);
					refTime = refTime.substring(0,19);
//					refTime = refTime + " 00 00 00";
					System.out.println("refTime: " + refTime);
					
					try {
							// Set GMT time zone
							TimeZone zone = TimeZone.getTimeZone("GMT Time");
//							TimeZone zone = TimeZone.getTimeZone("UTC+4");
							DateFormat df = new java.text.SimpleDateFormat("yyyy MM dd HH mm ss");
							df.setTimeZone(zone);
	
							long ut = df.parse(refTime).getTime() ;
							System.out.println("ut: " + ut);
							Time tm = new Time(ut); 
							double gpsTime = tm.getGpsTime();
	//						double gpsTime = tm.getRoundedGpsTime();
							System.out.println("gpsT: " + gpsTime);
							
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
						
					
//					System.out.println("refTime: " + refTime);
					System.out.println("toc: " + toc);
					System.out.println("toe: " + toe);
					System.out.println("unixTime: " + unixTime);				
					System.out.println("satID: " + satID);
					System.out.println("X: " + X);
					System.out.println("Y: " + Y);
					System.out.println("Z: " + Z);
					System.out.println("Xv: " + Xv);
					System.out.println("Yv: " + Yv);
					System.out.println("Zv: " + Zv);
					System.out.println("Xa: " + Xa);
					System.out.println("Ya: " + Ya);
					System.out.println("Za: " + Za);
					System.out.println("tn: " + tn);
					System.out.println("gammaN: " + gammaN);
//					System.out.println("tb: " + tb);
					System.out.println("tk: " + tk);
					System.out.println("En: " + En);
					System.out.println("					");
					
					/* integration step */
				    int int_step = 60 ; // [s]	
					
					/* Compute satellite clock error */
				    double satelliteClockError = computeSatelliteClockError(unixTime, eph, obsPseudorange);
				    System.out.println("satelliteClockError: " + satelliteClockError);
				    
					/* Compute clock corrected transmission time */
					double tGPS = computeClockCorrectedTransmissionTime(unixTime, satelliteClockError, obsPseudorange);
					tGPS = eph.getTow()*7*86400 + tGPS;
				    System.out.println("tGPS: " + tGPS);
					
				    /* Time from the ephemerides reference epoch */
					double tk2 = checkGpsTime(tGPS - toe);
					System.out.println("tk2: " + tk2);
				    
				    /* number of iterations on "full" steps */
					int n = (int) Math.floor(Math.abs(tk2 / int_step));
					System.out.println("Number of interations: " + n);
					
					/* array containing integration steps (same sign as tk) */
					 double [][] tkArray = new double [n][1];
//					 double ii = tkArray * int_step * (tk2/Math.abs(tk2));
					
					// numerical integration steps (i.e. re-calculation of satellite positions from toe to tk)
					double[] pos = {X, Y, Z};
					double[] vel = {Xv, Yv, Zv};
					double[] acc = {Xa, Ya, Za};				
					double[] pos1;
					double[] vel1;
									
					
//					for (int i = 0 ; i < n ; i++ ){
//						
//							/* Runge-Kutta numerical integration algorithm */
//					        // step 1 
//							pos1 = pos;
//							vel1 = vel;
//							
//							// differential position
//							double[] pos1_dot = vel;
//							double[] vel1_dot = satellite_motion_diff_eq(pos1, vel1, acc, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
//							
//							// step 2
//							double[] pos2 = pos + pos1_dot*ii(s)/2;
//					        double[] vel2 = vel + vel1_dot*ii(s)/2;
//							double[] pos2_dot = vel2;						
//							double[] vel2_dot = satellite_motion_diff_eq(pos2, vel2, acc, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
//							
//							// step 3											
//							double[] pos3 = pos + pos1_dot*ii(s)/2;
//					        double[] vel3 = vel + vel1_dot*ii(s)/2;
//					        double[] pos3_dot = vel3;
//							double[] vel3_dot = satellite_motion_diff_eq(pos3, vel3, acc, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
//							
//							// step 4
//							double[] pos4 = pos + pos1_dot*ii(s)/2;
//					        double[] vel4 = vel + vel1_dot*ii(s)/2;
//							double[] pos4_dot = vel4;
//							double[] vel4_dot = satellite_motion_diff_eq(pos4, vel4, acc, Constants.ELL_A_GLO, Constants.GM_GLO, Constants.J2_GLO, Constants.OMEGAE_DOT_GLO);
//						
//							// final position and velocity
//						    pos = pos + (pos1_dot + 2*pos2_dot + 2*pos3_dot + pos4_dot)*ii(s)/6;
//						    vel = vel + (vel1_dot + 2*vel2_dot + 2*vel3_dot + vel4_dot)*ii(s)/6;
//						
//					}
										
									
					/* transformation from PZ-90.02 to WGS-84 (ITRF2000) */
					double x1 = X - 0.36;
					double y1 = Y + 0.86;
					double z1 = Z + 0.18;
					
					/* satellite velocity */
				    double Xv1 = vel[0];
				    double Yv1 = vel[1];
				    double Zv1 = vel[2];
					
					// Fill in the satellite position matrix
//				
//					SatellitePosition sp = new SatellitePosition(unixTime,satID, x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
//							x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
//							y1 * Math.sin(ik));
//					sp.setSatelliteClockError(satelliteClockError);
//		
//					// Apply the correction due to the Earth rotation during signal travel time
//					SimpleMatrix R = computeEarthRotationCorrection(unixTime, receiverClockError, tGPS);
//					sp.setSMMultXYZ(R);
		
//					return sp ;
					return null ;
		
			
		}
		
		
		
	}

	private double[] satellite_motion_diff_eq(double[] pos, double[] vel,
			double[] acc, long ellAGlo, double gmGlo, double j2Glo,
			double omegaeDotGlo) {
		// TODO Auto-generated method stub
		
		
		/* renaming variables for better readability position */
		double X = pos[0];
		double Y = pos[1];
		double Z = pos[2];
		
		/* velocity */
		double Xv = vel[0];
		double Yv = vel[1];
		
		/* acceleration (i.e. perturbation) */
		double Xa = acc[1];
		double Ya = acc[2];
		double Za = acc[3];
		
		/* parameters */
		double r = Math.sqrt(Math.pow(X,2) + Math.pow(Y,2) + Math.pow(Z,2));
		double g = -gmGlo/Math.pow(r,3);
		double h = j2Glo*1.5*Math.pow((ellAGlo/r),2);
		double k = 5*Math.pow(Z,2)/Math.pow(r,2);
		
		/* differential velocity */
		double[] vel_dot = new double[2] ;
		vel_dot[0] = g*X*(1 - h*(k - 1)) + Xa + Math.pow(omegaeDotGlo,2)*X + 2*omegaeDotGlo*Yv;
		vel_dot[1] = g*Y*(1 - h*(k - 1)) + Ya + Math.pow(omegaeDotGlo,2)*Y - 2*omegaeDotGlo*Xv;
		vel_dot[2] = g*Z*(1 - h*(k - 3)) + Za;
		
		return vel_dot;
	}

	/**
	 * @param time
	 *            (Uncorrected GPS time)
	 * @return GPS time accounting for beginning or end of week crossover
	 */
	protected double checkGpsTime(double time) {

		// Account for beginning or end of week crossover
		if (time > Constants.SEC_IN_HALF_WEEK) {
			time = time - 2 * Constants.SEC_IN_HALF_WEEK;
		} else if (time < -Constants.SEC_IN_HALF_WEEK) {
			time = time + 2 * Constants.SEC_IN_HALF_WEEK;
		}
		return time;
	}

	/**
	 * @param traveltime
	 */
	protected SimpleMatrix computeEarthRotationCorrection(long unixTime, double receiverClockError, double transmissionTime) {

		// Computation of signal travel time
		// SimpleMatrix diff = satellitePosition.minusXYZ(approxPos);//this.coord.minusXYZ(approxPos);
		// double rho2 = Math.pow(diff.get(0), 2) + Math.pow(diff.get(1), 2)
		// 		+ Math.pow(diff.get(2), 2);
		// double traveltime = Math.sqrt(rho2) / Constants.SPEED_OF_LIGHT;
		double receptionTime = (new Time(unixTime)).getGpsTime();
		double traveltime = receptionTime + receiverClockError - transmissionTime;

		// Compute rotation angle
		double omegatau = Constants.EARTH_ANGULAR_VELOCITY * traveltime;

		// Rotation matrix
		double[][] data = new double[3][3];
		data[0][0] = Math.cos(omegatau);
		data[0][1] = Math.sin(omegatau);
		data[0][2] = 0;
		data[1][0] = -Math.sin(omegatau);
		data[1][1] = Math.cos(omegatau);
		data[1][2] = 0;
		data[2][0] = 0;
		data[2][1] = 0;
		data[2][2] = 1;
		SimpleMatrix R = new SimpleMatrix(data);

		return R;
		// Apply rotation
		//this.coord.ecef = R.mult(this.coord.ecef);
		//this.coord.setSMMultXYZ(R);// = R.mult(this.coord.ecef);
		//satellitePosition.setSMMultXYZ(R);// = R.mult(this.coord.ecef);

	}

	/**
	 * @param eph
	 * @return Clock-corrected GPS transmission time
	 */
	protected double computeClockCorrectedTransmissionTime(long unixTime, double satelliteClockError, double obsPseudorange) {

		double gpsTime = (new Time(unixTime)).getGpsTime();

		// Remove signal travel time from observation time
		double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

		return tRaw - satelliteClockError;
	}

	/**
	 * @param eph
	 * @return Satellite clock error
	 */
	protected double computeSatelliteClockError(long unixTime, EphGps eph, double obsPseudorange){
		
		if (eph.getSatType() == 'R'){   // In case of GLONASS
			
				double gpsTime = (new Time(unixTime)).getGpsTime();
				System.out.println("## gpsTime: " + gpsTime);
				System.out.println("## obsPseudorange: " + obsPseudorange);

				// Remove signal travel time from observation time
				double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);		
				System.out.println("## tRaw: " + tRaw);
				
				tRaw = eph.getTow()*7*86400 + tRaw;
//				double toe = tow*7*86400 + toc;
				System.out.println("## tRaw2: " + tRaw);
				
				double toe = eph.getToe() ;
				System.out.println("## toe: " + toe);
				
				// Clock error computation
				double dt = checkGpsTime(tRaw - eph.getToe());
				System.out.println("## dt: " + dt);
				
				double timeCorrection =  eph.getTauN() + eph.getGammaN() * dt ;			
//				double timeCorrection =  - eph.getTauN() + eph.getGammaN() * dt ;					
				
				return timeCorrection;
			
		}else{		// other than GLONASS
				double gpsTime = (new Time(unixTime)).getGpsTime();
				// Remove signal travel time from observation time
				double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);
		
				// Compute eccentric anomaly
				double Ek = computeEccentricAnomaly(tRaw, eph);
		
				// Relativistic correction term computation
				double dtr = Constants.RELATIVISTIC_ERROR_CONSTANT * eph.getE() * eph.getRootA() * Math.sin(Ek);
		
				// Clock error computation
				double dt = checkGpsTime(tRaw - eph.getToc());
				double timeCorrection = (eph.getAf2() * dt + eph.getAf1()) * dt + eph.getAf0() + dtr - eph.getTgd();
				double tGPS = tRaw - timeCorrection;
				dt = checkGpsTime(tGPS - eph.getToc());
				timeCorrection = (eph.getAf2() * dt + eph.getAf1()) * dt + eph.getAf0() + dtr - eph.getTgd();
		
				return timeCorrection;		
		}
	}

	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param eph
	 * @return Eccentric anomaly
	 */
	protected double computeEccentricAnomaly(double time, EphGps eph) {

		// Semi-major axis
		double A = eph.getRootA() * eph.getRootA();

		// Time from the ephemerides reference epoch
		double tk = checkGpsTime(time - eph.getToe());

		// Computed mean motion [rad/sec]
		double n0 = Math.sqrt(Constants.EARTH_GRAVITATIONAL_CONSTANT / Math.pow(A, 3));

		// Corrected mean motion [rad/sec]
		double n = n0 + eph.getDeltaN();

		// Mean anomaly
		double Mk = eph.getM0() + n * tk;

		// Eccentric anomaly starting value
		Mk = Math.IEEEremainder(Mk + 2 * Math.PI, 2 * Math.PI);
		double Ek = Mk;

		int i;
		double EkOld, dEk;

		// Eccentric anomaly iterative computation
		for (i = 0; i < 10; i++) {
			EkOld = Ek;
			Ek = Mk + eph.getE() * Math.sin(Ek);
			dEk = Math.IEEEremainder(Ek - EkOld, 2 * Math.PI);
			if (Math.abs(dEk) < 1e-12)
				break;
		}

		// TODO Display/log warning message
		if (i == 10)
			System.out.println("Eccentric anomaly does not converge");

		return Ek;

	}

}
