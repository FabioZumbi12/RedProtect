package br.net.fabiozumbi12.RedProtect.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;

public class RPYaml extends YamlConfiguration {

  public void Yaml() {}

  private File file;

  public File getFile(){
	  return this.file;
  }
  
  public void load(File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
	  this.file = file;
	  load(new FileInputStream(file));
  }
  
  /**Save to file if loaded by {@code load(File file)}.
  * @throws IOException If file is null.
  */
  public void save() throws IOException{
	  Validate.notNull(file, "File cannot be null");
	  save(this.file);
  }

  public void load(InputStream stream) throws IOException, InvalidConfigurationException {
    Validate.notNull(stream, "Stream cannot be null");

    InputStreamReader reader = new InputStreamReader(stream, Charset.forName("UTF-8"));
    StringBuilder builder = new StringBuilder();
    BufferedReader input = new BufferedReader(reader);
    try {
      String line;
      while ((line = input.readLine()) != null) {
    	  builder.append(line);
          builder.append('\n');
      }
    } finally {
      input.close();    
  }

   loadFromString(builder.toString());
  }

  @Override
  public void save(File file) throws IOException {
    Validate.notNull(file, "File cannot be null");

    Files.createParentDirs(file);
    String data = saveToString();

    FileOutputStream stream = new FileOutputStream(file);
    OutputStreamWriter writer = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
    try {
      writer.write(data);
    } finally {
      writer.close();
    }
  }

}