/*
 * Copyright (c) 2015 Nate Mortensen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.net.fabiozumbi12.RedProtect.hooks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MojangUUIDs {

	public static String getName(String UUID) {
		try {
		   URL url = new URL("https://api.mojang.com/user/profiles/"+ UUID.replaceAll("-", "") + "/names");
		   BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		   String line = in.readLine();
		   if (line == null){
			   return null;
		   }
		   JSONArray array = (JSONArray) new JSONParser().parse(line);
		   HashMap<Long, String> names = new HashMap<Long, String>();
		   String name = "";
		   for (Object profile : array) {
			   JSONObject jsonProfile = (JSONObject) profile;
			   if (jsonProfile.containsKey("changedToAt")){
				   names.put((long)jsonProfile.get("changedToAt"), (String)jsonProfile.get("name"));
				   continue;
			   }			   
			   name = (String) jsonProfile.get("name");
		   }	
		   if (!names.isEmpty()){
			   Long key = Collections.max(names.keySet());
			   return names.get(key);
		   } else {
			   return name;
		   }
		} catch (Exception ex) {
		   ex.printStackTrace();
		}
		   return null;
		}
	
	public static String getUUID(String player) {
		try {
		  URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + player);
		  BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		  String line = in.readLine();
		  if (line == null){
		     return null;
		  }
		  JSONObject jsonProfile = (JSONObject) new JSONParser().parse(line);
		  String name = (String) jsonProfile.get("id");		
		  return toUUID(name);
		} catch (Exception ex) {
		   ex.printStackTrace();
		}
		return null;
	}
	
	private static String toUUID(String uuid){
		return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-"
				   + uuid.substring(12, 16) + "-" + uuid.substring(16, 20)
				   + "-" + uuid.substring(20, 32);
	}
	
	public static String getName(UUID uuid) {
		return getName(uuid.toString());
	}
	    
}