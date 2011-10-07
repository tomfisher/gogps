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
package org.gogpsproject;

import java.io.IOException;
import java.util.Calendar;

import org.gogpsproject.parser.rtcm3.RTCM3Client;
import org.gogpsproject.producer.RinexProducer;
/**
 * <p>
 *
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */

/**
 * @author Lorenzo
 *
 */
public class ConvertToRinex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
		int yy = c.get(Calendar.YEAR)-2000;
		int p=0;
		String inFile = args[p++];
		String outFile = inFile.indexOf(".dat")>0?inFile.substring(0, inFile.indexOf(".dat"))+"."+yy+"o":inFile+"."+yy+"o";

		System.out.println("in :"+inFile);
		System.out.println("out:"+outFile);

		ObservationsBuffer masterIn = new ObservationsBuffer();
		try {
			masterIn.readFromLog(inFile,true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("RINEX");
		RinexProducer rp = new RinexProducer(outFile, args!=null&&args.length>=p+1&&args[p++].startsWith("y"));
		rp.setDefinedPosition(masterIn.getDefinedPosition());

		Observations o = masterIn.nextObservations();
		while(o!=null){
			rp.addObservations(o);
			o = masterIn.nextObservations();
		}
		rp.streamClosed();
		System.out.println("END");

	}

}
