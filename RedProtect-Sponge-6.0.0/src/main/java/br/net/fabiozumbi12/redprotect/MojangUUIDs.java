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
package br.net.fabiozumbi12.redprotect;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MojangUUIDs {

	public static String getName(String UUID) {
		try {
		   URL url = new URL("https://api.mojang.com/user/profiles/"+ UUID.replaceAll("-", "") + "/names");
		   BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		   String line = in.readLine();
		   if (line == null){
			   return null;
		   }
		   JsonArray array = (JsonArray) new JsonParser().parse(line);
		   HashMap<Long, String> names = new HashMap<Long, String>();
		   String name = "";
		   for (Object profile : array) {
			   JsonObject jsonProfile = (JsonObject) profile;
			   if (jsonProfile.has("changedToAt")){
				   names.put(jsonProfile.get("changedToAt").getAsLong(), jsonProfile.get("name").getAsString());
				   continue;
			   }			   
			   name = jsonProfile.get("name").getAsString();
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
		  JsonObject jsonProfile = (JsonObject) new JsonParser().parse(line);
		  String name = jsonProfile.get("id").getAsString();		
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