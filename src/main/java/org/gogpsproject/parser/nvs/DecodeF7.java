/*
 * Copyright (c) 2010 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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

package org.gogpsproject.parser.nvs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.gogpsproject.EphGps;
import org.gogpsproject.ObservationSet;
import org.gogpsproject.Observations;
import org.gogpsproject.Time;
import org.gogpsproject.util.Bits;
import org.gogpsproject.util.UnsignedOperation;


public class DecodeF7 {

	private final static double TWO_P_4   = 16.0; // 2^4
	private final static double TWO_P_M5  = 0.03125; // 2^-5
	private final static double TWO_P_M19 = 0.0000019073486328125; // 2^-19
	private final static double TWO_P_M29 = 0.00000000186264514923095703125; // 2^-29
	private final static double TWO_P_M31 = 0.0000000004656612873077392578125; // 2^-31
	private final static double TWO_P_M33 = 1.16415321826934814453125e-10; // 2^-33
	private final static double TWO_P_M43 = 1.136868377216160297393798828125e-13; // 2^-43
	private final static double TWO_P_M55 = 2.7755575615628913510590791702271e-17; // 2^-55
	private final static double PI        = 3.141592653589793238462643383279502884197169399375105820974944592;

	InputStream in;

	public DecodeF7(InputStream _in) {
		in = _in;
	}

	public EphGps decode() throws IOException,NVSException {
		// parse little Endian data


		EphGps eph = new EphGps();
		int satType = in.read();  // satType 1 = GPS, 2 = GLONASS		
		System.out.println("satType: " + satType);  
		int satId = in.read();
		System.out.println("svid: " + satId);  
		
		eph.setSatID((int)satId);

		byte bytes[];
		int signInt;
		String signStr; 
		int espInt;
		String espStr;
		long mantInt;
		String mantStr; 
		double mantInt2;
		double crs;
	
		
		/*  Crs, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		
		String binCrs = "";
		for (int j = 3; j >= 0; j--) {   // for little endian
	//		boolean[] temp1 = Bits.intToBits(bytes[j], 8);
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
		//	System.out.println("CRS"+ j + ": " + bytes[j] );
		//	System.out.println("CRS"+ j + ": " + temp0 );
			binCrs =  binCrs + temp0  ;
		}
		//System.out.println("binCRS: " + binCrs );
		signStr = binCrs.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binCrs.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binCrs.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = (double) (mantInt / Math.pow(2, 23));
        crs = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Crs: "+ crs);
	
        
        /*  Dn, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		
		String binDn = "";
		for (int j = 3; j >= 0; j--) {	 // for little endian
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
			binDn =  binDn + temp0  ;
		}
		signStr = binDn.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binDn.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binDn.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = mantInt / Math.pow(2, 23);
        double dn = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Dn: "+ dn);
   
        
        /*  M0, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		
		String binM0 = "";
		for (int j = 7; j >= 0; j--) {	  // for little endian
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
			binM0 =  binM0 + temp0  ;
		}
		signStr = binM0.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binM0.substring(1,12);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binM0.substring(12,64);
        mantInt = Long.parseLong(mantStr, 2);
       // mantInt = Double.parseDouble(mantStr); 
//        mantInt = (double) (mantInt / Math.pow(2, 52));
        mantInt2 = mantInt / Math.pow(2, 52);
//        double m0 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 1023)) * (1 + mantInt));
        double m0 = Math.pow(-1, signInt) * Math.pow(2, (espInt - 1023)) * (1 + mantInt2);   // FP64
        System.out.println("M0: "+ m0);
        
        
        /*  Cuc, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		
		String binCuc = "";
		for (int j = 3; j >= 0; j--) {	 // for little endian
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
			binCuc =  binCuc + temp0  ;
		}
		signStr = binCuc.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binCuc.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binCuc.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = mantInt / Math.pow(2, 23);
        double cuc = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Cuc: "+ cuc);
        
        
        /*  E, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		
		String binE = "";
		for (int j = 7; j >= 0; j--) {	  // for little endian
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
			binE =  binE + temp0  ;
		}
		signStr = binE.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binE.substring(1,12);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binE.substring(12,64);
        mantInt = Long.parseLong(mantStr, 2);
       // mantInt = Double.parseDouble(mantStr); 
//        mantInt = (double) (mantInt / Math.pow(2, 52));
        mantInt2 = mantInt / Math.pow(2, 52);
//        double m0 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 1023)) * (1 + mantInt));
        double e = Math.pow(-1, signInt) * Math.pow(2, (espInt - 1023)) * (1 + mantInt2);   // FP64
        System.out.println("E: "+ e);
        
        
        /*  Cus, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		
		String binCus = "";
		for (int j = 3; j >= 0; j--) {	 // for little endian
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
			binCus =  binCus + temp0  ;
		}
		signStr = binCus.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binCus.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binCus.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = mantInt / Math.pow(2, 23);
        double cus = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Cus: "+ cus);
        
        
        /*  SqrtA, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		
		String binSqrtA = "";
		for (int j = 7; j >= 0; j--) {	  // for little endian
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
			binSqrtA =  binSqrtA + temp0  ;
		}
		signStr = binSqrtA.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binSqrtA.substring(1,12);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binSqrtA.substring(12,64);
        mantInt = Long.parseLong(mantStr, 2);
       // mantInt = Double.parseDouble(mantStr); 
//        mantInt = (double) (mantInt / Math.pow(2, 52));
        mantInt2 = mantInt / Math.pow(2, 52);
//        double m0 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 1023)) * (1 + mantInt));
        double sqrtA = Math.pow(-1, signInt) * Math.pow(2, (espInt - 1023)) * (1 + mantInt2);   // FP64
        System.out.println("SqrtA: "+ sqrtA);
        
        
        /*  Toe, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		
		String binToe = "";
		for (int j = 7; j >= 0; j--) {	  // for little endian
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
			binToe =  binToe + temp0  ;
		}
		signStr = binToe.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binToe.substring(1,12);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binToe.substring(12,64);
        mantInt = Long.parseLong(mantStr, 2);
       // mantInt = Double.parseDouble(mantStr); 
//        mantInt = (double) (mantInt / Math.pow(2, 52));
        mantInt2 = mantInt / Math.pow(2, 52);
//        double m0 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 1023)) * (1 + mantInt));
        double toe = Math.pow(-1, signInt) * Math.pow(2, (espInt - 1023)) * (1 + mantInt2);   // FP64
        System.out.println("Toe: "+ toe);
        
        
        /*  Cic, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		
		String binCic = "";
		for (int j = 3; j >= 0; j--) {	 // for little endian
			String temp0 = Integer.toBinaryString(bytes[j] & 0xFF);  // & 0xFF is for converting to unsigned 
			temp0 = String.format("%8s",temp0).replace(' ', '0');
			binCic =  binCic + temp0  ;
		}
		signStr = binCic.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binCic.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binCic.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = mantInt / Math.pow(2, 23);
        double cic = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Cic: "+ cic);
        
        
        
        
        
		/*
		byte bits[] = new byte[4];
		in.read(bits, 0, bits.length);
		double crs = Bits.byteToIEEE754Double(bits);
		System.out.println("CRS: " + crs );
		*/
		
//		long svid=0;
//		for(int i=3;i>=0;i--){
//			svid=svid<<8;
//			svid = svid | Bits.getUInt(bytes[i]);
//
//		}

		//byte[] bytes = new byte[1];
		//in.read(bytes, 0, bytes.length);
	//	int[] data;
		
	/*	
		int crs = in.read();


		boolean[] bits = new boolean[8];
		int indice = 0;
		boolean[] temp1 = Bits.intToBits(crs, 8);

	
		for (int i = 0; i < 8; i++) {
			bits[indice] = temp1[i];
			indice++;
		}
		int numSV = (int)Bits.bitsTwoComplement(bits);
		System.out.println("NumSV :  " + numSV + " S ");
		*/
//		System.out.println("crs: " + crs);  
		//float crs_bit = Bits.intToBits(crs, 8);
		//System.out.print("crs "+ crs_bit);
//		String binary = Integer.toBinaryString( crs );
//		System.out.println( binary );
		
		
//		System.out.print("svid "+svid);
//		baos.write(bytes);

//		System.out.println("IODC("+eph.getIodc()+"), IODE2("+IODE2+"), IODE3("+IODE3+") not matching for SVID "+svid);

		return null;
	}


}